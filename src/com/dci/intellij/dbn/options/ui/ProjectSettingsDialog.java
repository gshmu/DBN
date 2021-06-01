package com.dci.intellij.dbn.options.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettings;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.help.HelpManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ProjectSettingsDialog extends DBNDialog<ProjectSettingsForm> {
    private JButton bApply;
    private final ProjectSettings projectSettings;

    public ProjectSettingsDialog(Project project) {
        super(project, project.isDefault() ? "Default Settings" : "Settings", true);
        setModal(true);
        setResizable(true);
        //setHorizontalStretch(1.5f);

        ProjectSettings projectSettings = ProjectSettingsManager.getSettings(project);
        this.projectSettings = projectSettings.clone();
        this.projectSettings.createCustomComponent();
        init();
    }

    @NotNull
    @Override
    protected ProjectSettingsForm createForm() {
        return projectSettings.ensureSettingsEditor();
    }

    public ProjectSettings getProjectSettings() {
        return projectSettings;
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
                getOKAction(),
                getCancelAction(),
                new ApplyAction(),
                getHelpAction()
        };
    }

    @Override
    protected JButton createJButtonForAction(Action action) {
        if (action instanceof ApplyAction) {
            bApply = new JButton();
            bApply.setAction(action);
            bApply.setEnabled(false);
            return bApply;
        }
        return super.createJButtonForAction(action);
    }

    @Override
    public void doCancelAction() {
        //projectSettings.reset();
        super.doCancelAction();
        projectSettings.disposeUIResources();
    }

    @Override
    public void doOKAction() {
        try {
            projectSettings.apply();
            super.doOKAction();
            projectSettings.disposeUIResources();
        } catch (ConfigurationException e) {
            MessageUtil.showErrorDialog(getProject(), e.getMessage());
        }

    }

    public void doApplyAction() {
        try {
            projectSettings.apply();
            bApply.setEnabled(false);
            setCancelButtonText("Close");
        } catch (ConfigurationException e) {
            MessageUtil.showErrorDialog(getProject(), e.getTitle(), e.getMessage());
        }
    }

    @Override
    protected void doHelpAction() {
        HelpManager.getInstance().invokeHelp(projectSettings.getHelpTopic());
    }

    private class ApplyAction extends AbstractAction {
        private final Alarm alarm = new Alarm(ProjectSettingsDialog.this);
        private final Runnable reloader = new Runnable() {
            @Override
            public void run() {
                if (isShowing()) {
                    boolean isModified = projectSettings.isModified();
                    bApply.setEnabled(isModified);
                    //setCancelButtonText(isModified ? "Cancel" : "Close");
                    addReloadRequest();
                }
            }
        };

        private void addReloadRequest() {
            alarm.addRequest(reloader, 500, ModalityState.stateForComponent(getWindow()));
        }

        public ApplyAction() {
            putValue(Action.NAME, "Apply");
            putValue(DEFAULT_ACTION, Boolean.FALSE);
            addReloadRequest();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            doApplyAction();
        }
    }

    public void selectConnectionSettings(@Nullable ConnectionId connectionId) {
        ProjectSettingsForm settingsEditor = projectSettings.getSettingsEditor();
        if (settingsEditor != null) {
            settingsEditor.selectConnectionSettings(connectionId);
        }
    }

    public void selectSettings(ConfigId configId) {
        ProjectSettingsForm globalSettingsEditor = projectSettings.getSettingsEditor();
        if (globalSettingsEditor != null) {
            globalSettingsEditor.selectSettingsEditor(configId);
        }
    }
}
