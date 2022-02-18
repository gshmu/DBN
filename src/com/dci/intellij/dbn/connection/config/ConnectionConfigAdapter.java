package com.dci.intellij.dbn.connection.config;

import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionId;

public final class ConnectionConfigAdapter implements ConnectionConfigListener {
    private Runnable changesConsumer;
    private Consumer<ConnectionId> removeConsumer;
    private Consumer<ConnectionId> changeConsumer;
    private Consumer<ConnectionId> nameChangeConsumer;


    @Override
    public void connectionsChanged() {
        Safe.run(changesConsumer, c -> c.run());
    }

    @Override
    public void connectionRemoved(ConnectionId connectionId) {
        Safe.run(removeConsumer, c -> c.accept(connectionId));
    }

    @Override
    public void connectionChanged(ConnectionId connectionId) {
        Safe.run(changeConsumer, c -> c.accept(connectionId));
    }

    @Override
    public void connectionNameChanged(ConnectionId connectionId) {
        Safe.run(nameChangeConsumer, c -> c.accept(connectionId));
    }

    public com.dci.intellij.dbn.connection.config.ConnectionConfigAdapter whenSetupChanged(Runnable changesConsumer) {
        this.changesConsumer = changesConsumer;
        return this;
    }

    public com.dci.intellij.dbn.connection.config.ConnectionConfigAdapter whenRemoved(Consumer<ConnectionId> removeConsumer) {
        this.removeConsumer = removeConsumer;
        return this;
    }

    public com.dci.intellij.dbn.connection.config.ConnectionConfigAdapter whenChanged(Consumer<ConnectionId> changeConsumer) {
        this.changeConsumer = changeConsumer;
        return this;
    }

    public com.dci.intellij.dbn.connection.config.ConnectionConfigAdapter whenNameChanged(Consumer<ConnectionId> nameChangeConsumer) {
        this.nameChangeConsumer = nameChangeConsumer;
        return this;
    }
}
