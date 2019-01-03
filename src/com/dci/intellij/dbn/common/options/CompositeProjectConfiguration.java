package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ProjectSupplier;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class CompositeProjectConfiguration<T extends CompositeConfigurationEditorForm> extends CompositeConfiguration<T> implements ProjectSupplier {
    private ProjectRef projectRef;

    public CompositeProjectConfiguration(@NotNull Project project) {
        this.projectRef = ProjectRef.from(project);
    }

    @NotNull
    public Project getProject() {
        return projectRef.getnn();
    }


}
