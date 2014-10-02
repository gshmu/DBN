package com.dci.intellij.dbn.options;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.ProjectManager;

@State(
        name = "DBNavigator.DefaultProject.Settings",
        storages = {@Storage(file = StoragePathMacros.APP_CONFIG + "/dbnavigator.xml")}
)
public class DefaultProjectSettingsManager implements ApplicationComponent, PersistentStateComponent<Element> {
    private ProjectSettings projectSettings;

    private DefaultProjectSettingsManager() {
        projectSettings = new ProjectSettings(ProjectManager.getInstance().getDefaultProject());
    }

    public static DefaultProjectSettingsManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DefaultProjectSettingsManager.class);
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

        @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.Application.TemplateProjectSettings";
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
    }
}
