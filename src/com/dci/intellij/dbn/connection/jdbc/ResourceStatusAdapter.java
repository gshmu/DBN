package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

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
        if (is(current) || is(changing)) return true;

        if (is(checking)) return false;

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
        return is(current);
    }

    public void attempt() {
        if (!is(current) && !is(changing)) {
            synchronized (this) {
                if (!is(current) && !is(changing)) {
                    try {
                        set(changing, true);
                        attemptInner();
                    } catch (Exception t){
                        LOGGER.warn("Error " + changing + " resource", t);
                    } finally {
                        set(current, true);
                    }
                }
            }
        }
    }

    protected abstract void attemptInner() throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return "" + current;
    }
}
