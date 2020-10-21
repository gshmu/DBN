package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class DBNFormImpl
        extends DBNComponent.Base
        implements DBNForm, NotificationSupport {

    private boolean registeredDataProvider;

    public DBNFormImpl(@Nullable Disposable parent) {
        super(parent);
    }

    public DBNFormImpl(@Nullable Disposable parent, @Nullable Project project) {
        super(parent, project);
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
