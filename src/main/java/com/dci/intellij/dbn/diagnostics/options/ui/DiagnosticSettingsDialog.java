package com.dci.intellij.dbn.diagnostics.options.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.Messages;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class DiagnosticSettingsDialog extends DBNDialog<DiagnosticSettingsForm> {

    public DiagnosticSettingsDialog(Project project) {
        super(project, "Diagnostic Settings", true);
        setModal(false);
        setResizable(true);
        setCancelButtonText("Cancel");
        init();
    }

    @NotNull
    @Override
    protected DiagnosticSettingsForm createForm() {
        return new DiagnosticSettingsForm(this);
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                getHelpAction()
        };
    }

    @Override
    protected void doOKAction() {
        DiagnosticSettingsForm settingsForm = getForm();
        try {
            settingsForm.applyFormChanges();
            super.doOKAction();
        } catch (ConfigurationException e) {
            Messages.showErrorDialog(getProject(), "Invalid Configuration", e.getMessage());
        }

    }
}
