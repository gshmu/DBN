package com.dci.intellij.dbn.connection.console.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Set;

public class CreateRenameConsoleForm extends DBNFormImpl{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JTextField consoleNameTextField;
    private JLabel errorLabel;

    private final ConnectionHandlerRef connectionHandler;
    private final DBConsoleType consoleType;
    private final DBConsole console;

    CreateRenameConsoleForm(final CreateRenameConsoleDialog parent, @NotNull ConnectionHandler connectionHandler, @Nullable final DBConsole console, DBConsoleType consoleType) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();
        this.console = console;
        this.consoleType = consoleType;
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorLabel.setVisible(false);

        DBNHeaderForm headerForm = console == null ?
                new DBNHeaderForm(this, "[New " + consoleType.getName() + "]", consoleType.getIcon(), connectionHandler.getEnvironmentType().getColor()) :
                new DBNHeaderForm(this, console);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        final Set<String> consoleNames = connectionHandler.getConsoleBundle().getConsoleNames();

        String name;
        if (console == null) {
            name = connectionHandler.getName() + " 1";
            while (consoleNames.contains(name)) {
                name = NamingUtil.getNextNumberedName(name, true);
            }
        } else {
            name = console.getName();
            consoleNames.remove(name);
            parent.getOKAction().setEnabled(false);
        }
        consoleNameTextField.setText(name);

        consoleNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String errorText = null;
                String text = StringUtil.trim(consoleNameTextField.getText());

                if (StringUtil.isEmpty(text)) {
                    errorText = "Console name must be specified";
                }
                else if (consoleNames.contains(text)) {
                    errorText = "Console name already in use";
                }


                errorLabel.setVisible(errorText != null);
                parent.getOKAction().setEnabled(errorText == null && (console == null || !console.getName().equals(text)));
                if (errorText != null) {
                    errorLabel.setText(errorText);
                }
            }
        });
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return consoleNameTextField;
    }

    public String getConsoleName() {
        return consoleNameTextField.getText();
    }

    public DBConsoleType getConsoleType() {
        return console == null ? consoleType : console.getConsoleType();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    public DBConsole getConsole() {
        return console;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

}
