package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.file.util.VirtualFiles;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.context.ConnectionContextProvider;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.dci.intellij.dbn.common.action.UserDataKeys.FILE_CONNECTION_MAPPING;
import static com.dci.intellij.dbn.common.util.Commons.coalesce;

@Getter
public class FileConnectionContextRegistry extends StatefulDisposable.Base {
    private final ProjectRef project;
    private final Map<String, FileConnectionContext> mappings = new ConcurrentHashMap<>();

    public FileConnectionContextRegistry(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public boolean setConnectionHandler(@NotNull VirtualFile file, @Nullable ConnectionHandler connection) {
        if (VirtualFiles.isDatabaseFileSystem(file)) {
            return false;
        }

        FileConnectionContext mapping = ensureFileConnectionMapping(file);
        boolean changed = mapping.setConnectionId(connection == null ? null : connection.getConnectionId());

        if (changed) {
            if (connection == null || connection.isVirtual()) {
                setDatabaseSession(file, null);
                setDatabaseSchema(file, null);
            } else {
                // restore session if available in new connection
                SessionId sessionId = mapping.getSessionId();
                boolean match = connection.getSessionBundle().hasSession(sessionId);
                sessionId = match ? sessionId : SessionId.MAIN;
                mapping.setSessionId(sessionId);

                // restore schema if available in new connection
                SchemaId schemaId = mapping.getSchemaId();
                DBSchema schema = schemaId == null ? null : connection.getSchema(schemaId);
                if (schema == null) {
                    schemaId = connection.getDefaultSchema();
                }
                mapping.setSchemaId(schemaId);
            }
        }

        return changed;
    }

    public boolean setDatabaseSchema(VirtualFile file, SchemaId schemaId) {
        FileConnectionContext mapping = ensureFileConnectionMapping(file);
        return mapping.setSchemaId(schemaId);
    }

    public boolean setDatabaseSession(VirtualFile file, DatabaseSession session) {
        FileConnectionContext mapping = ensureFileConnectionMapping(file);
        return mapping.setSessionId(session == null ? null : session.getId());
    }

    @Nullable
    public ConnectionHandler getDatabaseConnection(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveDdlAttachment(underlyingFile,   mapping -> mapping.getConnection()),
                () -> resolveMappingProvider(underlyingFile, mapping -> mapping.getConnection()),
                () -> resolveFileMapping(underlyingFile,     mapping -> mapping.getConnection()));
    }

    @Nullable
    public SchemaId getDatabaseSchema(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveDdlAttachment(underlyingFile,   mapping -> mapping.getSchemaId()),
                () -> resolveMappingProvider(underlyingFile, mapping -> mapping.getSchemaId()),
                () -> resolveFileMapping(underlyingFile,     mapping -> mapping.getSchemaId()));
    }

    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile file) {
        VirtualFile underlyingFile = VirtualFiles.getUnderlyingFile(file);
        return coalesce(
                () -> resolveMappingProvider(underlyingFile, mapping -> mapping.getSession()),
                () -> resolveFileMapping(underlyingFile,     mapping -> mapping.getSession()));
    }

    @Nullable
    private <T> T resolveMappingProvider(@NotNull VirtualFile file, Function<ConnectionContextProvider, T> handler) {
        if (VirtualFiles.isDatabaseFileSystem(file)) {
            if (file instanceof ConnectionContextProvider) {
                ConnectionContextProvider mappingProvider = (ConnectionContextProvider) file;
                return handler.apply(mappingProvider);
            }
        }
        return null;
    }

    @Nullable
    private <T> T resolveFileMapping(@NotNull VirtualFile file, Function<FileConnectionContext, T> handler) {
        FileConnectionContext connectionMapping = getFileConnectionMapping(file);
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
        return getFileConnectionMapping(file, true);
    }

    @Nullable
    public FileConnectionContext getFileConnectionMapping(VirtualFile file) {
        return getFileConnectionMapping(file, false);
    }

    private FileConnectionContext getFileConnectionMapping(VirtualFile file, boolean ensure) {
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

        FileConnectionContext mapping = null;
        if (file instanceof LightVirtualFile) {
            mapping = file.getUserData(FILE_CONNECTION_MAPPING);

            if (mapping == null && ensure) {
                mapping = new FileConnectionContextImpl(file);
                file.putUserData(FILE_CONNECTION_MAPPING, mapping);
            }
            return mapping;
        }

        if (VirtualFiles.isLocalFileSystem(file)) {
            mapping = file.getUserData(FILE_CONNECTION_MAPPING);
            if (mapping == null) {
                mapping = mappings.get(file.getUrl());

                if (mapping == null && ensure) {
                    mapping = new FileConnectionContextImpl(file);
                    mappings.put(file.getUrl(), mapping);
                }

                if (mapping != null) {
                    file.putUserData(FILE_CONNECTION_MAPPING, mapping);
                }
            }
            if (mapping == null) {
                VirtualFile parent = file.getParent();
                if (parent != null) {
                    return getFileConnectionMapping(parent);
                }

            }
        }

        return mapping;
    }

    public boolean removeMapping(VirtualFile file) {
        FileConnectionContext mapping = mappings.remove(file.getUrl());
        FileConnectionContext localMapping = file.getUserData(FILE_CONNECTION_MAPPING);
        file.putUserData(FILE_CONNECTION_MAPPING, null);

        return mapping != null || localMapping != null;
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connection) {
        List<VirtualFile> list = new ArrayList<>();

        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        for (FileConnectionContext mapping : mappings.values()) {
            ConnectionId connectionId = mapping.getConnectionId();
            if (connection.getConnectionId() == connectionId) {
                VirtualFile file = localFileSystem.findFileByPath(mapping.getFileUrl());
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
