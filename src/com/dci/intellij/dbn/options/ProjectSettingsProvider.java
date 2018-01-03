package com.dci.intellij.dbn.options;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ProjectRef;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableProvider;
import com.intellij.openapi.project.Project;

public class ProjectSettingsProvider extends ConfigurableProvider{
    private ProjectRef projectRef;

    public ProjectSettingsProvider(Project project) {
        this.projectRef = ProjectRef.from(project);
    }

    @Nullable
    @Override
    public Configurable createConfigurable() {
        Project project = getProject();
        ProjectSettings projectSettings = project.isDefault() ?
                DefaultProjectSettingsManager.getInstance().getDefaultProjectSettings() :
                ProjectSettingsManager.getInstance(project).getProjectSettings();

        return projectSettings.clone();
    }

    @NotNull
    public Project getProject() {
        return projectRef.getnn();
    }
}
