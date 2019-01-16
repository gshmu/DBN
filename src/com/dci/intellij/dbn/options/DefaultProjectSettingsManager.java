package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.impl.ProjectLifecycleListener;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "DBNavigator.DefaultProject.Settings",
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DefaultProjectSettingsManager implements ApplicationComponent, PersistentStateComponent<Element> {
    private ProjectSettings defaultProjectSettings;

    private DefaultProjectSettingsManager() {
        defaultProjectSettings = new ProjectSettings(ProjectManager.getInstance().getDefaultProject());
    }

    public static DefaultProjectSettingsManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DefaultProjectSettingsManager.class);
    }

    public ProjectSettings getDefaultProjectSettings() {
        return defaultProjectSettings;
    }

        @Override
    public void initComponent() {
        EventUtil.subscribe(null, ProjectLifecycleListener.TOPIC, projectLifecycleListener);
    }

    @Override
    public void disposeComponent() {
    }

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
        defaultProjectSettings.writeConfiguration(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        defaultProjectSettings.readConfiguration(element);
    }

    /*********************************************************
     *              ProjectLifecycleListener                 *
     *********************************************************/
    private ProjectLifecycleListener projectLifecycleListener = new ProjectLifecycleListener() {

        @Override
        public void projectComponentsInitialized(@NotNull Project project) {
            // not working. this event is notified in the project message bus
            //loadDefaultProjectSettings(project);
        }
    };
}
