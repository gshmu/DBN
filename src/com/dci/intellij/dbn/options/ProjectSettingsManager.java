package com.dci.intellij.dbn.options;

import java.util.List;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionSetupListener;
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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

@State(
        name = "DBNavigator.Project.Settings",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectSettingsManager implements ProjectComponent, PersistentStateComponent<Element> {
    private Project project;
    private ProjectSettings projectSettings;

    private ProjectSettingsManager(Project project) {
        this.project = project;
        projectSettings = new ProjectSettings(project);
    }

    public static ProjectSettingsManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ProjectSettingsManager.class);
    }

    public static ProjectSettings getSettings(Project project) {
        if (project.isDefault()) {
            return DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
        } else {
            return ProjectSettingsManager.getInstance(project).getProjectSettings();
        }
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
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
        settingsDialog.focusSettings(configId);
        settingsDialog.show();
    }

    public void openConnectionSettings(@Nullable String connectionId) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        settingsDialog.focusConnectionSettings(connectionId);
        settingsDialog.show();
    }

    public void createConnection(@NotNull DatabaseType databaseType, @NotNull ConnectionConfigType configType) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        ConnectionBundleSettingsForm settingsEditor = settingsDialog.getProjectSettings().getConnectionSettings().getSettingsEditor();
        if (settingsEditor != null) {
            String connectionId = settingsEditor.createNewConnection(databaseType, configType);
            settingsDialog.focusConnectionSettings(connectionId);
            settingsDialog.show();
        }
    }

    public void createConnections(List<TnsName> tnsNames) {
        Project project = getProject();
        ProjectSettingsDialog settingsDialog = new ProjectSettingsDialog(project);
        ConnectionBundleSettingsForm settingsEditor = settingsDialog.getProjectSettings().getConnectionSettings().getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.importTnsNames(tnsNames);
            settingsDialog.focusConnectionSettings(null);
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
    public void loadState(Element element) {
        projectSettings.readConfiguration(element);
        getProject().putUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY, true);
    }

    private Project getProject() {
        return project;
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {}

    @Override
    public void initComponent() {
        //importDefaultSettings(true);
    }

    public void exportToDefaultSettings() {
        MessageUtil.showQuestionDialog(
                project, "Default Project Settings",
                "This will overwrite your default settings with the ones from the current project (including database connections configuration). \nAre you sure you want to continue?",
                new String[]{"Yes", "No"}, 0,
                new SimpleTask() {
                    @Override
                    protected boolean canExecute() {
                        return getHandle() == 0;
                    }

                    @Override
                    protected void execute() {
                        try {
                            Element element = new Element("state");
                            getProjectSettings().writeConfiguration(element);

                            ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                            ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                            defaultProjectSettings.readConfiguration(element);
                            MessageUtil.showInfoDialog(project, "Project Settings", "Project settings exported as default");
                        } finally {
                            ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                        }
                    }
                });
    }

    public void importDefaultSettings(final boolean isNewProject) {
        Boolean settingsLoaded = project.getUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY);
        if (settingsLoaded == null || !settingsLoaded || !isNewProject) {
            String message = isNewProject ?
                    "Do you want to import the default project settings into project \"" + project.getName() + "\"?":
                    "Your current settings will be overwritten with the default project settings, including database connections configuration. \nAre you sure you want to import the default project settings into project \"" + project.getName() + "\"?";
            MessageUtil.showQuestionDialog(
                    project, "Default Project Settings",
                    message,
                    new String[]{"Yes", "No"}, 0,
                    new SimpleTask() {
                        @Override
                        protected boolean canExecute() {
                            return getHandle() == 0;
                        }

                        @Override
                        protected void execute() {
                            try {
                                Element element = new Element("state");
                                ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings();
                                defaultProjectSettings.writeConfiguration(element);

                                ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(true);
                                getProjectSettings().readConfiguration(element);

                                EventUtil.notify(project, ConnectionSetupListener.TOPIC).setupChanged();

                                if (!isNewProject) {
                                    MessageUtil.showInfoDialog(project, "Project Settings", "Default project settings loaded to project \"" + project.getName() + "\".");
                                }
                            } finally {
                                ConnectionBundleSettings.IS_IMPORT_EXPORT_ACTION.set(false);
                            }

                        }
                    });
        }
    }

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.Settings";
    }
}
