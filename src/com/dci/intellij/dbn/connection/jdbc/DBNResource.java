package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.Traceable;
import com.intellij.openapi.diagnostic.Logger;

public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private long initTimestamp = System.currentTimeMillis();
    protected T inner;

    private ResourceStatusAdapter<Closeable> closed;
    private ResourceStatusAdapter<Cancellable> cancelled;

    protected Traceable traceable = new Traceable();
    private ResourceType type;

    DBNResource(T inner, ResourceType type) {
        if (inner instanceof DBNResource) {
            throw new IllegalArgumentException("Resource already wrapped");
        }

        this.inner = inner;
        this.type = type;

        if (this instanceof Closeable) {
            final Closeable closeable = (Closeable) this;
            closed = new ResourceStatusAdapter<Closeable>(closeable,
                    ResourceStatus.CLOSED,
                    ResourceStatus.CLOSED_SETTING,
                    ResourceStatus.CLOSED_CHECKING) {
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    closeable.closeInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return closeable.isClosedInner();
                }
            };
        }

        if (this instanceof Cancellable) {
            final Cancellable cancellable = (Cancellable) this;
            cancelled = new ResourceStatusAdapter<Cancellable>(cancellable,
                    ResourceStatus.CANCELLED,
                    ResourceStatus.CANCELLED_SETTING,
                    ResourceStatus.CANCELLED_CHECKING) {
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    cancellable.cancelInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return cancellable.isCancelledInner();
                }
            };
        }
    }

    @Override
    public void statusChanged(ResourceStatus status) {
    }

    @Override
    public ResourceType getResourceType() {
        return type;
    }

    public long getInitTimestamp() {
        return initTimestamp;
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void close() {
        closed.change(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() {
        cancelled.change(true);
    }

    public T getInner() {
        return inner;
    }
}
