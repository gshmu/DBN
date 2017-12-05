package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.connection.ConnectionId;

public abstract class ConnectionSettingsAdapter implements ConnectionSettingsListener {
    @Override
    public void connectionsChanged() {

    }

    @Override
    public void connectionChanged(ConnectionId connectionId) {

    }

    @Override
    public void connectionNameChanged(ConnectionId connectionId) {

    }
}
