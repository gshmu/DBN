package com.dci.intellij.dbn.connection.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.ui.DocumentAdapter;

public class ConnectionPasswordForm extends DBNFormImpl<ConnectionPasswordDialog>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JCheckBox rememberPasswordCheckBox;
    private JPasswordField passwordField;

    public ConnectionPasswordForm(@NotNull final ConnectionPasswordDialog parentComponent, final @Nullable ConnectionHandler connectionHandler) {
        super(parentComponent);
        if (connectionHandler != null) {
            DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler);
            headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        } else {
            rememberPasswordCheckBox.setVisible(false);
        }

        passwordField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String password = new String(passwordField.getPassword());
                parentComponent.setPassword(password);
            }
        });

        rememberPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parentComponent.setRememberPassword(rememberPasswordCheckBox.isSelected());
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
        return passwordField;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
