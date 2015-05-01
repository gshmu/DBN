package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionAuthenticationForm extends DBNFormImpl<ConnectionAuthenticationDialog>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPasswordField passwordField;
    private JTextArea hintTextArea;
    private JTextField userTextField;
    private JCheckBox emptyPasswordCheckBox;

    public ConnectionAuthenticationForm(@NotNull final ConnectionAuthenticationDialog parentComponent, final @Nullable ConnectionHandler connectionHandler) {
        super(parentComponent);
        hintTextArea.setBackground(mainPanel.getBackground());
        hintTextArea.setFont(mainPanel.getFont());

        final Authentication authentication = parentComponent.getAuthentication();

        String user = authentication.getUser();
        if (StringUtil.isNotEmpty(user)) {
            userTextField.setText(user);
        }
        boolean isEmptyPassword = authentication.isEmptyPassword();
        emptyPasswordCheckBox.setSelected(isEmptyPassword);
        passwordField.setEnabled(!isEmptyPassword);
        passwordField.setBackground(isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());

        if (connectionHandler != null) {
            DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

            int passwordExpiryTime = connectionHandler.getSettings().getDetailSettings().getPasswordExpiryTime();
            String expiryTimeText = passwordExpiryTime == 0 ? "0 - no expiry" :
                    passwordExpiryTime == 1 ? "1 minute" : passwordExpiryTime + " minutes";

            hintTextArea.setText(
                    StringUtil.wrap("The system needs your credentials to connect to this database. " +
                            "\nYou can configure how long the credentials stay active on idle connectivity " +
                            "in \nDBN Settings > Connection > Details (currently set to " + expiryTimeText + ")", 90, ": ,."));

        } else {
            hintTextArea.setText("The system needs your credentials to connect to this database.");
        }

        userTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String user = userTextField.getText();
                authentication.setUser(user);
                parentComponent.updateConnectButton();
            }
        });

        passwordField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {String password = new String(passwordField.getPassword());
                authentication.setPassword(password);
                parentComponent.updateConnectButton();
            }
        });

        emptyPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isEmptyPassword = emptyPasswordCheckBox.isSelected();
                passwordField.setEnabled(!isEmptyPassword);
                passwordField.setBackground(isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());
                authentication.setEmptyPassword(isEmptyPassword);
                parentComponent.updateConnectButton();
            }
        });
    }

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
