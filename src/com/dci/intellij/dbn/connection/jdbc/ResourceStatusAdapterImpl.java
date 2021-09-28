package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.Timeout;
import com.dci.intellij.dbn.common.util.ExceptionUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.environment.Environment;
import com.dci.intellij.dbn.language.common.WeakRef;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLRecoverableException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public abstract class ResourceStatusAdapterImpl<T extends Resource> implements ResourceStatusAdapter<T> {
    private final WeakRef<T> resource;
    private final ResourceStatus subject;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private final Boolean terminalStatus;
    private final long checkInterval;
    private long checkTimestamp;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    ResourceStatusAdapterImpl(T resource, ResourceStatus subject, ResourceStatus changing, ResourceStatus checking, long checkInterval, @NotNull Boolean initialStatus, @Nullable Boolean terminalStatus) {
        this.resource = WeakRef.of(resource);
        this.subject = subject;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
        this.terminalStatus = terminalStatus;
        set(subject, initialStatus);
    }

    @Override
    public final boolean get() {
        Lock readLock = this.lock.readLock();
        if (readLock.tryLock()) {
            try {
                if (canCheck()) {
                    check();
                }
            } finally {
                readLock.unlock();
            }
        }
        return value();
    }

    @Override
    public final void set(boolean value) throws SQLException {
        Lock writeLock = this.lock.writeLock();
        if (writeLock.tryLock()) {
            try {
                if (canChange(value)) {
                    set(changing, true);
                    changeControlled(value);
                }
            } finally {
                writeLock.unlock();
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
        return this.resource.ensure();
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
                if (TimeUtil.isOlderThan(checkTimestamp, checkInterval) && !ThreadMonitor.isDispatchThread()) {
                    checkTimestamp = currentTimeMillis;
                    set(subject, checkControlled());
                }
            }
        } catch (SQLRecoverableException e){
            fail();
        } catch (Exception e){
            log.warn("Failed to check resource " + subject + " status", e);
            fail();
        } finally {
            set(checking, false);
        }
    }

    private void fail() {
        if (terminalStatus != null) {
            set(subject, terminalStatus);
        } else {
            if (checkInterval > 0) {
                checkTimestamp =
                    System.currentTimeMillis() - checkInterval + TimeUtil.Millis.FIVE_SECONDS; // retry in 5 seconds
            }

        }
    }

    private boolean canCheck() {
        if (isChecking() || isChanging() || isTerminal()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean canChange(boolean value) {
        if (isChanging() || isTerminal()) {
            return false;
        } else {
            return get() != value;
        }
    }

    private boolean isTerminal() {
        return terminalStatus != null && terminalStatus == value();
    }

    private boolean checkControlled() throws SQLException{
        AtomicReference<SQLException> exception = new AtomicReference<>();
        Boolean result = Timeout.call(5, is(subject), true, () -> {
            try {
                return checkInner();
            } catch (SQLException e) {
                exception.set(e);
                return terminalStatus == null ? value() : terminalStatus;
            } catch (AbstractMethodError e) {
                // not implemented (??) TODO suggest using built in drivers
                log.warn("Functionality not supported by jdbc driver", e);
                return value();
            } catch (RuntimeException t){
                log.warn("Failed to invoke jdbc utility", t);
                return terminalStatus == null ? value() : terminalStatus;
            }
        });
        if (exception.get() != null) {
            throw exception.get();
        }
        return result;
    }

    private void changeControlled(boolean value) throws SQLException{
        boolean daemon = true;
        T resource = getResource();
        ResourceType resourceType = resource.getResourceType();
        if (resourceType == ResourceType.CONNECTION && subject == ResourceStatus.CLOSED) {
            // non daemon threads for closing connections
            daemon = false;
        }

        SQLException exception = Timeout.call(10, null, daemon, () -> {
            try {
                if (Environment.DATABASE_DEBUG_MODE)
                    log.info("[DBN] Applying status " +  subject + " = " + value + " for " + resource);

                changeInner(value);
                set(subject, value);
            } catch (Throwable e) {
                log.warn("[DBN] Failed to apply status " + subject + " = " + value + " for " + resource + ": " + e.getMessage());
                fail();
                return ExceptionUtil.toSqlException(e);
            } finally {
                set(changing, false);

                if (Environment.DATABASE_DEBUG_MODE)
                    log.info("[DBN] Done applying status " + subject + " = "  + value +  " for " + resource);
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
