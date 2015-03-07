package com.dci.intellij.dbn.connection.info.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Action;
import javax.swing.JComponent;

public class ConnectionInfoDialog extends DBNDialog {
    private ConnectionInfoForm connectionInfoForm;

    public ConnectionInfoDialog(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject(), "Connection Information", true);
        connectionInfoForm = new ConnectionInfoForm(this, connectionHandler, true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

/*
    protected String getDimensionServiceKey() {
        return "DBNavigator.ConnectionIngfo";
    }      
*/

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{
            getCancelAction()
        };
    }


    @Nullable
    protected JComponent createCenterPanel() {
        return connectionInfoForm.getComponent();
    }
}
