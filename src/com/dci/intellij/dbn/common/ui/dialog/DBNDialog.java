package com.dci.intellij.dbn.common.ui.dialog;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@Nullifiable
public abstract class DBNDialog<C extends DBNForm> extends DialogWrapper implements DisposableProjectComponent{
    private C component;
    private ProjectRef projectRef;
    private boolean disposed;
    private boolean rememberSelection;

    protected DBNDialog(Project project, String title, boolean canBeParent) {
        super(project, canBeParent);
        setTitle(Constants.DBN_TITLE_PREFIX + title);
        projectRef = ProjectRef.from(project);
        getHelpAction().setEnabled(false);
    }

    @NotNull
    public final C getComponent() {
        if (component == null && !isDisposed()) {
            component = createComponent();
        }
        return Failsafe.nn(component);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    @NotNull
    protected final JComponent createCenterPanel() {
        return getComponent().getComponent();
    }

    protected abstract @NotNull C createComponent();

    @Override
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

    @Override
    protected void doHelpAction() {
        super.doHelpAction();
    }

    @Override
    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }

    public boolean isRememberSelection() {
        return rememberSelection;
    }

    public void registerRememberSelectionCheckBox(final JCheckBox rememberSelectionCheckBox) {
        rememberSelectionCheckBox.addActionListener(e -> rememberSelection = rememberSelectionCheckBox.isSelected());
    }

    @Override
    public void dispose() {
        DisposableProjectComponent.super.dispose();
    }

    public void disposeInner(){
        Disposer.dispose(component);
        super.dispose();
        DisposableProjectComponent.super.disposeInner();
    };

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void markDisposed() {
        disposed = true;
    }
}
