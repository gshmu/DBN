package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.common.util.Traceable;
import com.dci.intellij.dbn.environment.Environment;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.UUID;

@Getter
@Slf4j
public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private final long initTimestamp = System.currentTimeMillis();
    private final ResourceType resourceType;
    private final String resourceId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    protected T inner;

    private ResourceStatusAdapter<CloseableResource> closed;
    private ResourceStatusAdapter<CancellableResource> cancelled;

    protected Traceable traceable = new Traceable();

    DBNResource(T inner, ResourceType type) {
        if (inner instanceof DBNResource) {
            throw new IllegalArgumentException("Resource already wrapped");
        }

        this.inner = inner;
        this.resourceType = type;

        if (this instanceof CloseableResource) {
            CloseableResource closeable = (CloseableResource) this;
            closed = new ResourceStatusAdapterImpl<CloseableResource>(closeable,
                    ResourceStatus.CLOSED,
                    ResourceStatus.CLOSED_APPLYING,
                    ResourceStatus.CLOSED_CHECKING,
                    TimeUtil.Millis.FIVE_SECONDS,
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
            CancellableResource cancellable = (CancellableResource) this;
            cancelled = new ResourceStatusAdapterImpl<CancellableResource>(cancellable,
                    ResourceStatus.CANCELLED,
                    ResourceStatus.CANCELLED_APPLYING,
                    ResourceStatus.CANCELLED_CHECKING,
                    TimeUtil.Millis.FIVE_SECONDS,
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

        if (Environment.DATABASE_RESOURCE_DEBUG_MODE) log.info("[DBN] Created " + this);
    }

    @Override
    public String toString() {
        String string = resourceType + " (" + resourceId + ")";
        String suffix = super.toString();
        return StringUtil.isEmpty(suffix) ? string : string + " - " + suffix + "";
    }

    @Override
    public void statusChanged(ResourceStatus status) {
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
