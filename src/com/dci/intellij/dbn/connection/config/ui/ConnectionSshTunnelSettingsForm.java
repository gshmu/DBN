package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.connection.config.ConnectionSshTunnelSettings;
import com.dci.intellij.dbn.connection.ssh.SshAuthType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.getSelection;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.initComboBox;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.setSelection;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class ConnectionSshTunnelSettingsForm extends ConfigurationEditorForm<ConnectionSshTunnelSettings> {
    private JPanel mainPanel;
    private JPanel sshGroupPanel;
    private JTextField hostTextField;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JTextField portTextField;
    private JCheckBox activeCheckBox;
    private JComboBox<SshAuthType> authTypeComboBox;
    private JPasswordField keyPassphraseField;
    private TextFieldWithBrowseButton keyFileField;
    private JLabel passwordLabel;
    private JLabel privateKeyFileLabel;
    private JLabel privateKeyPassphraseLabel;

    public ConnectionSshTunnelSettingsForm(final ConnectionSshTunnelSettings configuration) {
        super(configuration);

        updateBorderTitleForeground(sshGroupPanel);
        initComboBox(authTypeComboBox, SshAuthType.values());
        resetFormChanges();

        authTypeComboBox.addActionListener(e -> showHideFields());

        enableDisableFields();
        showHideFields();
        registerComponent(mainPanel);

        keyFileField.addBrowseFolderListener(
                "Select Private Key file",
                "",
                null, new FileChooserDescriptor(true, false, false, false, false, false));
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            Object source = e.getSource();

            if (source == activeCheckBox) {
                enableDisableFields();
            }
        };
    }

    private void showHideFields() {
        boolean isKeyPair = getSelection(authTypeComboBox) == SshAuthType.KEY_PAIR;
        passwordField.setVisible(!isKeyPair);
        passwordLabel.setVisible(!isKeyPair);

        privateKeyFileLabel.setVisible(isKeyPair);
        privateKeyPassphraseLabel.setVisible(isKeyPair);
        keyFileField.setVisible(isKeyPair);
        keyPassphraseField.setVisible(isKeyPair);
    }

    private void enableDisableFields() {
        boolean enabled = activeCheckBox.isSelected();
        hostTextField.setEnabled(enabled);
        portTextField.setEnabled(enabled);
        userTextField.setEnabled(enabled);
        authTypeComboBox.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        passwordField.setBackground(enabled ? UIUtil.getTextFieldBackground() : UIUtil.getPanelBackground());
        keyFileField.setEnabled(enabled);
        keyPassphraseField.setEnabled(enabled);
        keyPassphraseField.setBackground(enabled ? UIUtil.getTextFieldBackground() : UIUtil.getPanelBackground());

    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConnectionSshTunnelSettings configuration = getConfiguration();
        applyFormChanges(configuration);
    }

    @Override
    public void applyFormChanges(ConnectionSshTunnelSettings configuration) throws ConfigurationException {
        boolean enabled = activeCheckBox.isSelected();
        configuration.setActive(enabled);
        configuration.setHost(ConfigurationEditorUtil.validateStringInputValue(hostTextField, "Host", enabled));
        ConfigurationEditorUtil.validateIntegerInputValue(portTextField, "Port", enabled, 0, 999999, null);
        configuration.setPort(portTextField.getText());
        configuration.setUser(userTextField.getText());
        SshAuthType authType = getSelection(authTypeComboBox);

        boolean isKeyPair = authType == SshAuthType.KEY_PAIR;
        ConfigurationEditorUtil.validateStringInputValue(keyFileField.getTextField(), "Key file", enabled && isKeyPair);
        //ConfigurationEditorUtil.validateStringInputValue(keyPassphraseField, "Key passphrase", enabled && isKeyPair);

        configuration.setAuthType(authType);
        configuration.setPassword(String.valueOf(passwordField.getPassword()));
        configuration.setKeyFile(keyFileField.getText());
        configuration.setKeyPassphrase(String.valueOf(keyPassphraseField.getPassword()));
    }

    @Override
    public void resetFormChanges() {
        ConnectionSshTunnelSettings configuration = getConfiguration();
        activeCheckBox.setSelected(configuration.isActive());
        hostTextField.setText(configuration.getHost());
        portTextField.setText(configuration.getPort());
        userTextField.setText(configuration.getUser());
        passwordField.setText(configuration.getPassword());
        setSelection(authTypeComboBox, configuration.getAuthType());
        keyFileField.setText(configuration.getKeyFile());
        keyPassphraseField.setText(configuration.getKeyPassphrase());
    }
}
