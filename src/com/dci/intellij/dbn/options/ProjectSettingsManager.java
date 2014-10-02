package com.dci.intellij.dbn.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.navigation.options.NavigationSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;

@State(
        name = "DBNavigator.Project.Settings",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectSettingsManager implements ProjectComponent, PersistentStateComponent<Element> {
    private ProjectSettings projectSettings;

    private ProjectSettingsManager(Project project) {
        projectSettings = new ProjectSettings(project);
    }

    public static ProjectSettingsManager getInstance(Project project) {
        return project.getComponent(ProjectSettingsManager.class);
    }

    public static ProjectSettings getSettings(Project project) {
        if (project.isDefault()) {
            return DefaultProjectSettingsManager.getInstance().getProjectSettings();
        } else {
            return getInstance(project).getProjectSettings();
        }
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    public GeneralProjectSettings getGeneralSettings() {
        return projectSettings.getGeneralSettings();
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return projectSettings.getBrowserSettings();
    }

    public NavigationSettings getNavigationSettings() {
        return projectSettings.getNavigationSettings();
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return projectSettings.getConnectionSettings();
    }

    public DataGridSettings getDataGridSettings() {
        return projectSettings.getDataGridSettings();
    }

    public DataEditorSettings getDataEditorSettings() {
        return projectSettings.getDataEditorSettings();
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return projectSettings.getCodeCompletionSettings();
    }

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return projectSettings.getCodeStyleSettings();
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return projectSettings.getExecutionEngineSettings();
    }

    public DDLFileSettings getDdlFileSettings() {
        return projectSettings.getDdlFileSettings();
    }

    @Override
    public void projectOpened() {
        Boolean settingsLoaded = projectSettings.getProject().getUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY);
        if (settingsLoaded == null || !settingsLoaded) {
            ProjectSettings defaultProjectSettings = DefaultProjectSettingsManager.getInstance().getProjectSettings();
            Element element = new Element("state");
            defaultProjectSettings.writeConfiguration(element);
            projectSettings.readConfiguration(element);
        }
    }

    @Override
    public void projectClosed() {}

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Project.Settings";
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
        projectSettings.getProject().putUserData(DBNDataKeys.PROJECT_SETTINGS_LOADED_KEY, true);
    }
}
