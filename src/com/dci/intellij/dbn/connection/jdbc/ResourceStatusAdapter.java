package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.ExceptionUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public abstract class ResourceStatusAdapter<T extends Resource> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private final FailsafeWeakRef<T> resource;
    private final ResourceStatus value;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private final long checkInterval;
    protected long checkTimestamp;
    private boolean terminal;

    ResourceStatusAdapter(T resource, ResourceStatus value, ResourceStatus changing, ResourceStatus checking, long checkInterval, boolean terminal) {
        this.resource = new FailsafeWeakRef<T>(resource);
        this.value = value;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
        this.terminal = terminal;
    }

    private boolean is(ResourceStatus status) {
        T resource = getResource();
        return resource.is(status);
    }

    private boolean set(ResourceStatus status, boolean value) {
        T resource = getResource();
        boolean changed = resource.set(status, value);
        if (status == this.value && changed) resource.statusChanged(this.value);
        return changed;
    }

    @NotNull
    private T getResource() {
        return this.resource.get();
    }

    private boolean value() {
        return is(value);
    }

    private boolean isChecking() {
        return is(checking);
    }

    private boolean isChanging() {
        return is(changing);
    }


    public final boolean get() {
        Synchronized.run(
                this,
                () -> canCheck(),
                () -> {
                    try {
                        set(checking, true);
                        if (checkInterval == 0) {
                            set(value, checkControlled());
                        } else {
                            long currentTimeMillis = System.currentTimeMillis();
                            if (TimeUtil.isOlderThan(checkTimestamp, checkInterval)) {
                                checkTimestamp = currentTimeMillis;
                                set(value, checkControlled());
                            }
                        }
                    } catch (Exception t){
                        LOGGER.warn("Failed to check resource " + value + "status", t);
                        fail();
                    } finally {
                        set(checking, false);
                    }
                });

        return value();
    }

    protected void fail() {
        if (terminal) {
            set(value, true); // TODO really
        } else {
            if (checkInterval > 0) {
                checkTimestamp =
                    System.currentTimeMillis() - checkInterval + TimeUtil.FIVE_SECONDS; // retry in 5 seconds
            }

        }
    }

    public final void change(boolean value) throws SQLException {
        Synchronized.run(this,
                () -> canChange(value),
                () -> {
                    set(changing, true);
                    changeControlled(value);
                });
    }

    private boolean canCheck() {
        if (isChecking() || isChanging()) {
            return false;
        } else if (terminal && value()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean canChange(boolean value) {
        if (isChanging()) {
            return false;
        } else if (terminal && value()) {
            return false;
        } else {
            return get() != value;
        }
    }

    private boolean checkControlled() {
        return SimpleTimeoutCall.invoke(5, is(value), true, () -> checkInner());
    }

    private void changeControlled(final boolean value) throws SQLException{
        boolean daemon = true;
        T resource = getResource();
        if (resource.getResourceType() == ResourceType.CONNECTION && ResourceStatusAdapter.this.value == ResourceStatus.CLOSED) {
            // non daemon threads for closing connections
            daemon = false;
        }

        SQLException exception = SimpleTimeoutCall.invoke(10, null, daemon, () -> {
            try {
                if (DatabaseNavigator.debugModeEnabled) LOGGER.info("Started " + getLogIdentifier());
                changeInner(value);
                set(ResourceStatusAdapter.this.value, value);
            } catch (Throwable e) {
                LOGGER.warn("Error " + getLogIdentifier() + ": " + e.getMessage());
                fail();
                return ExceptionUtil.toSqlException(e);
            } finally {
                set(changing, false);
                if (DatabaseNavigator.debugModeEnabled) LOGGER.info("Done " + getLogIdentifier());
            }
            return null;
        });

        if (exception != null) {
            throw exception;
        }

    }

    @NotNull
    private String getLogIdentifier() {
        return changing.toString().toLowerCase() + " " + getResource().getResourceType();
    }

    protected abstract void changeInner(boolean value) throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return getResource().toString();
    }
}
