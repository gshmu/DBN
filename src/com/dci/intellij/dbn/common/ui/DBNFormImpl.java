package com.dci.intellij.dbn.common.ui;

import javax.swing.JComponent;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.project.Project;

public abstract class DBNFormImpl extends GUIUtil implements DBNForm {
    boolean disposed;

    public EnvironmentSettings getEnvironmentSettings(Project project) {
        return GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
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
    }


}
