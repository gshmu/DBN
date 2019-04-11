package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@Nullifiable
public abstract class DBNFormImpl<P extends DisposableProjectComponent> extends DisposableBase implements DBNForm, NotificationSupport {
    private ProjectRef projectRef;
    private WeakRef<P> parentComponent;
    private boolean registeredDataProvider;

    public DBNFormImpl() {
    }


    public DBNFormImpl(@NotNull P parentComponent) {
        super(parentComponent);
        this.parentComponent = WeakRef.from(parentComponent);
    }

    public DBNFormImpl(Project project) {
        this.projectRef = ProjectRef.from(project);
    }

    @NotNull
    @Override
    public final JComponent getComponent() {
        JComponent component = ensureComponent();
        if (!registeredDataProvider) {
            registeredDataProvider = true;
            DataManager.registerDataProvider(getComponent(), this);
        }
        return component;
    }

    protected abstract JComponent ensureComponent();

    public EnvironmentSettings getEnvironmentSettings(Project project) {
        return GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
    }

    @Nullable
    public P getParentComponent() {
        return parentComponent == null ? null : parentComponent.get();
    }

    public void setParentComponent(P parentComponent) {
        this.parentComponent = WeakRef.from(parentComponent);
    }

    @NotNull
    public P ensureParentComponent() {
        return Failsafe.nn(getParentComponent());
    }

    @Override
    @NotNull
    public final Project getProject() {
        if (projectRef != null) {
            return projectRef.nn();
        }

        if (parentComponent != null) {
            return parentComponent.nn().getProject();
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
    public void disposeInner() {
        JComponent component = getComponent();
        DataManager.removeDataProvider(component);
        Disposer.dispose(component);
        super.disposeInner();
    }
}
