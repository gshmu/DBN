package com.dci.intellij.dbn.common.ui.component;

import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.project.ProjectSupplier;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public abstract class DBNComponentBase extends StatefulDisposable.Base implements DBNComponent {
    private final ProjectRef project;
    private WeakRef<Disposable> parent;

    public DBNComponentBase(@Nullable Disposable parent) {
        this.parent = WeakRef.of(parent);
        this.project = null;
        SafeDisposer.register(parent, this);
    }

    @Deprecated // load project from data context
    public DBNComponentBase(Disposable parent, @Nullable Project project) {
        this.parent = WeakRef.of(parent);
        this.project = ProjectRef.of(project);
        SafeDisposer.register(parent, this);
    }

    @Nullable
    @Override
    public final <T extends Disposable> T parent() {
        return (T) WeakRef.get(parent);
    }

    public final void setParent(Disposable parent) {
        this.parent = WeakRef.of(parent);
        SafeDisposer.register(parent, this);
    }

    @Override
    @Nullable
    public final Project getProject() {
        if (project != null) {
            return project.ensure();
        }

        if (this.parent != null) {
            Disposable parent = this.parent.ensure();

            if (parent instanceof ProjectSupplier) {
                ProjectSupplier component = (ProjectSupplier) parent;
                Project project = component.getProject();
                if (project != null) {
                    return project;
                }
            }
        }

        return Lookup.getProject(getComponent());
    }
}
