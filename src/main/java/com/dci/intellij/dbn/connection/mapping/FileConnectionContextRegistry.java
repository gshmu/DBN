package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.file.util.VirtualFiles;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListener;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.dci.intellij.dbn.common.action.UserDataKeys.FILE_CONNECTION_MAPPING;
import static com.dci.intellij.dbn.common.util.Commons.coalesce;

@Getter
public class FileConnectionContextRegistry extends StatefulDisposableBase implements ConnectionConfigListener {
    private final ProjectRef project;
    private final Map<String, FileConnectionContext> mappings = new ConcurrentHashMap<>();

    public FileConnectionContextRegistry(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    public void connectionRemoved(ConnectionId connectionId) {

    }

    public boolean setConnectionHandler(@NotNull VirtualFile file, @Nullable ConnectionHandler connection) {
        if (VirtualFiles.isDatabaseFileSystem(file)) {
            return false;
        }

        FileConnectionContext context = ensureFileConnectionMapping(file);
        boolean changed = context.setConnectionId(connection == null ? null : connection.getConnectionId());

        if (changed) {
            if (connection == null || connection.isVirtual()) {
                setDatabaseSession(file, null);
                setDatabaseSchema(file, null);
            } else {
                // restore session if available in new connection
                SessionId sessionId = context.getSessionId();
                boolean match = connection.getSessionBundle().hasSession(sessionId);
                sessionId = match ? sessionId : SessionId.MAIN;
                context.setSessionId(sessionId);

                // restore schema if available in new connection
                SchemaId schemaId = context.getSchemaId();
                DBSchema schema = schemaId == null ? null : connection.getSchema(schemaId);
                if (schema == null) {
                    schemaId = connection.getDefaultSchema();
                }
                context.setSchemaId(schemaId);
            }
        }

        return changed;
    }

    public boolean setDatabaseSchema(VirtualFile file, SchemaId schemaId) {
        FileConnectionContext context = ensureFileConnectionMapping(file);
        return context.setSchemaId(schemaId);
    }

    public boolean setDatabaseSession(VirtualFile file, DatabaseSession session) {
        FileConnectionContext context = ensureFileConnectionMapping(file);
        return context.setSessionId(session == null ? null : session.getId());
    }

    @Nullable
    public ConnectionHandler getDatabaseConnection(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveDdlAttachment(underlyingFile,   context -> context.getConnection()),
                () -> resolveMappingProvider(underlyingFile, context -> context.getConnection()),
                () -> resolveFileMapping(underlyingFile,     context -> context.getConnection()));
    }

    @Nullable
    public SchemaId getDatabaseSchema(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveDdlAttachment(underlyingFile,   context -> context.getSchemaId()),
                () -> resolveMappingProvider(underlyingFile, context -> context.getSchemaId()),
                () -> resolveFileMapping(underlyingFile,     context -> context.getSchemaId()));
    }

    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveMappingProvider(underlyingFile, context -> context.getSession()),
                () -> resolveFileMapping(underlyingFile,     context -> context.getSession()));
    }

    @Nullable
    private <T> T resolveMappingProvider(@NotNull VirtualFile file, Function<DBVirtualFile, T> handler) {
        if (VirtualFiles.isDatabaseFileSystem(file)) {
            if (file instanceof DBVirtualFile) {
                DBVirtualFile databaseFile = (DBVirtualFile) file;
                return handler.apply(databaseFile);
            }
        }
        return null;
    }

    @Nullable
    private <T> T resolveFileMapping(@NotNull VirtualFile file, Function<FileConnectionContext, T> handler) {
        FileConnectionContext connectionMapping = getFileConnectionContext(file);
        if (connectionMapping != null) {
            return handler.apply(connectionMapping);
        }
        return null;
    }

    @Nullable
    private <T> T resolveDdlAttachment(@NotNull VirtualFile file, Function<DBSchemaObject, T> handler) {
        if (VirtualFiles.isLocalFileSystem(file)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            Project project = getProject();
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
            DBSchemaObject schemaObject = fileAttachmentManager.getEditableObject(file);
            if (schemaObject != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                return handler.apply(schemaObject);
            }
        }
        return null;
    }

    @NotNull
    private FileConnectionContext ensureFileConnectionMapping(VirtualFile file) {
        return getFileConnectionContext(file, true);
    }

    @Nullable
    public FileConnectionContext getFileConnectionContext(VirtualFile file) {
        return getFileConnectionContext(file, false);
    }

    private FileConnectionContext getFileConnectionContext(VirtualFile file, boolean ensure) {
        file = VirtualFiles.getUnderlyingFile(file);

        if (file instanceof FileConnectionContextProvider) {
            FileConnectionContextProvider mappingProvider = (FileConnectionContextProvider) file;
            return mappingProvider.getConnectionContext();
        }

        if (VirtualFiles.isDatabaseFileSystem(file)) {
            if (ensure) {
                throw new UnsupportedOperationException();
            }
        }

        FileConnectionContext context = null;
        if (file instanceof LightVirtualFile) {
            context = file.getUserData(FILE_CONNECTION_MAPPING);

            if (context == null && ensure) {
                context = new FileConnectionContextImpl(file);
                file.putUserData(FILE_CONNECTION_MAPPING, context);
            }
            return context;
        }

        if (VirtualFiles.isLocalFileSystem(file)) {
            context = file.getUserData(FILE_CONNECTION_MAPPING);
            if (context == null) {
                context = mappings.get(file.getUrl());

                if (context == null && ensure) {
                    context = new FileConnectionContextImpl(file);
                    mappings.put(file.getUrl(), context);
                }

                if (context != null) {
                    file.putUserData(FILE_CONNECTION_MAPPING, context);
                }
            }
            if (context == null) {
                VirtualFile parent = file.getParent();
                if (parent != null) {
                    return getFileConnectionContext(parent);
                }

            }
        }

        return context;
    }

    public void cleanup() {
        Set<String> urls = mappings.keySet();
        for (String url : urls) {
            FileConnectionContext context = mappings.get(url);
            VirtualFile file = context.getFile();
            if (file == null) {
                mappings.remove(url);
                continue;
            }

            if (context.getConnectionId() == null) {
                mappings.remove(url);
                continue;
            }

            FileConnectionContext parentContext = getFileConnectionContext(file.getParent());
            if (parentContext != null && parentContext.isSameAs(context)) {
                mappings.remove(url);
                continue;
            }
        }
    }

    public boolean removeMapping(VirtualFile file) {
        FileConnectionContext context = mappings.remove(file.getUrl());
        FileConnectionContext localMapping = file.getUserData(FILE_CONNECTION_MAPPING);
        file.putUserData(FILE_CONNECTION_MAPPING, null);

        return context != null || localMapping != null;
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connection) {
        List<VirtualFile> list = new ArrayList<>();

        for (FileConnectionContext context : mappings.values()) {
            ConnectionId connectionId = context.getConnectionId();
            if (connection.getConnectionId() == connectionId) {
                VirtualFile file = context.getFile();
                if (file != null) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    @Override
    protected void disposeInner() {
        mappings.clear();
    }
}
