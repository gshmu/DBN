package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class CompositeProjectConfiguration<P extends ProjectConfiguration, E extends CompositeConfigurationEditorForm>
        extends CompositeConfiguration<P, E>
        implements ProjectConfiguration<P, E> {

    private ProjectRef projectRef;

    public CompositeProjectConfiguration(P parent) {
        super(parent);
    }

    public CompositeProjectConfiguration(@NotNull Project project) {
        super(null);
        this.projectRef = ProjectRef.of(project);
    }

    @NotNull
    @Override
    public Project getProject() {
        P parent = getParent();
        return parent == null ? projectRef.ensure() : parent.getProject();
    }

}
