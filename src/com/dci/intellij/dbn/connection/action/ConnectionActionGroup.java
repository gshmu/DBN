package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.action.AutoCommitToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.AutoConnectToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.DatabaseLoggingToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.PendingTransactionsOpenAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionCommitAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionRollbackAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ConnectionActionGroup extends DefaultActionGroup {

    public ConnectionActionGroup(ConnectionHandler connectionHandler) {
        //add(new ConnectAction(connectionHandler));
        add(new TransactionCommitAction(connectionHandler));
        add(new TransactionRollbackAction(connectionHandler));
        add(new AutoCommitToggleAction(connectionHandler));
        add(new DatabaseLoggingToggleAction(connectionHandler));
        addSeparator();
        add(new OpenSQLConsoleAction(connectionHandler));
        add(new PendingTransactionsOpenAction(connectionHandler));
        addSeparator();
        add(new AutoConnectToggleAction(connectionHandler));
        add(new ConnectAction(connectionHandler));
        add(new DisconnectAction(connectionHandler));
        add(new TestConnectivityAction(connectionHandler));
        add(new LoadAllObjectsAction(connectionHandler));
        add(new DevTestConnectionAction(connectionHandler));
        addSeparator();
        add(new ShowDatabaseInformationAction(connectionHandler));
        add(new OpenConnectionSettingsAction(connectionHandler));
    }
}
