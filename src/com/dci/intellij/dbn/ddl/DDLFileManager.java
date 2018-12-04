package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.ddl.options.DDLFileExtensionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        WriteActionRunner.invoke(() -> {
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
        return FailsafeUtil.getComponent(project, DDLFileManager.class);
    }

    public DDLFileExtensionSettings getExtensionSettings() {
        return DDLFileSettings.getInstance(getProject()).getExtensionSettings();
    }

    public DDLFileType getDDLFileType(DDLFileTypeId ddlFileTypeId) {
        return getExtensionSettings().getDDLFileType(ddlFileTypeId);
    }

    public DDLFileType getDDLFileTypeForExtension(String extension) {
        return getExtensionSettings().getDDLFileTypeForExtension(extension);
    }

    public String createDDLStatement(DBSourceCodeVirtualFile sourceCodeFile, DBContentType contentType) {
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
                    List<String> registeredExtension = new ArrayList<String>();
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
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public void projectOpened() {
        SimpleLaterInvocator.invoke(() -> registerExtensions(getExtensionSettings()));
    }

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
