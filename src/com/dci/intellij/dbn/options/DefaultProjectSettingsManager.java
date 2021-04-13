package com.dci.intellij.dbn.options;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.ApplicationComponent;
import com.dci.intellij.dbn.common.latent.Latent;
import com.intellij.ide.ApplicationInitializedListener;
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
public class DefaultProjectSettingsManager implements ApplicationComponent, PersistentStateComponent<Element>, ApplicationInitializedListener {
    private boolean componentsInitialised = false;

    private final Latent<ProjectSettings> defaultProjectSettings = Latent.basic(() -> {
        ProjectManager projectManager = ProjectManager.getInstance();
        Project defaultProject = projectManager.getDefaultProject();
        return new ProjectSettings(defaultProject);
    });

    @Override
    public void componentsInitialized() {
        componentsInitialised = true;
    }

    private DefaultProjectSettingsManager() {}

    public static DefaultProjectSettingsManager getInstance() {
        return ApplicationManager.getApplication().getComponent(DefaultProjectSettingsManager.class);
    }

    @Nullable
    public ProjectSettings getDefaultProjectSettings() {
        return componentsInitialised ? defaultProjectSettings.get() : null;
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
        ProjectSettings projectSettings = getDefaultProjectSettings();
        if (projectSettings != null) {
            Element element = new Element("state");
            projectSettings.writeConfiguration(element);
            return element;
        }
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {
        ProjectSettings projectSettings = getDefaultProjectSettings();
        if (projectSettings != null) {
            projectSettings.readConfiguration(element);
        }
    }
}
