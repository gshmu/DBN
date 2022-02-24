package com.dci.intellij.dbn.object.properties;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.pom.Navigatable;
import lombok.EqualsAndHashCode;

import javax.swing.Icon;

@EqualsAndHashCode(callSuper = false)
public class ConnectionPresentableProperty extends PresentableProperty{
    private final ConnectionHandlerRef connection;

    public ConnectionPresentableProperty(ConnectionHandler connection) {
        this.connection = connection.ref();
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @Override
    public String getName() {
        return "Connection";
    }

    @Override
    public String getValue() {
        return getConnection().getName();
    }

    @Override
    public Icon getIcon() {
        return getConnection().getIcon();
    }

    @Override
    public Navigatable getNavigatable() {
        return getConnection().getObjectBundle();
    }
}
