package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentStatus;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatus;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusHolder;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.connection.jdbc.ResourceStatus;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.util.SkipEntrySQLException;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DynamicContentResultSetLoader<T extends DynamicContentElement> extends DynamicContentLoaderImpl<T> implements DynamicContentLoader<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private boolean master;

    public DynamicContentResultSetLoader(@Nullable DynamicContentType parentContentType, @NotNull DynamicContentType contentType, boolean register, boolean master) {
        super(parentContentType, contentType, register);
        this.master = master;
    }

    public abstract ResultSet createResultSet(DynamicContent<T> dynamicContent, DBNConnection connection) throws SQLException;
    public abstract T createElement(DynamicContent<T> dynamicContent, ResultSet resultSet, LoaderCache loaderCache) throws SQLException;

    private static class DebugInfo {
        private String id = UUID.randomUUID().toString();
        private long startTimestamp = System.currentTimeMillis();
    }

    private DebugInfo preLoadContent(DynamicContent dynamicContent) {
        if (DatabaseNavigator.debugModeEnabled) {
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
    public void loadContent(DynamicContent<T> dynamicContent, boolean forceReload) throws DynamicContentLoadException, InterruptedException {
        boolean addDelay = DatabaseNavigator.getInstance().isSlowDatabaseModeEnabled();
        ProgressMonitor.setTaskDescription("Loading " + dynamicContent.getContentDescription());

        DebugInfo debugInfo = preLoadContent(dynamicContent);

        dynamicContent.checkDisposed();
        ConnectionHandler connectionHandler = dynamicContent.getConnectionHandler();
        LoaderCache loaderCache = new LoaderCache();
        int count = 0;
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
                if (addDelay) Thread.sleep(500);
                while (resultSet != null && resultSet.next()) {
                    if (addDelay) Thread.sleep(10);
                    dynamicContent.checkDisposed();

                    T element = null;
                    try {
                        element = createElement(dynamicContent, resultSet, loaderCache);
                    } catch (ProcessCanceledException e){
                        return;
                    } catch (RuntimeException e) {
                        System.out.println("RuntimeException: " + e.getMessage());
                    } catch (SkipEntrySQLException e) {
                        continue;
                    }

                    dynamicContent.checkDisposed();
                    if (element != null) {
                        if (list == null) {
                            list = dynamicContent.is(DynamicContentStatus.CONCURRENT) ?
                                    CollectionUtil.createConcurrentList() :
                                    new ArrayList<T>();
                        }
                        list.add(element);
                        if (count%10 == 0) {
                            String description = element.getDescription();
                            if (description != null)
                                ProgressMonitor.setSubtaskDescription(description);
                        }
                        count++;
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
        } catch (Exception e) {
            if (e instanceof InterruptedException) throw (InterruptedException) e;
            if (e instanceof ProcessCanceledException) throw (ProcessCanceledException) e;
            if (e == DatabaseInterface.DBN_NOT_CONNECTED_EXCEPTION) throw new InterruptedException();

            String message = StringUtil.trim(e.getMessage()).replace("\n", " ");
            LOGGER.warn("Error loading database content (" + dynamicContent.getContentDescription() + "): " + message);

            boolean modelException = false;
            if (e instanceof SQLException) {
                SQLException sqlException = (SQLException) e;
                if (Failsafe.check(dynamicContent)) {
                    DatabaseInterfaceProvider interfaceProvider = dynamicContent.getConnectionHandler().getInterfaceProvider();
                    modelException = interfaceProvider.getMessageParserInterface().isModelException(sqlException);
                }
            }
            throw new DynamicContentLoadException(e, modelException);
        } finally {
            loading.set(false);
        }
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
