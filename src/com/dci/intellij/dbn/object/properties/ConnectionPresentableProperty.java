package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.pom.Navigatable;

import javax.swing.*;

public class ConnectionPresentableProperty extends PresentableProperty{
    private final ConnectionHandlerRef connectionHandler;

    public ConnectionPresentableProperty(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler.getRef();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @Override
    public String getName() {
        return "Connection";
    }

    @Override
    public String getValue() {
        return getConnectionHandler().getName();
    }

    @Override
    public Icon getIcon() {
        return getConnectionHandler().getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return getConnectionHandler().getObjectBundle();
    }
}
