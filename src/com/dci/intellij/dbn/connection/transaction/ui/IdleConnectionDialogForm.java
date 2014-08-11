package com.dci.intellij.dbn.connection.transaction.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;

public class IdleConnectionDialogForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JTextArea hintTextArea;
    private JPanel headerPanel;

    public IdleConnectionDialogForm(ConnectionHandler connectionHandler, int timeoutMinutes) {
        int idleMinutes = connectionHandler.getIdleMinutes();
        int idleMinutesToDisconnect = connectionHandler.getSettings().getDetailSettings().getIdleTimeToDisconnect();

        String text = "The connection \"" + connectionHandler.getName()+ " \" is been idle for more than " + idleMinutes + " minutes. You have uncommitted changes on this connection. \n" +
                "Please specify whether to commit or rollback the changes. You can chose to keep the connection alive for " + idleMinutesToDisconnect + " more minutes. \n\n" +
                "NOTE: Connection will close automatically if this prompt stays unattended for more than " + timeoutMinutes + " minutes.";
        hintTextArea.setBackground(mainPanel.getBackground());
        hintTextArea.setFont(mainPanel.getFont());
        hintTextArea.setText(text);


        DBNHeaderForm headerForm = new DBNHeaderForm(connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
