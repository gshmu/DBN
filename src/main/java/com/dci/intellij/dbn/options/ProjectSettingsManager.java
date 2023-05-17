package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListener;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.tns.TnsImportType;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesBundle;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DatabaseEditorStateManager;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.object.common.loader.DatabaseLoaderManager;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsDialog;
import com.dci.intellij.dbn.vfs.DatabaseFileManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.common.message.MessageCallback.when;

@State(
    name = ProjectSettingsManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Getter
@Setter
public class ProjectSettingsManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.Settings";

    private final ProjectSettings projectSettings;
    private ConfigId lastConfigId;
    private boolean initialised;

    private ProjectSettingsManager(Project project) {
        super(project, COMPONENT_NAME);
        projectSettings = new ProjectSettings(project);
    }

    public static ProjectSettingsManager getInstance(@NotNull Project project) {
        return Components.projectService(project, ProjectSettingsManager.class);
    }

    public static ProjectSettings getSettings(Project project) {
        if (project.isDefault()) {
            return DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
        } else {
            return ProjectSettingsManager.getInstance(project).getProjectSettings();
        }
    }

    public ProjectSettings getProjectSettings() {
        return nd(projectSettings);
    }

    public GeneralProjectSettings getGeneralSettings() {
        return getProjectSettings().getGeneralSettings();
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return getProjectSettings().getBrowserSettings();
    }

    public NavigationSettings getNavigationSettings() {
        return getProjectSettings().getNavigationSettings();
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return getProjectSettings().getConnectionSettings();
    }

    public DataGridSettings getDataGridSettings() {
        return getProjectSettings().getDataGridSettings();
    }

    public DataEditorSettings getDataEditorSettings() {
        return getProjectSettings().getDataEditorSettings();
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return getProjectSettings().getCodeCompletionSettings();
    }

    public OperationSettings getOperationSettings() {
        return getProjectSettings().getOperationSettings();
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return getProjectSettings().getExecutionEngineSettings();
    }

    public DDLFileSettings getDdlFileSettings() {
        return getProjectSettings().getDdlFileSettings();
    }

    public void openDefaultProjectSettings() {
        Project project = ProjectManager.getInstance().getDefaultProject();
        ProjectSettingsDialog globalSettingsDialog = new ProjectSettingsDialog(project);
        globalSettingsDialog.show();
    }

    public void openProjectSettings(ConfigId configId) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        settingsDialog.selectSettings(configId);
        settingsDialog.show();
    }

    public void openConnectionSettings(@Nullable ConnectionId connectionId) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        settingsDialog.selectConnectionSettings(connectionId);
        settingsDialog.show();
    }

    public void createConnection(@NotNull DatabaseType databaseType, @NotNull ConnectionConfigType configType) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        ConnectionBundleSettingsForm settingsEditor = settingsDialog.getProjectSettings().getConnectionSettings().getSettingsEditor();
        if (settingsEditor != null) {
            ConnectionId connectionId = settingsEditor.createNewConnection(databaseType, configType);
            settingsDialog.selectConnectionSettings(connectionId);
            settingsDialog.show();
        }
    }

    public void createConnections(TnsNamesBundle tnsNames, TnsImportType importType, boolean selectedOnly) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        ConnectionBundleSettingsForm settingsEditor = settingsDialog.getProjectSettings().getConnectionSettings().getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.importTnsNames(tnsNames, importType, selectedOnly);
            settingsDialog.selectConnectionSettings(null);
            settingsDialog.show();
        }
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        projectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        projectSettings.readConfiguration(element);
        getProject().putUserData(UserDataKeys.PROJECT_SETTINGS_LOADED, true);
    }

    @Override
    public void initializeComponent() {
        // TODO SERVICES
        // TODO find another way to define "silent" dependencies

        Project project = getProject();
        ProjectSettingsProvider.init(project);
        DatabaseConsoleManager.getInstance(project);
        DatabaseEditorStateManager.getInstance(project);
        SourceCodeManager.getInstance(project);
        DatasetEditorManager.getInstance(project);
        DatabaseCompilerManager.getInstance(project);
        DDLFileAttachmentManager.getInstance(project);
        DatabaseLoaderManager.getInstance(project);
        DatabaseFileManager fileManager = DatabaseFileManager.getInstance(project);

        fileManager.reopenDatabaseEditors();
        initialised = true;
    }

    public void exportToDefaultSettings() {
        final Project project = getProject();
        Messages.showQuestionDialog(
                project, "Default project settings",
                "This will overwrite your default settings with the ones from the current project (including database connections configuration). \nAre you sure you want to continue?",
                new String[]{"Yes", "No"}, 0,
                option -> when(option == 0, () -> {
                    try {
                        Element element = new Element("state");
                        getProjectSettings().writeConfiguration(element);

                        ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                        ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                        defaultProjectSettings.readConfiguration(element);
                        Messages.showInfoDialog(project, "Project settings", "Project settings exported as default");
                    } finally {
                        ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                    }
                }));
    }

    public void importDefaultSettings(final boolean isNewProject) {
        final Project project = getProject();
        Boolean settingsLoaded = project.getUserData(UserDataKeys.PROJECT_SETTINGS_LOADED);
        if (settingsLoaded == null || !settingsLoaded || !isNewProject) {
            String message = isNewProject ?
                    "Do you want to import the default project settings into project \"" + project.getName() + "\"?":
                    "Your current settings will be overwritten with the default project settings, " +
                    "including database connections configuration.\n" +
                    "Are you sure you want to import the default project settings into project \"" + project.getName() + "\"?";
            Messages.showQuestionDialog(
                    project, "Default project settings",
                    message,
                    new String[]{"Yes", "No"}, 0,
                    option -> when(option == 0, () -> {
                        try {
                            Element element = new Element("state");
                            ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                            defaultProjectSettings.writeConfiguration(element);

                            ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                            getProjectSettings().readConfiguration(element);

                            ProjectEvents.notify(project,
                                    ConnectionConfigListener.TOPIC,
                                    (listener) -> listener.connectionsChanged());

                            if (!isNewProject) {
                                Messages.showInfoDialog(project, "Project settings", "Default project settings loaded to project \"" + project.getName() + "\".");
                            }
                        } finally {
                            ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                        }
                    }));
        }
    }
}
