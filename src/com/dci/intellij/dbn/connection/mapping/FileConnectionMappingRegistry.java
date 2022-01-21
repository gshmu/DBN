package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.file.util.VirtualFileUtil;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.progress.ProcessCanceledException;
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

import static com.dci.intellij.dbn.common.action.UserDataKeys.*;

@Getter
public class FileConnectionMappingRegistry extends StatefulDisposable.Base {
    private final ProjectRef project;
    private final Map<String, FileConnectionMapping> mappings = new ConcurrentHashMap<>();

    public FileConnectionMappingRegistry(Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public boolean setConnectionHandler(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        ConnectionHandlerRef connectionHandlerRef = ConnectionHandlerRef.of(connectionHandler);

        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandlerRef);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(CONNECTION_HANDLER, connectionHandlerRef);

            ConnectionId connectionId = connectionHandler == null ? null : connectionHandler.getConnectionId();
            SchemaId currentSchema = connectionHandler == null ? null  : connectionHandler.getUserSchema();

            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping == null) {
                String fileUrl = virtualFile.getUrl();
                mapping = new FileConnectionMapping(fileUrl, connectionId, SessionId.MAIN, currentSchema);
                mappings.put(fileUrl, mapping);
                return true;
            } else {
                if (mapping.getConnectionId() == null || mapping.getConnectionId() != connectionId) {
                    mapping.setConnectionId(connectionId);
                    mapping.setSessionId(SessionId.MAIN);
                    if (connectionHandler != null) {
                        // overwrite current schema only if the existing
                        // selection is not a valid schema for the given connection
                        currentSchema = mapping.getSchemaId();
                        setDatabaseSchema(virtualFile, currentSchema);
                    } else {
                        setDatabaseSchema(virtualFile, null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setDatabaseSchema(VirtualFile virtualFile, SchemaId schemaId) {
        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(DATABASE_SCHEMA, schemaId);
            return true;
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setDatabaseSchema(schemaId);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(DATABASE_SCHEMA, schemaId);
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping != null) {
                if (schemaId == null) {
                    mapping.setSchemaId(null);
                } else if (!schemaId.equals(mapping.getSchemaId())){
                    mapping.setSchemaId(schemaId);
                }

                return true;
            }
        }

        return false;
    }

    public boolean setDatabaseSession(VirtualFile virtualFile, DatabaseSession session) {
        if (virtualFile instanceof LightVirtualFile) {
            virtualFile.putUserData(DATABASE_SESSION, session);
            return true;
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(DATABASE_SESSION, session);
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping != null) {
                if (session == null) {
                    mapping.setSessionId(null);
                } else if (session.getId() != mapping.getSessionId()){
                    mapping.setSessionId(session.getId());
                }

                return true;
            }
        }

        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile sqlConsoleFile = (DBConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setDatabaseSession(session);
            return true;
        }

        return false;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler(@NotNull VirtualFile virtualFile) {
        try {
            Project project = getProject();
            if (virtualFile instanceof LightVirtualFile) {
                ConnectionHandlerRef connectionHandlerRef = virtualFile.getUserData(CONNECTION_HANDLER);
                if (connectionHandlerRef == null) {
                    LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                    VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                    if (originalFile != null && !originalFile.equals(virtualFile)) {
                        return getConnectionHandler(originalFile);
                    }
                }
                return ConnectionHandlerRef.get(connectionHandlerRef);
            }

            // if the file is a database content file then get the connection from the underlying database object
            if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
                if (virtualFile instanceof FileConnectionMappingProvider) {
                    FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                    return connectionMappingProvider.getConnectionHandler();
                }
            }

            if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
                // if the file is an attached ddl file, then resolve the object which it is
                // linked to, and return its database connection
                DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
                if (schemaObject != null) {
                    ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                    if (DatabaseFileSystem.isFileOpened(schemaObject)) {
                        return connectionHandler;
                    }
                }

                // lookup connection mappings
                ConnectionHandlerRef connectionRef = virtualFile.getUserData(CONNECTION_HANDLER);
                if (connectionRef == null) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                        ConnectionHandler connectionHandler = connectionManager.getConnection(mapping.getConnectionId());
                        connectionRef = ConnectionHandlerRef.of(connectionHandler);
                        virtualFile.putUserData(CONNECTION_HANDLER, connectionRef);

                    }
                }
                return ConnectionHandlerRef.get(connectionRef);
            }
        } catch (ProcessCanceledException ignore) {}

        return null;
    }

    @Nullable
    public SchemaId getDatabaseSchema(@NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            SchemaId schemaId = virtualFile.getUserData(DATABASE_SCHEMA);
            if (schemaId == null) {
                LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                if (originalFile != null && !originalFile.equals(virtualFile)) {
                    return getDatabaseSchema(originalFile);
                }
            }
            return schemaId;
        }

        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getSchemaId();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            Project project = getProject();
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
            DBSchemaObject schemaObject = fileAttachmentManager.getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.isFileOpened(schemaObject)) {
                return schemaObject.getSchemaIdentifier();
            }

            // lookup schema mappings
            SchemaId currentSchema = virtualFile.getUserData(DATABASE_SCHEMA);
            if (currentSchema == null) {
                ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        currentSchema = mapping.getSchemaId();
                        if (currentSchema == null) {
                            currentSchema = connectionHandler.getDefaultSchema();
                        }
                        mapping.setSchemaId(currentSchema);
                        virtualFile.putUserData(DATABASE_SCHEMA, currentSchema);
                    }
                }
            } else {
                return currentSchema;
            }
        }
        return null;
    }

    @Nullable
    public DatabaseSession getDatabaseSession(@NotNull VirtualFile virtualFile) {
        if (virtualFile instanceof LightVirtualFile) {
            DatabaseSession databaseSession = virtualFile.getUserData(DATABASE_SESSION);
            if (databaseSession == null) {
                LightVirtualFile lightVirtualFile = (LightVirtualFile) virtualFile;
                VirtualFile originalFile = lightVirtualFile.getOriginalFile();
                if (originalFile != null && !originalFile.equals(virtualFile)) {
                    return getDatabaseSession(originalFile);
                }
            }
            return databaseSession;
        }

        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof FileConnectionMappingProvider) {
                FileConnectionMappingProvider connectionMappingProvider = (FileConnectionMappingProvider) virtualFile;
                return connectionMappingProvider.getDatabaseSession();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // lookup schema mappings
            DatabaseSession databaseSession = virtualFile.getUserData(DATABASE_SESSION);
            if (databaseSession == null) {
                ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        SessionId sessionId = mapping.getSessionId();
                        databaseSession = connectionHandler.getSessionBundle().getSession(sessionId);
                        virtualFile.putUserData(DATABASE_SESSION, databaseSession);
                    }
                }
            }
            return databaseSession;
        }
        return null;
    }


    @Nullable
    public DBNConnection getConnection(@NotNull VirtualFile virtualFile) {
        ConnectionHandler connectionHandler = getConnectionHandler(virtualFile);
        if (connectionHandler != null) {
            DatabaseSession databaseSession = getDatabaseSession(virtualFile);
            if (databaseSession != null) {
                return connectionHandler.getConnectionPool().getSessionConnection(databaseSession.getId());
            }
        }
        return null;
    }

    private FileConnectionMapping lookupMapping(VirtualFile virtualFile) {
        return mappings.get(virtualFile.getUrl());
    }

    public void removeMapping(VirtualFile virtualFile) {
        mappings.remove(virtualFile.getUrl());
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connectionHandler) {
        List<VirtualFile> list = new ArrayList<>();

        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        for (FileConnectionMapping mapping : mappings.values()) {
            ConnectionId connectionId = mapping.getConnectionId();
            if (connectionHandler.getConnectionId() == connectionId) {
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
