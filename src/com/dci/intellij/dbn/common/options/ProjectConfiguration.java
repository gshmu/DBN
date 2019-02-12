package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ProjectSupplier;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class ProjectConfiguration<T extends ConfigurationEditorForm> extends Configuration<T> implements ProjectSupplier {
    private ProjectRef projectRef;

    public ProjectConfiguration(Project project) {
        this.projectRef = ProjectRef.from(project);
    }

    @NotNull
    @Override
    public Project getProject() {
        return projectRef.getnn();
    }
}
