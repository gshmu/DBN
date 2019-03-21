package com.dci.intellij.dbn.connection.console.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Set;

public class CreateRenameConsoleForm extends DBNFormImpl<CreateRenameConsoleDialog>{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JTextField consoleNameTextField;
    private JLabel errorLabel;

    private ConnectionHandlerRef connectionHandlerRef;
    private DBConsoleVirtualFile console;
    private DBConsoleType consoleType;

    public CreateRenameConsoleForm(final CreateRenameConsoleDialog parentComponent, @NotNull ConnectionHandler connectionHandler, @Nullable final DBConsoleVirtualFile console, DBConsoleType consoleType) {
        super(parentComponent);
        this.connectionHandlerRef = connectionHandler.getRef();
        this.console = console;
        this.consoleType = consoleType;
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorLabel.setVisible(false);

        DBNHeaderForm headerForm = console == null ?
                new DBNHeaderForm("[New " + consoleType.getName() + "]", consoleType.getIcon(), connectionHandler.getEnvironmentType().getColor(), this) :
                new DBNHeaderForm(console, this);
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
            parentComponent.getOKAction().setEnabled(false);
        }
        consoleNameTextField.setText(name);

        consoleNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String errorText = null;
                String text = StringUtil.trim(consoleNameTextField.getText());

                if (StringUtil.isEmpty(text)) {
                    errorText = "Console name must be specified";
                }
                else if (consoleNames.contains(text)) {
                    errorText = "Console name already in use";
                }


                errorLabel.setVisible(errorText != null);
                parentComponent.getOKAction().setEnabled(errorText == null && (console == null || !console.getName().equals(text)));
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
        return console == null ? consoleType : console.getType();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    public DBConsoleVirtualFile getConsole() {
        return console;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

}
