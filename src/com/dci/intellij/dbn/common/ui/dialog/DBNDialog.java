package com.dci.intellij.dbn.common.ui.dialog;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import java.awt.Dimension;

public abstract class DBNDialog<F extends DBNForm> extends DialogWrapper implements DBNComponent {
    private F form;
    private final ProjectRef project;
    private boolean rememberSelection;
    private boolean disposed;
    private Dimension defaultSize;

    protected DBNDialog(Project project, String title, boolean canBeParent) {
        super(project, canBeParent);
        this.project = ProjectRef.of(project);
        setTitle(Constants.DBN_TITLE_DIALOG_SUFFIX + title);
        getHelpAction().setEnabled(false);
    }

    @Override
    protected void init() {
        if (defaultSize != null) {
            setSize(
                (int) defaultSize.getWidth(),
                (int) defaultSize.getHeight());
        }
        super.init();
    }

    public void setDefaultSize(int width, int height) {
        this.defaultSize = new Dimension(width, height);
    }

    @NotNull
    public final F getForm() {
        if (form == null && !isDisposed()) {
            form = createForm();
        }
        return Failsafe.nn(form);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    @NotNull
    protected final JComponent createCenterPanel() {
        return getComponent();
    }

    @NotNull
    protected abstract F createForm();

    @Nullable
    public final <T extends Disposable> T parent() {
        return null;
    }

    @NotNull
    @Override
    public final JComponent getComponent() {
        return getForm().getComponent();
    }

    @Override
    protected String getDimensionServiceKey() {
        return Diagnostics.isDialogSizingReset() ? null : "DBNavigator." + getClass().getSimpleName();
    }

    protected static void renameAction(@NotNull Action action, String name) {
        action.putValue(Action.NAME, name);
    }

    protected static void makeDefaultAction(@NotNull Action action) {
        action.putValue(DEFAULT_ACTION, Boolean.TRUE);
    }

    protected static void makeFocusAction(@NotNull Action action) {
        action.putValue(FOCUSED_ACTION, Boolean.TRUE);
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        JComponent focusComponent = null;
        if (!isDisposed()) {
            focusComponent = getForm().getPreferredFocusedComponent();
            if (focusComponent == null) {
                focusComponent = super.getPreferredFocusedComponent();

                if (focusComponent == null) {
                    Action okAction = getOKAction();
                    focusComponent = getButton(okAction);

                    if (focusComponent == null) {
                        Action cancelAction = getCancelAction();
                        focusComponent = getButton(cancelAction);
                    }
                }
            }

        }
        return focusComponent;
    }

    @Override
    protected void doHelpAction() {
        super.doHelpAction();
    }

    @Override
    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public boolean isRememberSelection() {
        return rememberSelection;
    }

    public void registerRememberSelectionCheckBox(final JCheckBox rememberSelectionCheckBox) {
        rememberSelectionCheckBox.addActionListener(e -> rememberSelection = rememberSelectionCheckBox.isSelected());
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public final void dispose() {
        if (!disposed) {
            disposed = true;
            super.dispose();
            SafeDisposer.dispose(form, false, true);
            disposeInner();
            nullify();
        }
    }

    protected void disposeInner() {
    }


}
