package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.code.common.style.options.ProjectCodeStyleSettings;
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
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "DBNavigator.Project.Settings",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ProjectSettings implements ProjectComponent, PersistentStateComponent<Element> {
    private GlobalProjectSettings globalProjectSettings;

    private ProjectSettings(Project project) {
        globalProjectSettings = new GlobalProjectSettings(project);
    }

    public static ProjectSettings getInstance(Project project) {
        return project.getComponent(ProjectSettings.class);
    }

    public GlobalProjectSettings getGlobalProjectSettings() {
        return globalProjectSettings;
    }

    public GeneralProjectSettings getGeneralSettings() {
        return globalProjectSettings.getGeneralSettings();
    }

    public DatabaseBrowserSettings getBrowserSettings() {
        return globalProjectSettings.getBrowserSettings();
    }

    public NavigationSettings getNavigationSettings() {
        return globalProjectSettings.getNavigationSettings();
    }

    public ConnectionBundleSettings getConnectionSettings() {
        return globalProjectSettings.getConnectionSettings();
    }

    public DataGridSettings getDataGridSettings() {
        return globalProjectSettings.getDataGridSettings();
    }

    public DataEditorSettings getDataEditorSettings() {
        return globalProjectSettings.getDataEditorSettings();
    }

    public CodeCompletionSettings getCodeCompletionSettings() {
        return globalProjectSettings.getCodeCompletionSettings();
    }

    public ProjectCodeStyleSettings getCodeStyleSettings() {
        return globalProjectSettings.getCodeStyleSettings();
    }

    public ExecutionEngineSettings getExecutionEngineSettings() {
        return globalProjectSettings.getExecutionEngineSettings();
    }

    public DDLFileSettings getDdlFileSettings() {
        return globalProjectSettings.getDdlFileSettings();
    }

    @Override
    public void projectOpened() {}

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
        globalProjectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        globalProjectSettings.readConfiguration(element);
    }
}
