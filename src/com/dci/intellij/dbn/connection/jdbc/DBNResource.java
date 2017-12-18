package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.Traceable;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.SQLException;

public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private long initTimestamp = System.currentTimeMillis();
    protected T inner;

    private ResourceStatusAdapter<Closeable> CLOSED_STATUS_ADAPTER;
    private ResourceStatusAdapter<Cancellable> CANCELLED_STATUS_ADAPTER;

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
            CLOSED_STATUS_ADAPTER = new ResourceStatusAdapter<Closeable>(closeable,
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
            CANCELLED_STATUS_ADAPTER = new ResourceStatusAdapter<Cancellable>(cancellable,
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
        return CLOSED_STATUS_ADAPTER.get();
    }

    public void close() {
        CLOSED_STATUS_ADAPTER.change(true);
    }

    public boolean isCancelled() {
        return CANCELLED_STATUS_ADAPTER.get();
    }

    public void cancel() {
        CANCELLED_STATUS_ADAPTER.change(true);
    }
}
