package com.dci.intellij.dbn.connection.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ProjectConnectionBundle;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.language.editor.ui.DBLanguageFileEditorToolbarForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseContentVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.SQLConsoleVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import gnu.trove.THashSet;

@State(
    name = "DBNavigator.Project.FileConnectionMappingManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/misc.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class FileConnectionMappingManager extends VirtualFileAdapter implements ProjectComponent, PersistentStateComponent<Element> {
    private Project project;
    private Set<FileConnectionMapping> mappings = new THashSet<FileConnectionMapping>();
    private Key<ConnectionHandler> ACTIVE_CONNECTION_KEY = Key.create("DBNavigator.ActiveConnection");
    private Key<DBObjectRef<DBSchema>> CURRENT_SCHEMA_KEY = Key.create("DBNavigator.CurrentSchema");

    public boolean setActiveConnection(VirtualFile virtualFile, ConnectionHandler connectionHandler) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            virtualFile.putUserData(ACTIVE_CONNECTION_KEY, connectionHandler);

            String connectionId = connectionHandler == null ? null : connectionHandler.getId();
            String currentSchema = connectionHandler == null ? null  : connectionHandler.getUserName().toUpperCase();

            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (mapping == null) {
                mapping = new FileConnectionMapping(virtualFile.getUrl(), connectionId, currentSchema);
                mappings.add(mapping);
                return true;
            } else {
                if (mapping.getConnectionId() == null || !mapping.getConnectionId().equals(connectionId)) {
                    mapping.setConnectionId(connectionId);
                    if (connectionHandler != null) {
                        // overwrite current schema only if the existing
                        // selection is not a valid schema for the given connection
                        String currentSchemaName = mapping.getCurrentSchema();
                        DBSchema schema = connectionHandler.isVirtual() || currentSchemaName == null ? null : connectionHandler.getObjectBundle().getSchema(currentSchemaName);
                        setCurrentSchema(virtualFile, schema);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setCurrentSchema(VirtualFile virtualFile, DBSchema schema) {
        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            virtualFile.putUserData(CURRENT_SCHEMA_KEY, schema == null ? null : schema.getRef());
            FileConnectionMapping mapping = lookupMapping(virtualFile);
            if (schema != null && mapping != null && !schema.getName().equals(mapping.getCurrentSchema())) {
                mapping.setCurrentSchema(schema.getName());
                return true;
            }
        }

        if (virtualFile instanceof SQLConsoleVirtualFile) {
            SQLConsoleVirtualFile sqlConsoleFile = (SQLConsoleVirtualFile) virtualFile;
            sqlConsoleFile.setCurrentSchema(schema);
            return true;
        }
        return false;
    }

    @Nullable
    public ConnectionHandler getActiveConnection(VirtualFile virtualFile) {
        // if the file is a database content file then get the connection from the underlying database object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof DatabaseContentVirtualFile) {
                DatabaseContentVirtualFile contentFile = (DatabaseContentVirtualFile) virtualFile;
                return contentFile.getActiveConnection();
            }
            if (virtualFile instanceof DatabaseEditableObjectVirtualFile) {
                DatabaseEditableObjectVirtualFile databaseFile = (DatabaseEditableObjectVirtualFile) virtualFile;
                return databaseFile.getActiveConnection();
            }

            if (virtualFile instanceof SQLConsoleVirtualFile) {
                SQLConsoleVirtualFile sqlConsoleFile = (SQLConsoleVirtualFile) virtualFile;
                return sqlConsoleFile.getConnectionHandler();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its database connection
            DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.getInstance().isFileOpened(schemaObject)) {
                return schemaObject.getConnectionHandler();
            }

            // lookup connection mappings
            ConnectionHandler connectionHandler = virtualFile.getUserData(ACTIVE_CONNECTION_KEY);
            if (connectionHandler == null) {
                FileConnectionMapping mapping = lookupMapping(virtualFile);
                if (mapping != null) {
                    ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                    connectionHandler = connectionManager.getConnectionHandler(mapping.getConnectionId());
                    if (connectionHandler == null) connectionHandler =
                            ProjectConnectionBundle.getInstance(project).getVirtualConnection(mapping.getConnectionId());

                    if (connectionHandler != null)
                        virtualFile.putUserData(ACTIVE_CONNECTION_KEY, connectionHandler);
                }
            }
            return connectionHandler;
        }

        return null;
    }

    public DBSchema getCurrentSchema(VirtualFile virtualFile) {
        // if the file is a database content file then get the schema from the underlying schema object
        if (VirtualFileUtil.isDatabaseFileSystem(virtualFile)) {
            if (virtualFile instanceof DatabaseContentVirtualFile) {
                DatabaseContentVirtualFile contentFile = (DatabaseContentVirtualFile) virtualFile;
                return contentFile.getCurrentSchema();
            }

            if (virtualFile instanceof SQLConsoleVirtualFile) {
                SQLConsoleVirtualFile sqlConsoleFile = (SQLConsoleVirtualFile) virtualFile;
                return sqlConsoleFile.getCurrentSchema();
            }
        }

        if (VirtualFileUtil.isLocalFileSystem(virtualFile) || VirtualFileUtil.isVirtualFileSystem(virtualFile)) {
            // if the file is an attached ddl file, then resolve the object which it is
            // linked to, and return its parent schema
            DBSchemaObject schemaObject = DDLFileAttachmentManager.getInstance(project).getEditableObject(virtualFile);
            if (schemaObject != null && DatabaseFileSystem.getInstance().isFileOpened(schemaObject)) {
                return schemaObject.getSchema();
            }

            // lookup schema mappings
            DBObjectRef<DBSchema> currentSchemaRef = virtualFile.getUserData(CURRENT_SCHEMA_KEY);
            if (currentSchemaRef == null) {
                ConnectionHandler connectionHandler = getActiveConnection(virtualFile);
                if (connectionHandler != null && !connectionHandler.isVirtual()) {
                    FileConnectionMapping mapping = lookupMapping(virtualFile);
                    if (mapping != null) {
                        String schemaName = mapping.getCurrentSchema();
                        if (StringUtil.isEmptyOrSpaces(schemaName)) {
                            DBSchema userSchema = connectionHandler.getUserSchema();
                            currentSchemaRef = userSchema == null ? null : userSchema.getRef();
                            schemaName = currentSchemaRef == null ? null : currentSchemaRef.getName();
                        } else {
                            DBSchema schema = connectionHandler.getObjectBundle().getSchema(schemaName);
                            currentSchemaRef = schema == null ? null : schema.getRef();
                        }
                        mapping.setCurrentSchema(schemaName);
                        virtualFile.putUserData(CURRENT_SCHEMA_KEY, currentSchemaRef);
                    }
                }
            } else {
                return currentSchemaRef.get();
            }
        }
        return null;
    }


    private FileConnectionMapping lookupMapping(VirtualFile virtualFile) {
        String fileUrl = virtualFile.getUrl();
        return lookupMapping(fileUrl);
    }

    private FileConnectionMapping lookupMapping(String fileUrl) {
        for (FileConnectionMapping mapping : mappings) {
            if (fileUrl.equals(mapping.getFileUrl())) {
                return mapping;
            }
        }
        return null;
    }

    public void removeMapping(VirtualFile virtualFile) {
        FileConnectionMapping mapping = lookupMapping(virtualFile);
        if (mapping != null) {
            mappings.remove(mapping);
        }
    }

    public List<VirtualFile> getMappedFiles(ConnectionHandler connectionHandler) {
        List<VirtualFile> list = new ArrayList<VirtualFile>();

        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        for (FileConnectionMapping mapping : mappings) {
            String connectionId = mapping.getConnectionId();
            if (connectionHandler.getId().equals(connectionId)) {
                VirtualFile file = virtualFileManager.findFileByUrl(mapping.getFileUrl());
                if (file != null) {
                    list.add(file);
                }
            }
        }
        return list;
    }

    public void selectActiveConnectionForEditor(Editor editor, @Nullable ConnectionHandler connectionHandler) {
        if (editor!= null) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (VirtualFileUtil.isLocalFileSystem(virtualFile) ) {
                boolean changed = setActiveConnection(virtualFile, connectionHandler);
                if (changed) {
                    DocumentUtil.touchDocument(editor);

                    FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(virtualFile);
                    DBLanguageFileEditorToolbarForm toolbarForm = fileEditor.getUserData(DBLanguageFileEditorToolbarForm.USER_DATA_KEY);
                    if (toolbarForm != null) {
                        toolbarForm.getAutoCommitLabel().setConnectionHandler(connectionHandler);
                    }
                }
            }
        }
    }

    public void setCurrentSchemaForSelectedEditor(Editor editor, DBSchema schema) {
        if (editor!= null) {
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (VirtualFileUtil.isLocalFileSystem(virtualFile) || virtualFile instanceof SQLConsoleVirtualFile) {
                boolean changed = setCurrentSchema(virtualFile, schema);
                if (changed) {
                    DocumentUtil.touchDocument(editor);
                }
            }
        }
    }

    /***************************************
     *         VirtualFileListener         *
     ***************************************/

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        removeMapping(event.getFile());
    }

    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
        String oldFileUrl = event.getOldParent().getUrl() + "/" + event.getFileName();
        FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
        if (fileConnectionMapping != null) {
            fileConnectionMapping.setFileUrl(event.getFile().getUrl());
        }
    }

    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        VirtualFile file = event.getFile();
        VirtualFile parent = file.getParent();
        if (file.isInLocalFileSystem() && parent != null) {
            String oldFileUrl = parent.getUrl() + "/" + event.getOldValue();
            FileConnectionMapping fileConnectionMapping = lookupMapping(oldFileUrl);
            if (fileConnectionMapping != null) {
                fileConnectionMapping.setFileUrl(file.getUrl());
            }
        }
    }

    /***************************************
     *          ProjectComponent         *
     ***************************************/
    private FileConnectionMappingManager(Project project) {
        this.project = project;
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    public static FileConnectionMappingManager getInstance(Project project) {
        return project.getComponent(FileConnectionMappingManager.class);
    }

    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.FileConnectionMappingManager";
    }

    public void projectOpened() {}
    public void projectClosed() {}
    public void initComponent() {}
    public void disposeComponent() {
        mappings.clear();
        project = null;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (FileConnectionMapping mapping : mappings) {
            Element mappingElement = new Element("mapping");
            mapping.writeState(mappingElement);
            element.addContent(mappingElement);
        }
        return element;
    }

    @Override
    public void loadState(Element element) {
        if (element != null) {
            for (Object child : element.getChildren()) {
                Element mappingElement = (Element) child;
                FileConnectionMapping mapping = new FileConnectionMapping();
                mapping.readState(mappingElement);
                mappings.add(mapping);
            }
        }
    }
}

