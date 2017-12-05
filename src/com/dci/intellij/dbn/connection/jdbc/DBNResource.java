package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.common.util.Traceable;
import com.intellij.openapi.diagnostic.Logger;

public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private long initTimestamp = System.currentTimeMillis();
    protected T inner;

    private ResourceStatusAdapter<Closeable> CLOSED_STATUS_ADAPTER;
    private ResourceStatusAdapter<Cancellable> CANCELLED_STATUS_ADAPTER;
    private ResourceStatusAdapter<Invalidable> INVALID_STATUS_ADAPTER;

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
                    ResourceStatus.CLOSING,
                    ResourceStatus.CHECKING_CLOSED) {
                @Override
                protected void changeInner() throws SQLException {
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
                    ResourceStatus.CANCELLING,
                    ResourceStatus.CHECKING_CANCELLED) {
                @Override
                protected void changeInner() throws SQLException {
                    cancellable.cancelInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return cancellable.isCancelledInner();
                }
            };
        }

        if (this instanceof Invalidable) {
            final Invalidable invalidable = (Invalidable) this;
            INVALID_STATUS_ADAPTER = new ResourceStatusAdapter<Invalidable>(invalidable,
                    ResourceStatus.INVALID,
                    ResourceStatus.INVALIDATING,
                    ResourceStatus.CHECKING_INVALID,
                    TimeUtil.THIRTY_SECONDS) {
                @Override
                protected void changeInner() throws SQLException {
                    invalidable.invalidateInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return invalidable.isInvalidInner();
                }
            };
        }
    }

    @Override
    public ResourceType getResourceType() {
        return type;
    }

    public long getInitTimestamp() {
        return initTimestamp;
    }

    public boolean isClosed() {
        return CLOSED_STATUS_ADAPTER.check();
    }

    public void close() {
        CLOSED_STATUS_ADAPTER.change();
    }

    public boolean isCancelled() {
        return CANCELLED_STATUS_ADAPTER.check();
    }

    public void cancel() {
        CANCELLED_STATUS_ADAPTER.change();
    }

    public boolean isValid() {
        return isValid(2);
    }

    public boolean isValid(int timeout) {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return INVALID_STATUS_ADAPTER.check();
    }

    public void invalidate() {
        INVALID_STATUS_ADAPTER.change();
    }
}
