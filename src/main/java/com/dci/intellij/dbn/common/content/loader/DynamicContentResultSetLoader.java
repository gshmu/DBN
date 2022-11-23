package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataFactory;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskDefinition;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.dci.intellij.dbn.common.Priority.LOW;
import static com.dci.intellij.dbn.common.Priority.MEDIUM;
import static com.dci.intellij.dbn.common.content.DynamicContentProperty.INTERNAL;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.isDatabaseAccessDebug;

@Slf4j
public abstract class DynamicContentResultSetLoader<
                T extends DynamicContentElement,
                M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<T, M>
        implements DynamicContentLoader<T, M> {

    private final boolean master;

    public DynamicContentResultSetLoader(
            @Nullable DynamicContentType<?> parentContentType,
            @NotNull DynamicContentType<?> contentType,
            boolean register,
            boolean master) {

        super(parentContentType, contentType, register);
        this.master = master;
    }

    public abstract ResultSet createResultSet(DynamicContent<T> dynamicContent, DBNConnection connection) throws SQLException;
    public abstract T createElement(DynamicContent<T> content, M metadata, LoaderCache cache) throws SQLException;

    private static class DebugInfo {
        private final String id = UUID.randomUUID().toString();
        private final long startTimestamp = System.currentTimeMillis();
    }

    private DebugInfo preLoadContent(DynamicContent<T> dynamicContent) {
        if (isDatabaseAccessDebug()) {
            DebugInfo debugInfo = new DebugInfo();
            log.info(
                    "[DBN] Loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ")");
            return debugInfo;
        }
        return null;
    }

    private void postLoadContent(DynamicContent<T> dynamicContent, DebugInfo debugInfo) {
        if (debugInfo != null) {
            log.info(
                    "[DBN] Done loading " + dynamicContent.getContentDescription() +
                    " (id = " + debugInfo.id + ") - " +
                    (System.currentTimeMillis() - debugInfo.startTimestamp) + "ms"   );
        }
    }

    @Override
    public void loadContent(DynamicContent<T> content) throws SQLException {
        InterfaceTaskDefinition taskDefinition = InterfaceTaskDefinition.create(
                content.is(INTERNAL) ? LOW : MEDIUM,
                "Loading data dictionary",
                "Loading " + content.getContentDescription(),
                content.createInterfaceContext());

        DatabaseInterfaceInvoker.execute(taskDefinition, conn -> {
            DebugInfo debugInfo = preLoadContent(content);
            ConnectionHandler connection = content.getConnection();
            IncrementalStatusAdapter loading = connection.getConnectionStatus().getLoading();
            try {
                loading.set(true);
                content.checkDisposed();
                ResultSet resultSet = null;
                List<T> list = null;
                try {
                    content.checkDisposed();
                    resultSet = createResultSet(content, conn);

                    DynamicContentType<?> contentType = content.getContentType();
                    M metadata = DBObjectMetadataFactory.INSTANCE.create(contentType, resultSet);

                    Diagnostics.introduceDatabaseLag(Diagnostics.getQueryingLag());
                    LoaderCache loaderCache = new LoaderCache();
                    int count = 0;

                    while (resultSet != null && resultSet.next()) {
                        Diagnostics.introduceDatabaseLag(Diagnostics.getFetchingLag());
                        content.checkDisposed();

                        T element = null;
                        try {
                            element = createElement(content, metadata, loaderCache);
                        } catch (ProcessCanceledException e) {
                            return;
                        } catch (RuntimeException e) {
                            log.warn("Failed to create element", e);
                        }

                        content.checkDisposed();
                        if (element == null) continue;

                        if (list == null) list = new ArrayList<>();
                        list.add(element);

                        if (count % 10 == 0) {
                            String description = element.getDescription();
                            if (description != null)
                                ProgressMonitor.setProgressDetail(description);
                        }
                        count++;
                    }
                } finally {
                    Resources.close(resultSet);
                }
                content.checkDisposed();
                content.setElements(list);
                content.set(DynamicContentProperty.MASTER, master);

                postLoadContent(content, debugInfo);

            } catch (ProcessCanceledException e) {
                throw new SQLTimeoutException(e);

            } catch (SQLTimeoutException |
                     SQLFeatureNotSupportedException |
                     SQLTransientConnectionException |
                     SQLNonTransientConnectionException e) {
                throw e;

            } catch (SQLException e) {
                DatabaseMessageParserInterface messageParserInterface = connection.getMessageParserInterface();
                boolean modelException = messageParserInterface.isModelException(e);
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

    public static class LoaderCache {
        private String name;
        private DBObject object;
        public DBObject getObject(String name) {
            if (Objects.equals(name, this.name)) {
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
