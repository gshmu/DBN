package com.dci.intellij.dbn.common.ui.dialog;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public abstract class DBNDialog<C extends DBNForm> extends DialogWrapper implements DisposableProjectComponent{
    private C component;
    private Project project;
    private boolean disposed;
    private boolean rememberSelection;

    protected DBNDialog(Project project, String title, boolean canBeParent) {
        super(project, canBeParent);
        setTitle(Constants.DBN_TITLE_PREFIX + title);
        this.project = project;
    }

    @NotNull
    public final C getComponent() {
        if (component == null && !isDisposed()) {
            component = createComponent();
            DisposerUtil.register(this, component);
        }
        return FailsafeUtil.get(component);
    }

    @NotNull
    protected final JComponent createCenterPanel() {
        return getComponent().getComponent();
    }

    protected abstract @NotNull C createComponent();

    protected String getDimensionServiceKey() {
        return "DBNavigator." + getClass().getSimpleName();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        if (!isDisposed()) {
            JComponent focusComponent = getComponent().getPreferredFocusedComponent();
            return focusComponent == null ? super.getPreferredFocusedComponent() : focusComponent;
        }
        return null;
    }

    @NotNull
    public Project getProject() {
        return FailsafeUtil.get(project);
    }

    public boolean isRememberSelection() {
        return rememberSelection;
    }

    public void registerRememberSelectionCheckBox(final JCheckBox rememberSelectionCheckBox) {
        rememberSelectionCheckBox.addActionListener(e -> rememberSelection = rememberSelectionCheckBox.isSelected());
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            component = null;
            project = null;
            super.dispose();
        }
    }

    public boolean isDisposed() {
        return disposed;
    }
}
