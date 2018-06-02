package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface TransactionListener extends EventListener{
    Topic<TransactionListener> TOPIC = Topic.create("Transaction event fired", TransactionListener.class);

    /**
     * This is typically called before the connection has been operationally committed
     */
    void beforeAction(ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action);

    /**
     * This is typically called after the connection has been operationally committed
     * @param succeeded indicates if the commit operation was successful or not
     */
    void afterAction(ConnectionHandler connectionHandler, DBNConnection connection, TransactionAction action, boolean succeeded);

}
