package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.WriteAction;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.ddl.options.DDLFileExtensionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@State(
    name = DDLFileManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DDLFileManager extends AbstractProjectComponent implements PersistentStateComponent<Element>{

    public static final String COMPONENT_NAME = "DBNavigator.Project.DDLFileManager";

    private DDLFileManager(Project project) {
        super(project);
    }
    private static boolean isRegisteringFileTypes = false;

    public static void registerExtensions(final DDLFileExtensionSettings settings) {
        WriteAction.invoke(() -> {
            try {
                isRegisteringFileTypes = true;
                FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                List<DDLFileType> ddlFileTypeList = settings.getDDLFileTypes();
                for (DDLFileType ddlFileType : ddlFileTypeList) {
                    for (String extension : ddlFileType.getExtensions()) {
                        fileTypeManager.associateExtension(ddlFileType.getLanguageFileType(), extension);
                    }
                }
            } finally {
                isRegisteringFileTypes = false;
            }
        });
    }

    public static DDLFileManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DDLFileManager.class);
    }

    private DDLFileExtensionSettings getExtensionSettings() {
        return DDLFileSettings.getInstance(getProject()).getExtensionSettings();
    }

    public DDLFileType getDDLFileType(DDLFileTypeId ddlFileTypeId) {
        return getExtensionSettings().getDDLFileType(ddlFileTypeId);
    }

    DDLFileType getDDLFileTypeForExtension(String extension) {
        return getExtensionSettings().getDDLFileTypeForExtension(extension);
    }

    String createDDLStatement(DBSourceCodeVirtualFile sourceCodeFile, DBContentType contentType) {
        DBSchemaObject object = sourceCodeFile.getObject();
        String content = sourceCodeFile.getOriginalContent().toString().trim();
        if (content.length() > 0) {
            Project project = getProject();

            ConnectionHandler connectionHandler = object.getConnectionHandler();
            String alternativeStatementDelimiter = connectionHandler.getSettings().getDetailSettings().getAlternativeStatementDelimiter();
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDDLInterface();
            return ddlInterface.createDDLStatement(project,
                    object.getObjectType().getTypeId(),
                    connectionHandler.getUserName(),
                    object.getSchema().getName(),
                    object.getName(),
                    contentType,
                    content,
                    alternativeStatementDelimiter);
        }
        return "";
    }


    @Nullable
    public DDLFileType getDDLFileType(DBObjectType objectType, DBContentType contentType) {
        DDLFileTypeId ddlFileTypeId = objectType.getDdlFileTypeId(contentType);
        return ddlFileTypeId == null ? null : getDDLFileType(ddlFileTypeId);
    }

    List<DDLFileType> getDDLFileTypes(DBObjectType objectType) {
        Collection<DDLFileTypeId> ddlFileTypeIds = objectType.getDdlFileTypeIds();
        if (ddlFileTypeIds != null) {
            List<DDLFileType> ddlFileTypes = new ArrayList<>();
            ddlFileTypeIds.forEach(ddlFileTypeId -> ddlFileTypes.add(getDDLFileType(ddlFileTypeId)));
            return ddlFileTypes;
        }
        return null;
    }


    /***************************************
     *            FileTypeListener         *
     ***************************************/

    private FileTypeListener fileTypeListener = new FileTypeListener() {
        @Override
        public void beforeFileTypesChanged(@NotNull FileTypeEvent event) {

        }

        @Override
        public void fileTypesChanged(@NotNull FileTypeEvent event) {
            if (!isRegisteringFileTypes) {
                StringBuilder restoredAssociations = null;
                FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                List<DDLFileType> ddlFileTypeList = getExtensionSettings().getDDLFileTypes();
                for (DDLFileType ddlFileType : ddlFileTypeList) {
                    DBLanguageFileType fileType = ddlFileType.getLanguageFileType();
                    List<FileNameMatcher> associations = fileTypeManager.getAssociations(fileType);
                    List<String> registeredExtension = new ArrayList<>();
                    for (FileNameMatcher association : associations) {
                        if (association instanceof ExtensionFileNameMatcher) {
                            ExtensionFileNameMatcher extensionMatcher = (ExtensionFileNameMatcher) association;
                            registeredExtension.add(extensionMatcher.getExtension());
                        }
                    }

                    for (String extension : ddlFileType.getExtensions()) {
                        if (!registeredExtension.contains(extension)) {
                            fileTypeManager.associateExtension(fileType, extension);
                            if (restoredAssociations == null) {
                                restoredAssociations = new StringBuilder();
                            } else {
                                restoredAssociations.append(", ");
                            }
                            restoredAssociations.append(extension);

                        }
                    }
                }
                if (restoredAssociations != null) {
                    String message =
                            "Following file associations have been restored: \"" + restoredAssociations + "\". " +
                                    "They are registered as DDL file types in project \"" + getProject().getName() + "\".\n" +
                                    "Please remove them from project DDL configuration first (Project Settings > DB Navigator > DDL File Settings).";
                    MessageUtil.showWarningDialog(getProject(), "Restored file extensions", message);
                }
            }
        }
    };

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @Override
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public void projectOpened() {
        SimpleLaterInvocator.invokeNonModal(() -> registerExtensions(getExtensionSettings()));
    }

    @Override
    public void projectClosed() {
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {

    }
}
