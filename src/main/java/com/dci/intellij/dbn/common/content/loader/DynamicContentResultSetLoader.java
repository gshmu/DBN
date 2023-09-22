package com.dci.intellij.dbn.common.content.loader;

import com.dci.intellij.dbn.common.Priority;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentElement;
import com.dci.intellij.dbn.common.content.DynamicContentProperty;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.exception.ElementSkippedException;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Resources;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNResultSet;
import com.dci.intellij.dbn.connection.jdbc.IncrementalStatusAdapter;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataFactory;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceInvoker;
import com.dci.intellij.dbn.database.interfaces.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.dci.intellij.dbn.diagnostics.DiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
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
import java.util.function.Supplier;

import static com.dci.intellij.dbn.common.content.DynamicContentProperty.INTERNAL;
import static com.dci.intellij.dbn.common.exception.Exceptions.toSqlException;
import static com.dci.intellij.dbn.common.exception.Exceptions.toSqlTimeoutException;
import static com.dci.intellij.dbn.common.util.TimeUtil.millisSince;
import static com.dci.intellij.dbn.common.util.Unsafe.cast;
import static com.dci.intellij.dbn.connection.Resources.markClosed;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;
import static com.dci.intellij.dbn.diagnostics.Diagnostics.isDatabaseAccessDebug;
import static com.dci.intellij.dbn.diagnostics.data.Activity.LOAD;
import static com.dci.intellij.dbn.diagnostics.data.Activity.QUERY;

