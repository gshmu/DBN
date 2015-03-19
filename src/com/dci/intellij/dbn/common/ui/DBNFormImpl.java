package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public abstract class DBNFormImpl<P extends DisposableProjectComponent> extends GUIUtil implements DBNForm {
    private boolean disposed;
    private Project project;
    private P parentComponent;

    public DBNFormImpl() {
    }

    public DBNFormImpl(@NotNull P parentComponent) {
        Disposer.register(parentComponent, this);
        this.parentComponent = parentComponent;
    }

    public DBNFormImpl(Project project) {
        this.project = project;
    }

    public EnvironmentSettings getEnvironmentSettings(Project project) {
        return GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
    }

    public P getParentComponent() {
        return parentComponent;
    }

    @NotNull
    public final Project getProject() {
        if (project != null) {
            return project;
        }

        if (parentComponent != null) {
            return parentComponent.getProject();
        }

        DataContext dataContext = DataManager.getInstance().getDataContext(getComponent());
        Project project = DataKeys.PROJECT.getData(dataContext);
        if (project == null || project.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return project;
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public void dispose() {
        disposed = true;
        project = null;
        parentComponent = null;
    }


}
