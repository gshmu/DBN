package com.dci.intellij.dbn.connection.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.intellij.ui.DottedBorder;
import com.intellij.util.ui.UIUtil;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;

public class ConnectionListCellRenderer extends DefaultListCellRenderer{
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        ConnectionHandler connectionHandler = (ConnectionHandler) value;
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus );
        String name = connectionHandler.getQualifiedName();

        ConnectivityStatus connectivityStatus = connectionHandler.getSettings().getDatabaseSettings().getConnectivityStatus();
        boolean active = connectionHandler.isActive();

        Icon icon = Icons.CONNECTION_DISABLED;
        if (active) {
            icon = connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
                   connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;
        }

        label.setIcon(icon);
        label.setText(name);
        if (!cellHasFocus && isSelected) {
            label.setForeground(list.getForeground());
            label.setBackground(list.hasFocus() ? list.getBackground() : UIUtil.getFocusedFillColor());
            label.setBorder(new DottedBorder(Color.BLACK));
        }
        return label;
    }
}
