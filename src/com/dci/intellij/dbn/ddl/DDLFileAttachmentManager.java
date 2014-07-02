package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.ui.ListUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.VirtualFileUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.ddl.ui.AttachDDLFileDialog;
import com.dci.intellij.dbn.ddl.ui.DDLFileNameListCellRenderer;
import com.dci.intellij.dbn.ddl.ui.DetachDDLFileDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.SelectFromListDialog;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileCopyEvent;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileMoveEvent;
import com.intellij.openapi.vfs.VirtualFilePropertyEvent;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DDLFileAttachmentManager extends AbstractProjectComponent implements VirtualFileListener, JDOMExternalizable {

    private Map<String, DBObjectRef<DBSchemaObject>> mappings = new HashMap<String, DBObjectRef<DBSchemaObject>>();
    private DDLFileAttachmentManager(Project project) {
        super(project);
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    @Nullable
    public List<VirtualFile> getBoundDDLFiles(DBSchemaObject object) {
        List<String> filePaths = getBoundFilePaths(object);
        List<VirtualFile> virtualFiles = null;

        if (filePaths.size() > 0) {
            for (String filePath : filePaths) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
                if (virtualFile == null || !virtualFile.isValid()) {
                    mappings.remove(filePath);    
                } else {
                    if (virtualFiles == null) virtualFiles = new ArrayList<VirtualFile>();
                    virtualFiles.add(virtualFile);
                }
            }
        }
        checkInvalidBoundFiles(virtualFiles, object);
        return virtualFiles;
    }

    @Nullable
    public DBSchemaObject getEditableObject(VirtualFile ddlFile) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.get(ddlFile.getPath());
        return DBObjectRef.get(objectRef);
    }


    public boolean hasBoundDDLFiles(DBSchemaObject object) {
        for (String filePath : mappings.keySet()) {
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(filePath);
            if (objectRef.is(object)) return true;
        }
        return false;
    }


    private void checkInvalidBoundFiles(List<VirtualFile> virtualFiles, DBSchemaObject object) {
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

    private boolean isValidDDLFile(VirtualFile virtualFile, DBSchemaObject object) {
        for (DDLFileType ddlFileType : object.getDDLFileTypes()) {
            if (ddlFileType.getExtensions().contains(virtualFile.getExtension())) {
                return true;
            }
        }
        return false;
    }

    public int showFileAttachDialog(DBSchemaObject object, List<VirtualFile> virtualFiles, boolean showLookupOption) {
        AttachDDLFileDialog dialog = new AttachDDLFileDialog(virtualFiles, object, showLookupOption);
        dialog.show();
        return dialog.getExitCode();
    }

    public int showFileDetachDialog(DBSchemaObject object, List<VirtualFile> virtualFiles) {
        DetachDDLFileDialog dialog = new DetachDDLFileDialog(virtualFiles, object);
        dialog.show();
        return dialog.getExitCode();
    }

    public void bindDDLFile(DBSchemaObject object, VirtualFile virtualFile) {
        DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.from(object);
        if (objectRef != null) {
            mappings.put(virtualFile.getPath(), objectRef);
            EventManager.notify(getProject(), DDLMappingListener.TOPIC).ddlFileAttached(virtualFile);
        }
    }

    public void detachDDLFile(VirtualFile virtualFile) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.remove(virtualFile.getPath());

        // map last used connection/schema
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);
        if (activeConnection == null) {
            DBSchemaObject schemaObject = objectRef.get();
            if (schemaObject != null) {
                ConnectionHandler connectionHandler = schemaObject.getConnectionHandler();
                DBSchema schema = schemaObject.getSchema();
                connectionMappingManager.setActiveConnection(virtualFile, connectionHandler);
                connectionMappingManager.setCurrentSchema(virtualFile, schema);
            }
        }

        EventManager.notify(getProject(), DDLMappingListener.TOPIC).ddlFileDetached(virtualFile);
    }

    private List<VirtualFile> lookupApplicableDDLFiles(DBSchemaObject object) {
        Module module = object.getConnectionHandler().getModule();
        Project project = object.getConnectionHandler().getProject();
        List<VirtualFile> fileList = new ArrayList<VirtualFile>();

        for (DDLFileType ddlFileType : object.getDDLFileTypes()) {
            for (String extension : ddlFileType.getExtensions()) {
                String fileName = object.getName().toLowerCase() + "." + extension;

                if (module == null) {
                    VirtualFile[] files = VirtualFileUtil.lookupFilesForName(project, fileName);
                    fileList.addAll(Arrays.asList(files));
                } else {
                    VirtualFile[] files = VirtualFileUtil.lookupFilesForName(module, fileName);
                    fileList.addAll(Arrays.asList(files));
                }
            }
        }
        return fileList;
    }

    public List<VirtualFile> lookupUnboundDDLFiles(DBSchemaObject object) {
        List<String> filePaths = getBoundFilePaths(object);
        List<VirtualFile> virtualFiles = lookupApplicableDDLFiles(object);
        List<VirtualFile> unboundVirtualFiles = new ArrayList<VirtualFile>();
        for (VirtualFile virtualFile : virtualFiles) {
            if (!filePaths.contains(virtualFile.getPath())) {
                unboundVirtualFiles.add(virtualFile);
            }
        }

        return unboundVirtualFiles;
    }

    public void createDDLFile(final DBSchemaObject object) {
        DDLFileNameProvider fileNameProvider = getDDLFileNameProvider(object);

        if (fileNameProvider != null) {
            //ConnectionHandler connectionHandler = object.getConnectionHandler();
            final Project project = object.getProject();
            FileChooserDescriptor descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
            descriptor.setTitle("Select New DDL-File Location");

/*            VirtualFile[] contentRoots;

            Module module = connectionHandler.getModule();
            if (module == null) {
                ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
                contentRoots = rootManager.getContentRoots();
            } else {
                ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                contentRoots = rootManager.getContentRoots();
            }
            descriptor.setIsTreeRootVisible(contentRoots.length == 1);
            for (VirtualFile contentRoot : contentRoots) {
                descriptor.addRoot(contentRoot);
            }*/

            VirtualFile[] selectedDirectories = FileChooser.chooseFiles(descriptor, project, null);
            if (selectedDirectories.length > 0) {
                final String fileName = fileNameProvider.getFileName();
                final VirtualFile parentDirectory = selectedDirectories[0];
                new WriteActionRunner() {
                    @Override
                    public void run() {
                        try {
                            VirtualFile virtualFile = parentDirectory.createChildData(this, fileName);
                            bindDDLFile(object, virtualFile);
                            DatabaseEditableObjectFile databaseFile = object.getVirtualFile();
                            databaseFile.updateDDLFiles();
                            DatabaseFileSystem.getInstance().reopenEditor(object);
                        } catch (IOException e) {
                            MessageUtil.showErrorDialog("Could not create file " + parentDirectory + File.separator + fileName + ".", e);
                        }
                    }
                }.start();
            }
        }                                
    }

    public void bindDDLFiles(DBSchemaObject object) {
        Project project = object.getProject();
        List<VirtualFile> virtualFiles = lookupUnboundDDLFiles(object);
        if (virtualFiles.size() == 0) {
            Module module = object.getConnectionHandler().getModule();
            List<String> boundFiles = getBoundFilePaths(object);

            StringBuilder message = new StringBuilder();
            message.append(boundFiles.size() == 0 ?
                    "No DDL Files were found in " :
                    "No additional DDL Files were found in ");
            if (module == null) {
                message.append("project scope.");
            } else {
                message.append("scope of module\"");
                message.append(module.getName());
                message.append("\".");
            }


            if (boundFiles.size() > 0) {
                message.append("\n\nFollowing files are already attached to ");
                message.append(object.getQualifiedNameWithType());
                message.append(":");
                for (String boundFile : boundFiles) {
                    message.append("\n");
                    message.append(boundFile);
                }
            }

            String[] options = {"OK", "Create new..."};
            int optionIndex = Messages.showDialog(project, message.toString(), Constants.DBN_TITLE_PREFIX + "No DDL Files found", options, 0, Messages.getInformationIcon() );
            if (optionIndex == 1) {
                createDDLFile(object);
            }
        } else {
            int exitCode = showFileAttachDialog(object, virtualFiles, false);
            if (exitCode != DialogWrapper.CANCEL_EXIT_CODE) {
                DatabaseFileSystem.getInstance().reopenEditor(object);
            }
        }
    }

    public void detachDDLFiles(DBSchemaObject object) {
        List<VirtualFile> virtualFiles = getBoundDDLFiles(object);
        int exitCode = showFileDetachDialog(object, virtualFiles);
        if (exitCode != DialogWrapper.CANCEL_EXIT_CODE) {
            DatabaseFileSystem.getInstance().reopenEditor(object);
        }
    }

    private DDLFileNameProvider getDDLFileNameProvider(DBSchemaObject object) {
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

    private List<String> getBoundFilePaths(DBSchemaObject object) {
        String objectPath = object.getQualifiedNameWithConnectionId();
        List<String> filePaths = new ArrayList<String>();
        for (String filePath : mappings.keySet()) {
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(filePath);
            if (objectRef.is(object)) {
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DDLFileAttachmentManager getInstance(Project project) {
        return project.getComponent(DDLFileAttachmentManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DDLFileAttachmentManager";
    }
    public void disposeComponent() {
        mappings.clear();
        super.disposeComponent();
    }
    /************************************************
     *               JDOMExternalizable             *
     ************************************************/

    @Override
    public void propertyChanged(@NotNull VirtualFilePropertyEvent event) {
        
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        DBObjectRef<DBSchemaObject> objectRef = mappings.get(event.getFile().getPath());
        DBSchemaObject object = DBObjectRef.get(objectRef);
        if (object != null) {
            detachDDLFile(event.getFile());
            DatabaseFileSystem.getInstance().reopenEditor(object);
        }
    }

    @Override
    public void fileMoved(@NotNull VirtualFileMoveEvent event) {
    }

    @Override
    public void fileCopied(@NotNull VirtualFileCopyEvent event) {
    }

    @Override
    public void beforePropertyChange(@NotNull VirtualFilePropertyEvent event) {
    }

    @Override
    public void beforeContentsChange(@NotNull VirtualFileEvent event) {
    }

    @Override
    public void beforeFileDeletion(@NotNull VirtualFileEvent event) {
    }

    @Override
    public void beforeFileMovement(@NotNull VirtualFileMoveEvent event) {
    }

    /************************************************
     *               JDOMExternalizable             *
     ************************************************/
    public void readExternal(Element element) throws InvalidDataException {
        for (Object child : element.getChildren()) {
            Element childElement = (Element) child;
            String file = childElement.getAttributeValue("file");
            DBObjectRef<DBSchemaObject> objectRef = DBObjectRef.from(childElement);
            if (objectRef != null) {
                mappings.put(file, objectRef);
            }

        }
    }

    public void writeExternal(Element element) throws WriteExternalException {
        for (String file : mappings.keySet()) {
            Element childElement = new Element("mapping");
            childElement.setAttribute("file", file);
            DBObjectRef<DBSchemaObject> objectRef = mappings.get(file);
            objectRef.writeState(childElement);
            element.addContent(childElement);
        }

    }
}
