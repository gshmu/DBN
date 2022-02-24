package com.dci.intellij.dbn.connection.transaction;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface TransactionListener extends EventListener{
    Topic<TransactionListener> TOPIC = Topic.create("Transaction event fired", TransactionListener.class);

    /**
     * This is typically called before the connection has been operationally committed
     */
    default void beforeAction(@NotNull ConnectionHandler connection, DBNConnection conn, TransactionAction action){};

    /**
     * This is typically called after the connection has been operationally committed
     * @param succeeded indicates if the commit operation was successful or not
     */
    default void afterAction(@NotNull ConnectionHandler connection, DBNConnection conn, TransactionAction action, boolean succeeded){};

}
