package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.ListUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.ddl.ui.AttachDDLFileDialog;
import com.dci.intellij.dbn.ddl.ui.DDLFileNameListCellRenderer;
import com.dci.intellij.dbn.ddl.ui.DetachDDLFileDialog;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.SelectFromListDialog;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

@State(
    name = DDLFileAttachmentManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DDLFileAttachmentManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DDLFileAttachmentManager";

    private Map<String, DBObjectRef<DBSchemaObject>> mappings = new HashMap<String, DBObjectRef<DBSchemaObject>>();
    private DDLFileAttachmentManager(Project project) {
        super(project);
        VirtualFileManager.getInstance().addVirtualFileListener(virtualFileListener);
        EventUtil.subscribe(project, project, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
    }

    private SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {
            if (!initialLoad && DatabaseFileSystem.isFileOpened(sourceCodeFile.getObject())) {
                updateDDLFiles(sourceCodeFile.getMainDatabaseFile());
            }
        }

        @Override
        public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            updateDDLFiles(sourceCodeFile.getMainDatabaseFile());
        }
    };

    @Nullable
    public List<VirtualFile> getAttachedDDLFiles(DBSchemaObject object) {
        List<String> fileUrls = getAttachedFileUrls(object);
        List<VirtualFile> virtualFiles = null;

        if (fileUrls.size() > 0) {
            VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
            for (String fileUrl : fileUrls) {
                VirtualFile virtualFile = virtualFileManager.findFileByUrl(fileUrl);
                if (virtualFile == null || !virtualFile.isValid()) {
                    mappings.remove(fileUrl);
                } else {
                    if (virtualFiles == null) virtualFiles = new ArrayList<VirtualFile>();
                    virtualFiles.add(virtualFile);
                }
            }
        }
        checkInvalidAttachedFiles(virtualFiles, object);
        return virtualFiles;
    }

    @Nullable
    public DBSchemaObject getEditableObject(VirtualFile ddlFile) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.get(ddlFile.getUrl());
        return DBObjectRef.get(objectRef);
    }

    public ConnectionHandler getMappedConnection(VirtualFile ddlFile) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.get(ddlFile.getUrl());
        if (objectRef != null) {
            ConnectionId connectionId = objectRef.getConnectionId();
            return ConnectionManager.getInstance(getProject()).getConnectionHandler(connectionId);
        }
        return null;
    }


    public boolean hasAttachedDDLFiles(DBSchemaObject object) {
        for (String filePath : mappings.keySet()) {
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(filePath);
            if (objectRef.is(object)) return true;
        }
        return false;
    }


    private void checkInvalidAttachedFiles(List<VirtualFile> virtualFiles, DBSchemaObject object) {
        if (virtualFiles != null && virtualFiles.size() > 0) {
            List<VirtualFile> obsolete = null;
            for (VirtualFile virtualFile : virtualFiles) {
                if (!virtualFile.isValid() || !isValidDDLFile(virtualFile, object)) {
                    if (obsolete == null) obsolete = new ArrayList<VirtualFile>();
                    obsolete.add(virtualFile);
                }
            }
            if (obsolete != null) {
                virtualFiles.removeAll(obsolete);
                for (VirtualFile virtualFile : obsolete) {
                    detachDDLFile(virtualFile);
                }
            }
        }
    }

    private static boolean isValidDDLFile(VirtualFile virtualFile, DBSchemaObject object) {
        for (DDLFileType ddlFileType : object.getDDLFileTypes()) {
            if (ddlFileType.getExtensions().contains(virtualFile.getExtension())) {
                return true;
            }
        }
        return false;
    }

    public static int showFileAttachDialog(DBSchemaObject object, List<VirtualFile> virtualFiles, boolean showLookupOption) {
        AttachDDLFileDialog dialog = new AttachDDLFileDialog(virtualFiles, object, showLookupOption);
        dialog.show();
        return dialog.getExitCode();
    }

    public static int showFileDetachDialog(DBSchemaObject object, List<VirtualFile> virtualFiles) {
        DetachDDLFileDialog dialog = new DetachDDLFileDialog(virtualFiles, object);
        dialog.show();
        return dialog.getExitCode();
    }

    public void attachDDLFile(DBSchemaObject object, VirtualFile virtualFile) {
        DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.from(object);
        if (objectRef != null) {
            mappings.put(virtualFile.getUrl(), objectRef);
            EventUtil.notify(getProject(), DDLFileAttachmentManagerListener.TOPIC).ddlFileAttached(virtualFile);
        }
    }

    public void detachDDLFile(VirtualFile virtualFile) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.remove(virtualFile.getUrl());

        // map last used connection/schema
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        ConnectionHandler activeConnection = connectionMappingManager.getConnectionHandler(virtualFile);
        if (activeConnection == null) {
            DBSchemaObject schemaObject = objectRef.get();
            if (schemaObject != null) {
                ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                DBSchema schema = schemaObject.getSchema();
                connectionMappingManager.setConnectionHandler(virtualFile, connectionHandler);
                connectionMappingManager.setDatabaseSchema(virtualFile, schema);
            }
        }

        EventUtil.notify(getProject(), DDLFileAttachmentManagerListener.TOPIC).ddlFileDetached(virtualFile);
    }

    private static List<VirtualFile> lookupApplicableDDLFiles(DBSchemaObject object) {
        List<VirtualFile> fileList = new ArrayList<VirtualFile>();

        ConnectionHandler connectionHandler = object.getConnectionHandler();
        Project project = connectionHandler.getProject();

        for (DDLFileType ddlFileType : object.getDDLFileTypes()) {
            for (String extension : ddlFileType.getExtensions()) {
                String fileName = object.getRef().getFileName().toLowerCase() + '.' + extension;
                VirtualFile[] files = VirtualFileUtil.lookupFilesForName(project, fileName);
                fileList.addAll(Arrays.asList(files));
            }
        }
        return fileList;
    }

    public List<VirtualFile> lookupDetachedDDLFiles(DBSchemaObject object) {
        List<String> fileUrls = getAttachedFileUrls(object);
        List<VirtualFile> virtualFiles = lookupApplicableDDLFiles(object);
        List<VirtualFile> detachedVirtualFiles = new ArrayList<VirtualFile>();
        for (VirtualFile virtualFile : virtualFiles) {
            if (!fileUrls.contains(virtualFile.getUrl())) {
                detachedVirtualFiles.add(virtualFile);
            }
        }

        return detachedVirtualFiles;
    }

    public void createDDLFile(final DBSchemaObject object) {
        DDLFileNameProvider fileNameProvider = getDDLFileNameProvider(object);

        if (fileNameProvider != null) {
            //ConnectionHandler connectionHandler = object.getConnectionHandler();
            final Project project = object.getProject();
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            descriptor.setTitle("Select New DDL-File Location");

            VirtualFile[] selectedDirectories = FileChooser.chooseFiles(descriptor, project, null);
            if (selectedDirectories.length > 0) {
                final String fileName = fileNameProvider.getFileName();
                final VirtualFile parentDirectory = selectedDirectories[0];
                new WriteActionRunner() {
                    @Override
                    public void run() {
                        try {
                            VirtualFile virtualFile = parentDirectory.createChildData(this, fileName);
                            attachDDLFile(object, virtualFile);
                            DBEditableObjectVirtualFile editableObjectFile = object.getEditableVirtualFile();
                            updateDDLFiles(editableObjectFile);
                            DatabaseFileSystem.getInstance().reopenEditor(object);
                        } catch (IOException e) {
                            MessageUtil.showErrorDialog(project, "Could not create file " + parentDirectory + File.separator + fileName + ".", e);
                        }
                    }
                }.start();
            }
        }                                
    }

    public void updateDDLFiles(final DBEditableObjectVirtualFile databaseFile) {
        Project project = getProject();
        DDLFileSettings ddlFileSettings = DDLFileSettings.getInstance(project);
        if (ddlFileSettings.getGeneralSettings().isSynchronizeDDLFilesEnabled()) {
            DDLFileManager ddlFileManager = DDLFileManager.getInstance(project);
            List<VirtualFile> ddlFiles = databaseFile.getAttachedDDLFiles();
            if (ddlFiles != null && !ddlFiles.isEmpty()) {
                for (VirtualFile ddlFile : ddlFiles) {
                    DDLFileType ddlFileType = ddlFileManager.getDDLFileTypeForExtension(ddlFile.getExtension());
                    DBContentType fileContentType = ddlFileType.getContentType();

                    StringBuilder buffer = new StringBuilder();
                    if (fileContentType.isBundle()) {
                        DBContentType[] contentTypes = fileContentType.getSubContentTypes();
                        for (DBContentType contentType : contentTypes) {
                            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                            if (sourceCodeFile != null) {
                                String statement = ddlFileManager.createDDLStatement(sourceCodeFile, contentType);
                                if (statement.trim().length() > 0) {
                                    buffer.append(statement);
                                    buffer.append('\n');
                                }
                                if (contentType != contentTypes[contentTypes.length - 1]) buffer.append('\n');
                            }
                        }
                    } else {
                        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(fileContentType);
                        if (sourceCodeFile != null) {
                            buffer.append(ddlFileManager.createDDLStatement(sourceCodeFile, fileContentType));
                            buffer.append('\n');
                        }
                    }
                    Document document = DocumentUtil.getDocument(ddlFile);
                    if (document != null) {
                        DocumentUtil.setText(document, buffer);
                    }
                }
            }
        }
    }

    public void attachDDLFiles(final DBSchemaObject object) {
        List<VirtualFile> virtualFiles = lookupDetachedDDLFiles(object);
        if (virtualFiles.size() == 0) {
            List<String> fileUrls = getAttachedFileUrls(object);

            StringBuilder message = new StringBuilder();
            message.append(fileUrls.size() == 0 ?
                    "No DDL Files were found in " :
                    "No additional DDL Files were found in ");
            message.append("project scope.");

            if (fileUrls.size() > 0) {
                message.append("\n\nFollowing files are already attached to ");
                message.append(object.getQualifiedNameWithType());
                message.append(':');
                for (String fileUrl : fileUrls) {
                    message.append('\n');
                    message.append(VirtualFileUtil.ensureFilePath(fileUrl));
                }
            }

            String[] options = {"OK", "Create New..."};
            MessageUtil.showInfoDialog(getProject(),
                    "No DDL files found",
                    message.toString(), options, 0,
                    new MessageCallback(1) {
                        @Override
                        protected void execute() {
                            createDDLFile(object);
                        }
            });
        } else {
            int exitCode = showFileAttachDialog(object, virtualFiles, false);
            if (exitCode != DialogWrapper.CANCEL_EXIT_CODE) {
                DatabaseFileSystem.getInstance().reopenEditor(object);
            }
        }
    }

    public void detachDDLFiles(DBSchemaObject object) {
        List<VirtualFile> virtualFiles = getAttachedDDLFiles(object);
        int exitCode = showFileDetachDialog(object, virtualFiles);
        if (exitCode != DialogWrapper.CANCEL_EXIT_CODE) {
            DatabaseFileSystem.getInstance().reopenEditor(object);
        }
    }

    private static DDLFileNameProvider getDDLFileNameProvider(DBSchemaObject object) {
        DDLFileType[] ddlFileTypes = object.getDDLFileTypes();
        if (ddlFileTypes.length == 1 && ddlFileTypes[0].getExtensions().size() == 1) {
            DDLFileType ddlFileType = ddlFileTypes[0];
            return new DDLFileNameProvider(object, ddlFileType, ddlFileType.getExtensions().get(0));
        } else {
            List<DDLFileNameProvider> fileNameProviders = new ArrayList<DDLFileNameProvider>();
            for (DDLFileType ddlFileType : ddlFileTypes) {
                for (String extension : ddlFileType.getExtensions()) {
                    DDLFileNameProvider fileNameProvider = new DDLFileNameProvider(object, ddlFileType, extension);
                    fileNameProviders.add(fileNameProvider);
                }
            }

            SelectFromListDialog fileTypeDialog = new SelectFromListDialog(
                    object.getProject(), fileNameProviders.toArray(),
                    ListUtil.BASIC_TO_STRING_ASPECT,
                    "Select DDL file type",
                    ListSelectionModel.SINGLE_SELECTION);
            JList list = (JList) fileTypeDialog.getPreferredFocusedComponent();
            list.setCellRenderer(new DDLFileNameListCellRenderer());
            fileTypeDialog.show();
            Object[] selectedFileTypes = fileTypeDialog.getSelection();
            if (selectedFileTypes != null) {
                return (DDLFileNameProvider) selectedFileTypes[0];
            }
        }
        return null;
    }

    private List<String> getAttachedFileUrls(DBSchemaObject object) {
        List<String> fileUrls = new ArrayList<String>();
        for (String fileUrl : mappings.keySet()) {
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(fileUrl);
            if (objectRef.is(object)) {
                fileUrls.add(fileUrl);
            }
        }
        return fileUrls;
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DDLFileAttachmentManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DDLFileAttachmentManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }
    public void dispose() {
        super.dispose();
        mappings.clear();
    }
    /************************************************
     *               VirtualFileListener            *
     ************************************************/

    private VirtualFileListener virtualFileListener = new VirtualFileListener() {
        @Override
        public void fileDeleted(@NotNull VirtualFileEvent event) {
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(event.getFile().getUrl());
            DBSchemaObject object = DBObjectRef.get(objectRef);
            if (object != null) {
                detachDDLFile(event.getFile());
                DatabaseFileSystem.getInstance().reopenEditor(object);
            }
        }
    };

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        for (String fileUrl : mappings.keySet()) {
            Element childElement = new Element("mapping");
            childElement.setAttribute("file-url", fileUrl);
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(fileUrl);
            objectRef.writeState(childElement);
            element.addContent(childElement);
        }

        return element;
    }

    @Override
    public void loadState(Element element) {
        VirtualFileManager virtualFileManager = VirtualFileManager.getInstance();
        for (Object child : element.getChildren()) {
            Element childElement = (Element) child;
            String fileUrl = childElement.getAttributeValue("file-url");
            if (StringUtil.isEmpty(fileUrl)) {
                // TODO backward compatibility. Do cleanup
                fileUrl = childElement.getAttributeValue("file");
            }

            if (StringUtil.isNotEmpty(fileUrl)) {
                fileUrl = VirtualFileUtil.ensureFileUrl(fileUrl);

                VirtualFile virtualFile = virtualFileManager.findFileByUrl(fileUrl);
                if (virtualFile != null) {
                    DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.from(childElement);
                    if (objectRef != null) {
                        mappings.put(fileUrl, objectRef);
                    }
                }
            }
        }
    }
}
