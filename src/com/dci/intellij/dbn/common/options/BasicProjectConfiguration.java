package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class BasicProjectConfiguration<P extends ProjectConfiguration, E extends ConfigurationEditorForm>
        extends BasicConfiguration<P, E>
        implements ProjectConfiguration<P, E> {

    private ProjectRef projectRef;

    public BasicProjectConfiguration(P parent) {
        super(parent);
    }

    public BasicProjectConfiguration(Project project) {
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
