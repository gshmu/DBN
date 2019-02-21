package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class IdleConnectionDialogForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JTextPane hintTextPane;

    public IdleConnectionDialogForm(ConnectionHandler connectionHandler, DBNConnection connection, int timeoutMinutes) {
        int idleMinutes = connection.getIdleMinutes();
        int idleMinutesToDisconnect = connectionHandler.getSettings().getDetailSettings().getIdleTimeToDisconnect();

        String text = "The connection \"" + connectionHandler.getConnectionName(connection) + "\" is been idle for more than " + idleMinutes + " minutes. You have uncommitted changes on this connection. " +
                "Please specify whether to commit or rollback the changes. You can choose to keep the connection alive for another " + idleMinutesToDisconnect + " more minutes. \n\n" +
                "NOTE: Connection will close automatically and changes will be rolled-back if this prompt stays unattended for more than " + timeoutMinutes + " minutes.";
        hintTextPane.setBackground(mainPanel.getBackground());
        hintTextPane.setFont(mainPanel.getFont());
        hintTextPane.setText(text);


        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
