package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.event.ProjectEventAdapter;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DBNFormImpl
        extends StatefulDisposable.Base
        implements DBNForm, NotificationSupport, ProjectEventAdapter {

    private ProjectRef project;
    private WeakRef<DBNComponent> parentComponent;
    private boolean registeredDataProvider;

    @Deprecated // no Disposer hierarchy
    protected DBNFormImpl() {
    }

    protected DBNFormImpl(@NotNull DBNComponent parent) {
        this.parentComponent = WeakRef.of(parent);
        Disposer.register(parent, this);
    }

    protected DBNFormImpl(@NotNull Project project) {
        this.project = ProjectRef.of(project);
    }

    @NotNull
    @Override
    public final JComponent getComponent() {
        JComponent component = getMainComponent();
        if (!registeredDataProvider) {
            registeredDataProvider = true;
            DataManager.registerDataProvider(component, this);
        }
        return component;
    }

    protected abstract JComponent getMainComponent();

    public EnvironmentSettings getEnvironmentSettings(Project project) {
        return GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
    }

    @Nullable
    public <T extends DBNComponent> T getParentComponent() {
        return (T) WeakRef.get(parentComponent);
    }

    public void setParentComponent(@NotNull DBNComponent parentComponent) {
        this.parentComponent = WeakRef.of(parentComponent);
        Disposer.register(parentComponent, this);
    }

    @Override
    @NotNull
    public final Project getProject() {
        if (project != null) {
            return project.ensure();
        }

        if (parentComponent != null) {
            return parentComponent.ensure().getProject();
        }

        DataContext dataContext = DataManager.getInstance().getDataContext(getComponent());
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        return Failsafe.nn(project);
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        return null;
    }

    @Override
    @Nullable
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    protected void disposeInner() {
        JComponent component = getComponent();
        DataManager.removeDataProvider(component);
        SafeDisposer.dispose(component);
        nullify();
    }
}
