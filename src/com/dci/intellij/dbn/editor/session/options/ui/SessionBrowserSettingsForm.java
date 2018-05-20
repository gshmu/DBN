package com.dci.intellij.dbn.editor.session.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class SessionBrowserSettingsForm extends ConfigurationEditorForm<SessionBrowserSettings> {
    private JPanel mainPanel;
    private DBNComboBox<SessionInterruptionOption> disconnectSessionComboBox;
    private DBNComboBox<SessionInterruptionOption> killSessionComboBox;
    private JCheckBox reloadOnFilterChangeCheckBox;

    public SessionBrowserSettingsForm(SessionBrowserSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        disconnectSessionComboBox.setValues(
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.IMMEDIATE,
                SessionInterruptionOption.POST_TRANSACTION);

        killSessionComboBox.setValues(
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.NORMAL,
                SessionInterruptionOption.IMMEDIATE);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        SessionBrowserSettings settings = getConfiguration();
        settings.getDisconnectSession().set(disconnectSessionComboBox.getSelectedValue());
        settings.getKillSession().set(killSessionComboBox.getSelectedValue());
        settings.setReloadOnFilterChange(reloadOnFilterChangeCheckBox.isSelected());
    }

    public void resetFormChanges() {
        SessionBrowserSettings settings = getConfiguration();
        disconnectSessionComboBox.setSelectedValue(settings.getDisconnectSession().get());
        killSessionComboBox.setSelectedValue(settings.getKillSession().get());
        reloadOnFilterChangeCheckBox.setSelected(settings.isReloadOnFilterChange());
    }
}
