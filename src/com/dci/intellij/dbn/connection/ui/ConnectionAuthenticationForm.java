package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class ConnectionAuthenticationForm extends DBNFormImpl<ConnectionAuthenticationDialog>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPasswordField passwordField;
    private JTextField userTextField;
    private JCheckBox emptyPasswordCheckBox;
    private JCheckBox osAuthenticationCheckBox;
    private JPanel hintPanel;

    private String cachedUser = "";
    private String cachedPassword = "";

    public ConnectionAuthenticationForm(@NotNull final ConnectionAuthenticationDialog parentComponent, final @Nullable ConnectionHandler connectionHandler) {
        super(parentComponent);

        AuthenticationInfo authenticationInfo = parentComponent.getAuthenticationInfo();

        String user = authenticationInfo.getUser();
        if (StringUtil.isNotEmpty(user)) {
            userTextField.setText(user);
            cachedUser = user;
        }

        boolean isEmptyPassword = authenticationInfo.isEmptyPassword();
        emptyPasswordCheckBox.setSelected(isEmptyPassword);
        passwordField.setEnabled(!isEmptyPassword);
        passwordField.setBackground(isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());

        String hintText;
        if (connectionHandler != null) {
            DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler, this);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

            int passwordExpiryTime = connectionHandler.getSettings().getDetailSettings().getPasswordExpiryTime();
            String expiryTimeText = passwordExpiryTime == 0 ? "0 - no expiry" :
                    passwordExpiryTime == 1 ? "1 minute" : passwordExpiryTime + " minutes";

            hintText = "The system needs your credentials to connect to this database. " +
                            "\nYou can configure how long the credentials stay active on idle connectivity " +
                            "in DBN Settings > Connection > Details (currently set to " + expiryTimeText + ")";

        } else {
            hintText = "The system needs your credentials to connect to this database.";
        }
        DBNHintForm hintForm = new DBNHintForm(hintText, null, true);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        updateAuthenticationFields();

        userTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String user = userTextField.getText();
                authenticationInfo.setUser(user);
                parentComponent.updateConnectButton();
            }
        });

        passwordField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String password = new String(passwordField.getPassword());
                authenticationInfo.setPassword(password);
                parentComponent.updateConnectButton();
            }
        });

        osAuthenticationCheckBox.addActionListener(e -> {
            authenticationInfo.setOsAuthentication(osAuthenticationCheckBox.isSelected());
            updateAuthenticationFields();
            parentComponent.updateConnectButton();
        });

        emptyPasswordCheckBox.addActionListener(e -> {
            authenticationInfo.setEmptyPassword(emptyPasswordCheckBox.isSelected());
            updateAuthenticationFields();
            parentComponent.updateConnectButton();
        });
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

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return StringUtil.isEmpty(userTextField.getText()) ? userTextField : passwordField;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
