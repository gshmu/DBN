package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.credentials.DatabaseCredentialManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ConnectionAuthenticationSettingsForm extends DBNFormImpl<ConnectionDatabaseSettingsForm> {
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox emptyPasswordCheckBox;
    private JPanel mainPanel;

    private String cachedUser = "";
    private String cachedPassword = "";

    private final ActionListener actionListener = e -> updateAuthenticationFields();

    public ConnectionAuthenticationSettingsForm(@NotNull ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);
        osAuthenticationCheckBox.addActionListener(actionListener);
        emptyPasswordCheckBox.addActionListener(actionListener);
    }

    protected void updateAuthenticationFields() {
        boolean isOsAuthentication = osAuthenticationCheckBox.isSelected();
        boolean isEmptyPassword = emptyPasswordCheckBox.isSelected();
        userTextField.setEnabled(!isOsAuthentication);

        passwordField.setEnabled(!isOsAuthentication && !emptyPasswordCheckBox.isSelected());
        passwordField.setBackground(isOsAuthentication || isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());
        emptyPasswordCheckBox.setEnabled(!isOsAuthentication);

        String user = userTextField.getText();
        String password = new String(passwordField.getPassword());
        if (StringUtil.isNotEmpty(user)) cachedUser = user;
        if (StringUtil.isNotEmpty(password)) cachedPassword = password;

        if (isOsAuthentication || isEmptyPassword) {
            passwordField.setText("");
        } else {
            passwordField.setText(cachedPassword);
        }

        if (isOsAuthentication) {
            userTextField.setText("");
            emptyPasswordCheckBox.setSelected(false);
        } else {
            userTextField.setText(cachedUser);
        }
    }

    public JTextField getUserTextField() {
        return userTextField;
    }

    public void applyFormChanges(AuthenticationInfo authenticationInfo){
        String oldUserName = authenticationInfo.getUser();
        String oldPassword = authenticationInfo.getPassword();
        String newUserName = userTextField.getText();
        String newPassword = new String(passwordField.getPassword());
        authenticationInfo.setUser(newUserName);
        authenticationInfo.setPassword(newPassword);
        authenticationInfo.setOsAuthentication(osAuthenticationCheckBox.isSelected());
        authenticationInfo.setEmptyPassword(emptyPasswordCheckBox.isSelected());

        if (!Configuration.IS_TRANSITORY.get() && !CommonUtil.safeEqual(oldUserName, newUserName) || !CommonUtil.safeEqual(oldPassword, newPassword)) {
            DatabaseCredentialManager credentialManager = DatabaseCredentialManager.getInstance();
            ConnectionId connectionId = authenticationInfo.getConnectionId();
            credentialManager.removePassword(connectionId, oldUserName);
            credentialManager.setPassword(connectionId, newUserName, newPassword, false);
        }

    }

    public void resetFormChanges(AuthenticationInfo authenticationInfo) {
        String user = authenticationInfo.getUser();
        String password = authenticationInfo.getPassword();
        if (StringUtil.isNotEmpty(user)) cachedUser = user;
        if (StringUtil.isNotEmpty(password)) cachedPassword = password;

        userTextField.setText(authenticationInfo.getUser());
        passwordField.setText(authenticationInfo.getPassword());
        osAuthenticationCheckBox.setSelected(authenticationInfo.isOsAuthentication());
        emptyPasswordCheckBox.setSelected(authenticationInfo.isEmptyPassword());
        updateAuthenticationFields();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
