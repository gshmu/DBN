package com.dci.intellij.dbn.execution;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;

public class ExecutionStatus {
    private transient boolean queued = false;
    private transient boolean prompted = false;
    private transient boolean executing = false;
    private transient boolean cancelled = false;

    public boolean isQueued() {
        return queued;
    }

    public void setQueued(boolean queued) {
        this.queued = queued;
    }

    public boolean isPrompted() {
        return prompted;
    }

    public void setPrompted(boolean prompted) {
        this.prompted = prompted;
    }

    public boolean isExecuting() {
        return executing;
    }

    public void setExecuting(boolean executing) {
        this.executing = executing;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean canExecute() {
        return !queued && !executing && !cancelled;
    }

    public void assertNotCancelled() throws SQLException {
        if (cancelled) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    public void reset() {
        queued = false;
        prompted = false;
        executing = false;
        cancelled = false;
    }

}
