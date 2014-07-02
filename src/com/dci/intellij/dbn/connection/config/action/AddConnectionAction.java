package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.GenericConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.JList;

public class AddConnectionAction extends DumbAwareAction {
    protected ConnectionBundle connectionBundle;
    protected JList list;

    public AddConnectionAction(JList list, ConnectionBundle connectionBundle) {
        super("Add connection", null, Icons.ACTION_ADD);
        this.connectionBundle = connectionBundle;
        this.list = list;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionBundle.setModified(true);
        ConnectionSettings connectionSettings = new ConnectionSettings(connectionBundle);
        connectionSettings.getDatabaseSettings().setNew(true);
        GenericConnectionDatabaseSettings connectionConfig = (GenericConnectionDatabaseSettings) connectionSettings.getDatabaseSettings();
        connectionConfig.generateNewId();

        String name = "Connection";
        ConnectionListModel model = (ConnectionListModel) list.getModel();
        while (model.getConnectionConfig(name) != null) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        connectionConfig.setName(name);
        int selectedIndex = list.getSelectedIndex() + 1;
        model.add(selectedIndex, connectionSettings);
        list.setSelectedIndex(selectedIndex);
    }
}
