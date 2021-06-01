package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DiagnosticsMonitorDialog extends DBNDialog<DiagnosticsMonitorForm> {

    public DiagnosticsMonitorDialog(Project project) {
        super(project, "Diagnostics Monitor", true);
        setModal(false);
        setResizable(true);
        setCancelButtonText("Close");
        init();
    }

    @NotNull
    @Override
    protected DiagnosticsMonitorForm createForm() {
        return new DiagnosticsMonitorForm(this);
    }

    @Override
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
}