@Slf4j
public abstract class DynamicContentResultSetLoader<E extends DynamicContentElement, M extends DBObjectMetadata>
        extends DynamicContentLoaderImpl<E, M>
        implements DynamicContentLoader<E, M> {

    private final boolean master;

    private DynamicContentResultSetLoader(
            String identifier,
            @Nullable DynamicContentType<?> parentContentType,
            @NotNull DynamicContentType<?> contentType,
            boolean register,
            boolean master) {

        super(identifier, parentContentType, contentType, register);
        this.master = master;
    }

    public static <E extends DynamicContentElement, M extends DBObjectMetadata> DynamicContentLoader<E, M> create(
            @NotNull String identifier,
            @Nullable DynamicContentType<?> parentContentType,
            @NotNull DynamicContentType<?> contentType,
            boolean register,
            boolean master,
            ResultSetFactory resultSetFactory,
            ElementFactory<E, M> elementFactory) {
        return new DynamicContentResultSetLoader<E, M>(identifier, parentContentType, contentType, register, master) {
            @Override
            public ResultSet createResultSet(DynamicContent dynamicContent, DBNConnection connection) throws SQLException {
                return resultSetFactory.create(dynamicContent, connection, dynamicContent.getMetadataInterface());
            }

            @Override
            public E createElement(DynamicContent<E> content, M metadata, LoaderCache cache) throws SQLException {
                return elementFactory.create(content, cache, metadata);
            }
        };
    }

    @FunctionalInterface
    public interface ResultSetFactory {
        ResultSet create(DynamicContent dynamicContent, DBNConnection conn, DatabaseMetadataInterface mdi) throws SQLException;
    }

    @FunctionalInterface
    public interface ElementFactory<E extends DynamicContentElement, M extends DBObjectMetadata> {
        E create(DynamicContent content, LoaderCache cache, M md) throws SQLException;
    }


    public abstract ResultSet createResultSet(DynamicContent<E> dynamicContent, DBNConnection connection) throws SQLException;
    public abstract E createElement(DynamicContent<E> content, M metadata, LoaderCache cache) throws SQLException;

    private static class DebugInfo {
        private final String id = UUID.randomUUID().toString();
        private final long startTimestamp = System.currentTimeMillis();
    }

    private DebugInfo preLoadContent(DynamicContent<E> content) {
        if (isDatabaseAccessDebug()) {
            DebugInfo debugInfo = new DebugInfo();
            log.info("[DBN] Loading {} (id = {})",
                    content.getContentDescription(),
                    debugInfo.id);

            return debugInfo;
        }
        return null;
    }

    private void postLoadContent(DynamicContent<E> content, DebugInfo debugInfo) {
        if (debugInfo == null) return;

        log.info(
                "[DBN] Done loading {} (id = {}) - {}ms",
                content.getContentDescription(),
                debugInfo.id,
                (System.currentTimeMillis() - debugInfo.startTimestamp));
    }


    private void postLoadContentFailure(DynamicContent<E> content, DebugInfo debugInfo, Throwable exception) {
        if (debugInfo == null) return;
        log.warn("[DBN] Failed to load {} (id = {}) - {}ms",
                content.getContentDescription(),
                debugInfo.id,
                System.currentTimeMillis() - debugInfo.startTimestamp,
                exception);
    }

    @Override
    public void loadContent(DynamicContent<E> content) throws SQLException {
        Priority priority = content.is(INTERNAL) ? Priority.LOW : Priority.MEDIUM;
        DatabaseInterfaceInvoker.execute(priority,
                "Loading data dictionary",
                "Loading " + content.getContentDescription(),
                content.getProject(),
                content.getConnectionId(),
                conn -> loadContent(content, conn));
    }

    private void loadContent(DynamicContent<E> content, DBNConnection conn) throws SQLException {
        DebugInfo debugInfo = preLoadContent(content);
        ConnectionHandler connection = content.getConnection();
        DiagnosticsManager diagnosticsManager = DiagnosticsManager.getInstance(connection.getProject());
        DiagnosticBundle<String> diagnostics = diagnosticsManager.getMetadataInterfaceDiagnostics(connection.getConnectionId());
        IncrementalStatusAdapter loading = connection.getConnectionStatus().getLoading();
        try {
            loading.set(true);
            content.checkDisposed();
            ResultSet resultSet = null;
            List<E> list = null;
            try {
                content.checkDisposed();
                resultSet = createResultSet(content, conn);
                String identifier = DBNResultSet.getIdentifier(resultSet);

                DynamicContentType<?> contentType = content.getContentType();
                M metadata = DBObjectMetadataFactory.INSTANCE.create(contentType, resultSet);

                Diagnostics.databaseLag(QUERY);
                LoaderCache loaderCache = new LoaderCache();
                int count = 0;

                long loadStart = System.currentTimeMillis();
                while (resultSet != null && resultSet.next()) {
                    Diagnostics.databaseLag(LOAD);
                    content.checkDisposed();

                    E element = null;
                    try {
                        element = createElement(content, metadata, loaderCache);
                    } catch (ElementSkippedException e) {
                        conditionallyLog(e);
                    } catch (ProcessCanceledException e) {
                        conditionallyLog(e);
                        return;
                    } catch (SQLRecoverableException e) {
                        throw e;
                    } catch (Throwable e) {
                        conditionallyLog(e);
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

                diagnostics.log(identifier, "LOAD", false, false, millisSince(loadStart));
            } finally {
                Resources.close(resultSet);
            }
            content.checkDisposed();
            content.setElements(list);
            content.set(DynamicContentProperty.MASTER, master);

            postLoadContent(content, debugInfo);

        } catch (ProcessCanceledException e) {
            conditionallyLog(e);
            postLoadContentFailure(content, debugInfo, e);
            throw toSqlTimeoutException(e, "Load process cancelled");

        } catch (SQLTimeoutException |
                 SQLFeatureNotSupportedException |
                 SQLTransientConnectionException |
                 SQLNonTransientConnectionException e) {
            conditionallyLog(e);
            postLoadContentFailure(content, debugInfo, e);
            throw e;

        } catch (SQLRecoverableException e) {
            conditionallyLog(e);
            markClosed(conn);
            throw e;
        } catch (SQLException e) {
            conditionallyLog(e);
            postLoadContentFailure(content, debugInfo, e);

            DatabaseMessageParserInterface messageParserInterface = connection.getMessageParserInterface();
            boolean modelException = messageParserInterface.isModelException(e);
            throw modelException ? new SQLFeatureNotSupportedException(e) : e;

        } catch (Throwable e) {
            conditionallyLog(e);
            postLoadContentFailure(content, debugInfo, e);
            throw toSqlException(e);

        } finally {
            loading.set(false);
        }
    }

    public static class LoaderCache {
        private String key;
        private DBObject object;
        public <T extends DBObject> T get(String name) {
            if (Objects.equals(this.key, name)) {
                return cast(object);
            }
            return null;
        }

        public <T extends DBObject> T get(String name, Supplier<DBObject> loader) {
            if (!Objects.equals(this.key, name)) {
                this.key = name;
                this.object = loader.get();
            }
            return cast(object);
        }

        public void set(String key, DBObject object) {
            this.key = key;
            this.object = object;
        }
    }
}
