package com.dci.intellij.dbn.common.options;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.project.ProjectStateManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public abstract class CompositeProjectConfiguration<P extends ProjectConfiguration, E extends CompositeConfigurationEditorForm>
        extends CompositeConfiguration<P, E>
        implements ProjectConfiguration<P, E> {

    private ProjectRef project;

    public CompositeProjectConfiguration(P parent) {
        super(parent);
        Disposer.register(parent, this);
    }

    public CompositeProjectConfiguration(@NotNull Project project) {
        super(null);
        this.project = ProjectRef.of(project);
        ProjectStateManager.registerDisposable(project, this);
    }

    @NotNull
    @Override
    public Project getProject() {
        if (project != null) {
            return project.ensure();
        }

        return getParent().ensureProject();
    }
}
