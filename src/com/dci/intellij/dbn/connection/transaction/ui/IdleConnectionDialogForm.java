package com.dci.intellij.dbn.connection.transaction.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Color;

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


        // HEADER
        String headerTitle = connectionHandler.getName();
        Icon headerIcon = connectionHandler.getIcon();
        Color headerBackground = UIUtil.getPanelBackground();
        if (getEnvironmentSettings(connectionHandler.getProject()).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = connectionHandler.getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
