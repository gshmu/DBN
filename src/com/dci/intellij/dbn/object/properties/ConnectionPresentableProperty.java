package com.dci.intellij.dbn.object.properties;

import javax.swing.Icon;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.pom.Navigatable;

public class ConnectionPresentableProperty extends PresentableProperty{
    private ConnectionHandlerRef connectionHandlerRef;

    public ConnectionPresentableProperty(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    public String getName() {
        return "Connection";
    }

    public String getValue() {
        return getConnectionHandler().getName();
    }

    public Icon getIcon() {
        return getConnectionHandler().getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return getConnectionHandler().getObjectBundle();
    }
}
