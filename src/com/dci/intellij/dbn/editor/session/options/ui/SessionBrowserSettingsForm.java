package com.dci.intellij.dbn.editor.session.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.session.options.SessionBrowserSettings;
import com.dci.intellij.dbn.editor.session.options.SessionInterruptionOption;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class SessionBrowserSettingsForm extends ConfigurationEditorForm<SessionBrowserSettings> {
    private JPanel mainPanel;
    private JComboBox<SessionInterruptionOption> disconnectSessionComboBox;
    private JComboBox<SessionInterruptionOption> killSessionComboBox;
    private JCheckBox reloadOnFilterChangeCheckBox;

    public SessionBrowserSettingsForm(SessionBrowserSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        initComboBox(disconnectSessionComboBox,
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.IMMEDIATE,
                SessionInterruptionOption.POST_TRANSACTION);

        initComboBox(killSessionComboBox,
                SessionInterruptionOption.ASK,
                SessionInterruptionOption.NORMAL,
                SessionInterruptionOption.IMMEDIATE);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        SessionBrowserSettings settings = getConfiguration();
        settings.getDisconnectSession().set(getSelection(disconnectSessionComboBox));
        settings.getKillSession().set(getSelection(killSessionComboBox));
        settings.setReloadOnFilterChange(reloadOnFilterChangeCheckBox.isSelected());
    }

    @Override
    public void resetFormChanges() {
        SessionBrowserSettings settings = getConfiguration();
        setSelection(disconnectSessionComboBox, settings.getDisconnectSession().get());
        setSelection(killSessionComboBox, settings.getKillSession().get());
        reloadOnFilterChangeCheckBox.setSelected(settings.isReloadOnFilterChange());
    }
}
