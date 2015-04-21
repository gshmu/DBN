package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.connection.config.ConnectionSshSslSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.UIUtil;

public class ConnectionSshSslSettingsForm extends ConfigurationEditorForm<ConnectionSshSslSettings>{
    private JPanel mainPanel;
    private JPanel sshGroupPanel;
    private JTextField hostTextField;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JTextField portTextField;
    private JCheckBox activeCheckBox;

    public ConnectionSshSslSettingsForm(final ConnectionSshSslSettings configuration) {
        super(configuration);

        updateBorderTitleForeground(sshGroupPanel);

        resetFormChanges();
        enableDisableFields();
        registerComponent(mainPanel);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                ConnectionSshSslSettings configuration = getConfiguration();
                configuration.setModified(true);

                if (source == activeCheckBox) {
                    enableDisableFields();
                }
            }
        };
    }

    private void enableDisableFields() {
        boolean enabled = activeCheckBox.isSelected();
        hostTextField.setEnabled(enabled);
        portTextField.setEnabled(enabled);
        userTextField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        passwordField.setBackground(enabled ? UIUtil.getTextFieldBackground() : UIUtil.getPanelBackground());
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionSshSslSettings configuration = getConfiguration();
        boolean enabled = activeCheckBox.isSelected();
        configuration.setActive(enabled);
        configuration.setHost(ConfigurationEditorUtil.validateStringInputValue(hostTextField, "Host", enabled));
        int port = ConfigurationEditorUtil.validateIntegerInputValue(portTextField, "Port", enabled, 0, 999999, null);
        configuration.setPort(Integer.toString(port));
        configuration.setUser(userTextField.getText());
        configuration.setPassword(new String(passwordField.getPassword()));

    }

    @Override
    public void resetFormChanges() {
        ConnectionSshSslSettings configuration = getConfiguration();
        activeCheckBox.setSelected(configuration.isActive());
        hostTextField.setText(configuration.getHost());
        portTextField.setText(configuration.getPort());
        userTextField.setText(configuration.getUser());
        passwordField.setText(configuration.getPassword());
    }


    @Override
    public void dispose() {
        super.dispose();
    }
}
