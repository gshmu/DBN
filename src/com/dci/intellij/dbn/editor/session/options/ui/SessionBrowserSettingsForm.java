package com.dci.intellij.dbn.editor.session.options.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.option.ui.InteractiveOptionComboBoxRenderer;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.intellij.openapi.options.ConfigurationException;

public class SessionBrowserSettingsForm extends ConfigurationEditorForm<SessionBrowserSettings> {
    private JPanel mainPanel;
    private JComboBox<SessionInterruptionOption> disconnectSessionComboBox;
    private JComboBox<SessionInterruptionOption> killSessionComboBox;

    public SessionBrowserSettingsForm(SessionBrowserSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        disconnectSessionComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        disconnectSessionComboBox.addItem(SessionInterruptionOption.ASK);
        disconnectSessionComboBox.addItem(SessionInterruptionOption.IMMEDIATE);
        disconnectSessionComboBox.addItem(SessionInterruptionOption.POST_TRANSACTION);

        killSessionComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        killSessionComboBox.addItem(SessionInterruptionOption.ASK);
        killSessionComboBox.addItem(SessionInterruptionOption.NORMAL);
        killSessionComboBox.addItem(SessionInterruptionOption.IMMEDIATE);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        SessionBrowserSettings settings = getConfiguration();
        settings.getDisconnectSessionOptionHandler().setSelectedOption((SessionInterruptionOption) disconnectSessionComboBox.getSelectedItem());
        settings.getKillSessionOptionHandler().setSelectedOption((SessionInterruptionOption) killSessionComboBox.getSelectedItem());
    }

    public void resetFormChanges() {
        SessionBrowserSettings settings = getConfiguration();
        disconnectSessionComboBox.setSelectedItem(settings.getDisconnectSessionOptionHandler().getSelectedOption());
        killSessionComboBox.setSelectedItem(settings.getKillSessionOptionHandler().getSelectedOption());

    }
}
