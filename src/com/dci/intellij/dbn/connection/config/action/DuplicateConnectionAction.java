package com.dci.intellij.dbn.connection.config.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionListModel;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

import javax.swing.JList;

public class DuplicateConnectionAction extends DumbAwareAction {
    protected ConnectionBundle connectionBundle;
    protected JList list;

    public DuplicateConnectionAction(JList list, ConnectionBundle connectionBundle) {
        super("Duplicate connection", null, Icons.ACTION_COPY);
        this.list = list;
        this.connectionBundle = connectionBundle;
    }

    public void actionPerformed(AnActionEvent anActionEvent) {
        connectionBundle.setModified(true);
        ConnectionSettings connectionSettings = (ConnectionSettings) list.getSelectedValue();
        ConnectionListModel model = (ConnectionListModel) list.getModel();
        ConnectionSettings clone = connectionSettings.clone();
        clone.getDatabaseSettings().setNew(true);
        String name = clone.getDatabaseSettings().getName();
        while (model.getConnectionConfig(name) != null) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        clone.getDatabaseSettings().setName(name);
        int selectedIndex = list.getSelectedIndex() + 1;
        model.add(selectedIndex, clone);
        list.setSelectedIndex(selectedIndex);
    }

    public void update(AnActionEvent e) {
        int length = list.getSelectedValues().length;
        e.getPresentation().setEnabled(length == 1);
    }
}
