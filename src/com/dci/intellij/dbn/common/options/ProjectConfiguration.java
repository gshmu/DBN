package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ProjectSupplier;
import com.intellij.openapi.project.Project;

public abstract class ProjectConfiguration<T extends ConfigurationEditorForm> extends Configuration<T> implements ProjectSupplier {
    private Project project;

    public ProjectConfiguration(Project project) {
        this.project = project;
    }

    @Override
    public Project getProject() {
        return project;
    }
}
