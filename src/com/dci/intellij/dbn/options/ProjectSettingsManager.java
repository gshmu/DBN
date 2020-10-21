package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.EventNotifier;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.dci.intellij.dbn.options.ui.ProjectSettingsDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;

@State(
    name = ProjectSettingsManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ProjectSettingsManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.Settings";

    private final ProjectSettings projectSettings;
    private ConfigId lastConfigId;

    private ProjectSettingsManager(Project project) {
        super(project);
        projectSettings = new ProjectSettings(project);
    }

    public static ProjectSettingsManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, ProjectSettingsManager.class);
    }

    public static ProjectSettings getSettings(Project project) {
        if (project.isDefault()) {
            return DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
        } else {
            return ProjectSettingsManager.getInstance(project).getProjectSettings();
        }
    }

    public ConfigId getLastConfigId() {
        return lastConfigId;
    }

    public void setLastConfigId(ConfigId lastConfigId) {
        this.lastConfigId = lastConfigId;
    }

    public ProjectSettings getProjectSettings() {
        return Failsafe.nd(projectSettings);
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

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return getProjectSettings().getCodeStyleSettings();
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

    public void createConnections(List<TnsName> tnsNames) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        ConnectionBundleSettingsForm settingsEditor = settingsDialog.getProjectSettings().getConnectionSettings().getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.importTnsNames(tnsNames);
            settingsDialog.selectConnectionSettings(null);
            settingsDialog.show();
        }
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        projectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        projectSettings.readConfiguration(element);
        getProject().putUserData(UserDataKeys.PROJECT_SETTINGS_LOADED, true);
    }

    public void exportToDefaultSettings() {
        final Project project = getProject();
        MessageUtil.showQuestionDialog(
                project, "Default project settings",
                "This will overwrite your default settings with the ones from the current project (including database connections configuration). \nAre you sure you want to continue?",
                new String[]{"Yes", "No"}, 0,
                (option) -> conditional(option == 0,
                        () -> {
                            try {
                                Element element = new Element("state");
                                getProjectSettings().writeConfiguration(element);

                                ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                                ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                                defaultProjectSettings.readConfiguration(element);
                                MessageUtil.showInfoDialog(project, "Project settings", "Project settings exported as default");
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
            MessageUtil.showQuestionDialog(
                    project, "Default project settings",
                    message,
                    new String[]{"Yes", "No"}, 0,
                    (option) -> conditional(option == 0,
                            () -> {
                                try {
                                    Element element = new Element("state");
                                    ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                                    defaultProjectSettings.writeConfiguration(element);

                                    ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                                    getProjectSettings().readConfiguration(element);

                                    EventNotifier.notify(project,
                                            ConnectionSettingsListener.TOPIC,
                                            (listener) -> listener.connectionsChanged());

                                    if (!isNewProject) {
                                        MessageUtil.showInfoDialog(project, "Project settings", "Default project settings loaded to project \"" + project.getName() + "\".");
                                    }
                                } finally {
                                    ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                                }
                            }));
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }
}
