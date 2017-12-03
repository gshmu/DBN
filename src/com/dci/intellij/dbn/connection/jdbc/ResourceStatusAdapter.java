package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.SQLException;

public abstract class ResourceStatusAdapter<T extends Resource> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private final PropertyHolder<ResourceStatus> holder;
    private final ResourceStatus current;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private long checkTimestamp;
    private long checkInterval;

    ResourceStatusAdapter(PropertyHolder<ResourceStatus> holder, ResourceStatus current, ResourceStatus changing, ResourceStatus checking) {
        this(holder, current, changing, checking, 0);
    }

    ResourceStatusAdapter(PropertyHolder<ResourceStatus> holder, ResourceStatus current, ResourceStatus changing, ResourceStatus checking, long checkInterval) {
        this.holder = holder;
        this.current = current;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
    }

    private boolean is(ResourceStatus status) {
        return holder.is(status);
    }

    private boolean set(ResourceStatus status, boolean value) {
        return holder.set(status, value);
    }

    public boolean check() {
        if (isCurrent() || isChanging()) return true;

        if (!isChecking() && !isChanging()) {
            synchronized (this) {
                if (!isChecking() && !isChanging()) {
                    try {
                        set(checking, true);
                        if (checkInterval == 0) {
                            set(current, checkInner());
                        } else {
                            long currentTimeMillis = System.currentTimeMillis();
                            if (TimeUtil.isOlderThan(checkTimestamp, checkInterval)) {
                                checkTimestamp = currentTimeMillis;
                                set(current, checkInner());
                            }
                        }
                    } catch (Exception t){
                        LOGGER.warn("Failed to check resource " + current + "status", t);
                        set(current, true);
                    } finally {
                        set(checking, false);
                    }
                    return isCurrent();

                }
            }
        }

        return false;
    }

    private boolean isChecking() {
        return is(checking);
    }

    private boolean isCurrent() {
        return is(current);
    }

    private boolean isChanging() {
        return is(changing);
    }

    public void attempt() {
        if (!isCurrent() && !isChanging() && !check()) {
            synchronized (this) {
                if (!isCurrent() && !isChanging()) {
                    try {
                        set(changing, true);
                        attemptInner();
                    } catch (Exception t){
                        LOGGER.warn("Error " + changing + " resource", t);
                    } finally {
                        set(current, true);
                        set(changing, false);
                    }
                }
            }
        }
    }

    protected abstract void attemptInner() throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return holder.toString();
    }
}
