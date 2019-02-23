package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.ExceptionUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public abstract class ResourceStatusAdapterImpl<T extends Resource> implements ResourceStatusAdapter<T> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private final FailsafeWeakRef<T> resource;
    private final ResourceStatus subject;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private final long checkInterval;
    private long checkTimestamp;
    private boolean terminal;

    protected ResourceStatusAdapterImpl(T resource, ResourceStatus subject, ResourceStatus changing, ResourceStatus checking, long checkInterval, boolean terminal) {
        this.resource = new FailsafeWeakRef<T>(resource);
        this.subject = subject;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
        this.terminal = terminal;
    }

    @Override
    public final boolean get() {
        if (canCheck()) {
            synchronized (this) {
                if (canCheck()) {
                    check();
                }
            }
        }
        return value();
    }

    @Override
    public final void set(boolean value) throws SQLException {
        if (canChange(value)) {
            synchronized (this) {
                if (canChange(value)) {
                    set(changing, true);
                    changeControlled(value);
                }
            }
        }
    }


    private boolean is(ResourceStatus status) {
        T resource = getResource();
        return resource.is(status);
    }

    private boolean set(ResourceStatus status, boolean value) {
        T resource = getResource();
        boolean changed = resource.set(status, value);
        if (status == this.subject && changed) resource.statusChanged(this.subject);
        return changed;
    }

    @NotNull
    private T getResource() {
        return this.resource.get();
    }

    private boolean value() {
        return is(subject);
    }

    private boolean isChecking() {
        return is(checking);
    }

    private boolean isChanging() {
        return is(changing);
    }

    private void check() {
        try {
            set(checking, true);
            if (checkInterval == 0) {
                set(subject, checkControlled());
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                if (TimeUtil.isOlderThan(checkTimestamp, checkInterval)) {
                    checkTimestamp = currentTimeMillis;
                    set(subject, checkControlled());
                }
            }
        } catch (Exception t){
            LOGGER.warn("Failed to check resource " + subject + " status", t);
            fail();
        } finally {
            set(checking, false);
        }
    }

    private void fail() {
        if (terminal) {
            set(subject, true); // TODO really
        } else {
            if (checkInterval > 0) {
                checkTimestamp =
                    System.currentTimeMillis() - checkInterval + TimeUtil.FIVE_SECONDS; // retry in 5 seconds
            }

        }
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
        return SimpleTimeoutCall.invoke(5, is(subject), true, () -> checkInner());
    }

    private void changeControlled(boolean value) throws SQLException{
        boolean daemon = true;
        T resource = getResource();
        if (resource.getResourceType() == ResourceType.CONNECTION && subject == ResourceStatus.CLOSED) {
            // non daemon threads for closing connections
            daemon = false;
        }

        SQLException exception = SimpleTimeoutCall.invoke(10, null, daemon, () -> {
            try {
                if (DatabaseNavigator.debugModeEnabled)
                    LOGGER.info("Started changing " + resource.getResourceType() + " resource " + subject + " status to " + value);

                changeInner(value);
                set(subject, value);
            } catch (Throwable e) {
                LOGGER.warn("Failed to change " + resource.getResourceType() + " resource " + subject + " status to " + value + ": " + e.getMessage());
                fail();
                return ExceptionUtil.toSqlException(e);
            } finally {
                set(changing, false);

                if (DatabaseNavigator.debugModeEnabled)
                    LOGGER.info("Done changing " + resource.getResourceType() + " resource " + subject + " status to " + value);
            }
            return null;
        });

        if (exception != null) {
            throw exception;
        }

    }

    protected abstract void changeInner(boolean value) throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return getResource().toString();
    }
}
