package com.dci.intellij.dbn.connection.resource.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.transaction.DatabaseTransactionManager;
import com.intellij.openapi.project.Project;

public class ResourceMonitorDialog extends DBNDialog<ResourceMonitorForm> {

    public ResourceMonitorDialog(Project project) {
        super(project, "Resource monitor", true);
        setModal(false);
        setResizable(true);
        setCancelButtonText("Close");
        init();
    }

    @NotNull
    @Override
    protected ResourceMonitorForm createComponent() {
        return new ResourceMonitorForm(this);
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private DatabaseTransactionManager getTransactionManager() {
        return DatabaseTransactionManager.getInstance(getProject());
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
        }
    }
}
