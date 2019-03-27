package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.common.util.Traceable;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.SQLException;

public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private long initTimestamp = System.currentTimeMillis();
    protected T inner;
    private ResourceType type;

    private ResourceStatusAdapter<CloseableResource> closed;
    private ResourceStatusAdapter<CancellableResource> cancelled;

    protected Traceable traceable = new Traceable();


    DBNResource(T inner, ResourceType type) {
        if (inner instanceof DBNResource) {
            throw new IllegalArgumentException("Resource already wrapped");
        }

        this.inner = inner;
        this.type = type;

        if (this instanceof CloseableResource) {
            final CloseableResource closeable = (CloseableResource) this;
            closed = new ResourceStatusAdapterImpl<CloseableResource>(closeable,
                    ResourceStatus.CLOSED,
                    ResourceStatus.CLOSED_SETTING,
                    ResourceStatus.CLOSED_CHECKING,
                    TimeUtil.FIVE_SECONDS,
                    Boolean.FALSE,
                    Boolean.TRUE) {
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

        if (this instanceof CancellableResource) {
            final CancellableResource cancellable = (CancellableResource) this;
            cancelled = new ResourceStatusAdapterImpl<CancellableResource>(cancellable,
                    ResourceStatus.CANCELLED,
                    ResourceStatus.CANCELLED_SETTING,
                    ResourceStatus.CANCELLED_CHECKING,
                    TimeUtil.FIVE_SECONDS,
                    Boolean.FALSE,
                    Boolean.TRUE) {
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
    public String toString() {
        String suffix = super.toString();
        return StringUtil.isEmpty(suffix) ? type.name() : type + " - " + suffix + "";
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

    public void close() throws SQLException {
        closed.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public void cancel() throws SQLException {
        cancelled.set(true);
    }

    public T getInner() {
        return inner;
    }
}
