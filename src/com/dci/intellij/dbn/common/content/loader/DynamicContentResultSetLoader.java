package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusHolder;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataFactory;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DynamicContentResultSetLoader<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private static final Logger LOGGER = LoggerFactory.createLogger();

    private boolean master;

    public DynamicContentResultSetLoader(
            @Nullable DynamicContentType parentContentType,
            @NotNull DynamicContentType contentType,
            boolean register,
            boolean master) {

        super(parentContentType, contentType, register);
        this.master = master;
    }

    public abstract ResultSet createResultSet(DynamicContent<T> dynamicContent, DBNConnection connection) throws SQLException;
    public abstract T createElement(DynamicContent<T> content, M metadata, LoaderCache cache) throws SQLException;

    private static class DebugInfo {
        private String id = UUID.randomUUID().toString();
        private long startTimestamp = System.currentTimeMillis();
    }

    private DebugInfo preLoadContent(DynamicContent dynamicContent) {
        if (DatabaseNavigator.DEBUG) {
            DebugInfo debugInfo = new DebugInfo();
            LOGGER.info(
                    "[DBN-INFO] Loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ")");
            return debugInfo;
        }
        return null;
    }

    private void postLoadContent(DynamicContent dynamicContent, DebugInfo debugInfo) {
        if (debugInfo != null) {
            LOGGER.info(
                    "[DBN-INFO] Done loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ") - " +
                    (System.currentTimeMillis() - debugInfo.startTimestamp) + "ms"   );
        }
    }

    @Override
    public void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws SQLException {
        ConnectionHandler connectionHandler = dynamicContent.getConnectionHandler();

        DatabaseInterface.run(
                connectionHandler,
                (interfaceProvider) -> {
                    boolean addDelay = DatabaseNavigator.getInstance().isSlowDatabaseModeEnabled();
                    ProgressMonitor.setTaskDescription("Loading " + dynamicContent.getContentDescription());

                    DebugInfo debugInfo = preLoadContent(dynamicContent);

                    dynamicContent.checkDisposed();
                    AtomicInteger count = new AtomicInteger();
                    IncrementalStatusAdapter<ConnectionHandlerStatusHolder, ConnectionHandlerStatus> loading = connectionHandler.getConnectionStatus().getLoading();
                    try {
                        loading.set(true);
                        dynamicContent.checkDisposed();
                        DBNConnection connection = connectionHandler.getPoolConnection(true);
                        ResultSet resultSet = null;
                        List<T> list = null;
                        try {
                            connection.set(ResourceStatus.ACTIVE, true);
                            dynamicContent.checkDisposed();
                            resultSet = createResultSet(dynamicContent, connection);

                            DynamicContentType contentType = dynamicContent.getContentType();
                            M metadata = DBObjectMetadataFactory.INSTANCE.create(contentType, resultSet);

                            if (addDelay) Thread.sleep(500);
                            LoaderCache loaderCache = new LoaderCache();
                            while (resultSet != null && resultSet.next()) {
                                if (addDelay) Thread.sleep(10);
                                dynamicContent.checkDisposed();

                                T element = null;
                                try {
                                    element = createElement(dynamicContent, metadata, loaderCache);
                                } catch (ProcessCanceledException e) {
                                    return;
                                } catch (RuntimeException e) {
                                    LOGGER.warn("Failed to create element", e);
                                }

                                dynamicContent.checkDisposed();
                                if (element != null) {
                                    if (list == null) {
                                        list = dynamicContent.is(DynamicContentStatus.CONCURRENT) ?
                                                CollectionUtil.createConcurrentList() :
                                                new ArrayList<T>();
                                    }
                                    list.add(element);
                                    if (count.get() % 10 == 0) {
                                        String description = element.getDescription();
                                        if (description != null)
                                            ProgressMonitor.setSubtaskDescription(description);
                                    }
                                    count.incrementAndGet();
                                }
                            }
                        } finally {
                            ResourceUtil.close(resultSet);
                            connection.set(ResourceStatus.ACTIVE, false);
                            connectionHandler.freePoolConnection(connection);
                        }
                        dynamicContent.checkDisposed();
                        dynamicContent.setElements(list);
                        dynamicContent.set(DynamicContentStatus.MASTER, master);

                        postLoadContent(dynamicContent, debugInfo);

                    } catch (InterruptedException | ProcessCanceledException e) {
                        throw new SQLTimeoutException(e);

                    } catch (SQLTimeoutException |
                            SQLFeatureNotSupportedException |
                            SQLTransientConnectionException |
                            SQLNonTransientConnectionException e) {
                        throw e;

                    } catch (SQLException e) {
                        boolean modelException = interfaceProvider.getMessageParserInterface().isModelException(e);
                        if (modelException) {
                            throw new SQLFeatureNotSupportedException(e);
                        }
                        throw e;
                    } catch (Throwable e) {
                        throw new SQLException(e);

                    } finally {
                        loading.set(false);
                    }
        });
    }

    public class LoaderCache {
        private String name;
        private DBObject object;
        public DBObject getObject(String name) {
            if (name.equals(this.name)) {
                return object;
            }
            return null;
        }

        public void setObject(String name, DBObject object) {
            this.name = name;
            this.object = object;
        }
    }
}
