package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.common.util.Traceable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.isDatabaseResourceDebug;

@Getter
@Slf4j
public abstract class DBNResource<T> extends ResourceStatusHolder implements Resource{
    private final long initTimestamp = System.currentTimeMillis();
    private final ResourceType resourceType;
    private final String resourceId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    protected final T inner;

    private ResourceStatusAdapter<CloseableResource> closed;
    private ResourceStatusAdapter<CancellableResource> cancelled;

    protected Traceable traceable = new Traceable();
    private final Map<String, Long> errorLogs = new ConcurrentHashMap<>();

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
                    ResourceStatus.CHANGING_CLOSED,
                    ResourceStatus.CHECKING_CLOSED,
                    TimeUtil.Millis.FIVE_SECONDS,
                    Boolean.FALSE,
                    Boolean.TRUE) {
                @Override
                protected void changeInner(boolean value) throws SQLException {
                    closeable.set(ResourceStatus.VALID, false);
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
                    ResourceStatus.CHANGING_CANCELLED,
                    ResourceStatus.CHECKING_CANCELLED,
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

        if (isDatabaseResourceDebug()) log.info("[DBN] Created " + this);
    }

    @Override
    public String toString() {
        String string = resourceType + " (" + resourceId + ")";
        String suffix = super.toString();
        return Strings.isEmpty(suffix) ? string : string + " - " + suffix + "";
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

    public boolean shouldNotify(String error) {
        long timestamp = System.currentTimeMillis();
        long lastTimestamp = errorLogs.computeIfAbsent(error, key -> 0L);
        errorLogs.put(error, timestamp);
        return TimeUtil.isOlderThan(lastTimestamp, TimeUtil.Millis.THIRTY_SECONDS);
    }
}
