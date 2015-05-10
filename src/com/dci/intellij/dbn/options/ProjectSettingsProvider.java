package com.dci.intellij.dbn.options;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

public class ProjectSettingsProvider extends ConfigurableProvider{
    private Project project;

    public ProjectSettingsProvider(Project project) {
        this.project = project;
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
        ProjectSettings projectSettings = new ProjectSettings(project);
        ProjectSettings currentProjectSettings = project.isDefault() ?
                DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings() :
                ProjectSettings.getInstance(project);
        Element state = currentProjectSettings.getState();
        projectSettings.loadState(state);
        return projectSettings;
    }
}
