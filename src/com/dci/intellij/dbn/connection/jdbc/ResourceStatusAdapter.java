package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutTask;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.application.ApplicationManager;
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

    private boolean isChecking() {
        return is(checking);
    }

    private boolean isCurrent() {
        return is(current);
    }

    private boolean isChanging() {
        return is(changing);
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

    public void change() {
        if (!isCurrent() && !isChanging() && !check()) {
            synchronized (this) {
                if (!isCurrent() && !isChanging()) {
                    set(changing, true);
                    changeControlled();
                }
            }
        }
    }

    private void changeControlled() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            new SimpleBackgroundTask("close resource") {
                @Override
                protected void execute() {
                    changeControlled();
                }
            }.start();
        } else {
            new SimpleTimeoutTask(10, TimeUnit.SECONDS) {
                @Override
                public void run() {
                    try {
                        changeInner();
                    } catch (Throwable e) {
                        LOGGER.warn("Error  " + changing + " resource: " + e.getMessage());
                    } finally {
                        set(current, true);
                        set(changing, false);
                    }
                }
            }.start();
        }
    }

    protected abstract void changeInner() throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return holder.toString();
    }
}
