package com.dci.intellij.dbn.connection.console.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.util.Set;
import org.jetbrains.generate.tostring.util.StringUtil;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;

public class CreateConsoleForm extends DBNFormImpl {
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JTextField consoleNameTextField;
    private JLabel errorLabel;
    private ConnectionHandler connectionHandler;

    public CreateConsoleForm(final CreateConsoleDialog createConsoleDialog, ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorLabel.setVisible(false);

        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        final Set<String> consoleNames = connectionHandler.getConsoleNames();

        String name = connectionHandler.getName() + "1";
        while (consoleNames.contains(name)) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        consoleNameTextField.setText(name);

        consoleNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String errorText = null;
                String text = consoleNameTextField.getText();

                if (StringUtil.isEmpty(text)) {
                    errorText = "Console name must be specified";
                }
                else if (consoleNames.contains(text.trim())) {
                    errorText = "Console name already in use";
                }


                errorLabel.setVisible(errorText != null);
                createConsoleDialog.getOKAction().setEnabled(errorText == null);
                if (errorText != null) {
                    errorLabel.setText(errorText);
                }
            }
        });
    }

    public String getConsoleName() {
        return consoleNameTextField.getText();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
