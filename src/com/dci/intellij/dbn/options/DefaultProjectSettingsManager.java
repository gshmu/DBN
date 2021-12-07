package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.util.InternalApi;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "DBNavigator.DefaultProject.Settings",
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DefaultProjectSettingsManager implements ApplicationComponent, PersistentStateComponent<Element> {
    private Element stateCapture;

    private final Latent<ProjectSettings> defaultProjectSettings = Latent.basic(() ->  {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project defaultProject = projectManager.getDefaultProject();
        ProjectSettings projectSettings = new ProjectSettings(defaultProject);
        if (stateCapture != null) {
            projectSettings.readConfiguration(stateCapture);
            stateCapture = null;
        }
        return projectSettings;
    });

    private DefaultProjectSettingsManager() {}

    public static DefaultProjectSettingsManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DefaultProjectSettingsManager.class);
    }

    @NotNull
    public ProjectSettings getDefaultProjectSettings() {
        return defaultProjectSettings.get();
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
        ProjectSettings projectSettings = attemptLoadProjectSettings();
        if (projectSettings != null) {
            Element element = new Element("state");
            projectSettings.writeConfiguration(element);
            return element;
        } else {
            return stateCapture;
        }
    }


    @Override
    public void loadState(@NotNull Element element) {
        ProjectSettings projectSettings = attemptLoadProjectSettings();
        if (projectSettings != null) {
            projectSettings.readConfiguration(element);
            stateCapture = null;
        } else {
            stateCapture = element;
        }
    }

    @Nullable
    private ProjectSettings attemptLoadProjectSettings() {
        return InternalApi.isComponentsLoadedOccurred() ?
                getDefaultProjectSettings() :
                defaultProjectSettings.value();
    }


}
