package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Write;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseDDLInterface;
import com.dci.intellij.dbn.ddl.options.DDLFileExtensionSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.fileTypes.FileTypeListener;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.util.Alarm;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@State(
    name = DDLFileManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DDLFileManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, NotificationSupport {

    public static final String COMPONENT_NAME = "DBNavigator.Project.DDLFileManager";

    private DDLFileManager(@NotNull Project project) {
        super(project);

        ProjectEvents.subscribe(project, this, FileTypeManager.TOPIC, fileTypeListener);

        StartupManager startupManager = StartupManager.getInstance(project);
        startupManager.registerPostStartupActivity((DumbAwareRunnable) () -> registerExtensions(getExtensionSettings()));
    }

    private final Alarm extensionRegisterer = Dispatch.alarm(DDLFileManager.this);

    public void registerExtensions(DDLFileExtensionSettings settings) {
        Dispatch.alarmRequest(extensionRegisterer, 0, false, () ->
                Write.run(getProject(), () -> {
                    FileTypeManager fileTypeManager = FileTypeManager.getInstance();
                    List<DDLFileType> ddlFileTypeList = settings.getFileTypes();
                    for (DDLFileType ddlFileType : ddlFileTypeList) {
                        for (String extension : ddlFileType.getExtensions()) {
                            fileTypeManager.associateExtension(ddlFileType.getLanguageFileType(), extension);
                        }
                    }
                }));
    }

    public static DDLFileManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DDLFileManager.class);
    }

    private DDLFileExtensionSettings getExtensionSettings() {
        return DDLFileSettings.getInstance(getProject()).getExtensionSettings();
    }

    public DDLFileType getDDLFileType(DDLFileTypeId ddlFileTypeId) {
        return getExtensionSettings().getFileType(ddlFileTypeId);
    }

    DDLFileType getDDLFileTypeForExtension(String extension) {
        return getExtensionSettings().getFileTypeForExtension(extension);
    }

    String createDDLStatement(DBSourceCodeVirtualFile sourceCodeFile, DBContentType contentType) {
        DBSchemaObject object = sourceCodeFile.getObject();
        String content = sourceCodeFile.getOriginalContent().toString().trim();
        if (content.length() > 0) {
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            String alternativeStatementDelimiter = connectionHandler.getSettings().getDetailSettings().getAlternativeStatementDelimiter();
            DatabaseDDLInterface ddlInterface = connectionHandler.getInterfaceProvider().getDdlInterface();
            return ddlInterface.createDDLStatement(getProject(),
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

    @NotNull
    List<DDLFileType> getDDLFileTypes(DBObjectType objectType) {
        Collection<DDLFileTypeId> ddlFileTypeIds = objectType.getDdlFileTypeIds();
        if (ddlFileTypeIds != null) {
            List<DDLFileType> ddlFileTypes = new ArrayList<>();
            ddlFileTypeIds.forEach(ddlFileTypeId -> ddlFileTypes.add(getDDLFileType(ddlFileTypeId)));
            return ddlFileTypes;
        }
        return Collections.emptyList();
    }


    /***************************************
     *            FileTypeListener         *
     ***************************************/

    private final FileTypeListener fileTypeListener = new FileTypeListener() {
        @Override
        public void fileTypesChanged(@NotNull FileTypeEvent event) {
            FileTypeManager fileTypeManager = FileTypeManager.getInstance();
            List<DDLFileType> ddlFileTypeList = getExtensionSettings().getFileTypes();
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

                Write.run(getProject(), () -> {
                    StringBuilder restoredAssociations = new StringBuilder();
                    for (String extension : ddlFileType.getExtensions()) {
                        if (!registeredExtension.contains(extension)) {
                            fileTypeManager.associateExtension(fileType, extension);
                            if (restoredAssociations.length() > 0) {
                                restoredAssociations.append(", ");
                            }
                            restoredAssociations.append(extension);

                        }
                    }

                    if (restoredAssociations.length() > 0) {
                        sendInfoNotification(
                                NotificationGroup.DDL,
                                "Following file associations have been restored: \"" + restoredAssociations + "\". " +
                                        "They are registered as DDL file types in project \"" + getProject().getName() + "\".\n" +
                                        "Please remove them from project DDL configuration first (Project Settings > DB Navigator > DDL File Settings).");
                    }
                });
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
