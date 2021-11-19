package com.dci.intellij.dbn.connection.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.transaction.action.AutoCommitToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.AutoConnectToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.DatabaseLoggingToggleAction;
import com.dci.intellij.dbn.connection.transaction.action.PendingTransactionsOpenAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionCommitAction;
import com.dci.intellij.dbn.connection.transaction.action.TransactionRollbackAction;
import com.dci.intellij.dbn.diagnostics.action.BulkLoadAllObjectsAction;
import com.dci.intellij.dbn.diagnostics.action.MiscellaneousConnectionAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;

public class ConnectionActionGroup extends DefaultActionGroup {

    public ConnectionActionGroup(ConnectionHandler connectionHandler) {
        //add(new ConnectAction(connectionHandler));
        add(new TransactionCommitAction(connectionHandler));
        add(new TransactionRollbackAction(connectionHandler));
        add(new AutoCommitToggleAction(connectionHandler));
        add(new DatabaseLoggingToggleAction(connectionHandler));
        addSeparator();
        add(new SQLConsoleOpenAction(connectionHandler));
        add(new PendingTransactionsOpenAction(connectionHandler));
        addSeparator();
        add(new AutoConnectToggleAction(connectionHandler));
        add(new DatabaseConnectAction(connectionHandler));
        add(new DatabaseDisconnectAction(connectionHandler));
        add(new DatabaseConnectivityTestAction(connectionHandler));
        add(new BulkLoadAllObjectsAction(connectionHandler));
        add(new MiscellaneousConnectionAction(connectionHandler));
        addSeparator();
        add(new DatabaseInformationOpenAction(connectionHandler));
        add(new ConnectionSettingsOpenAction(connectionHandler));
    }
}
