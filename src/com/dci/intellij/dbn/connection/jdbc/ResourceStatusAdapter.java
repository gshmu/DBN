package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutTask;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public abstract class ResourceStatusAdapter<T extends Resource> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private final T resource;
    private final ResourceStatus current;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private long checkTimestamp;
    private long checkInterval;

    ResourceStatusAdapter(T resource, ResourceStatus current, ResourceStatus changing, ResourceStatus checking) {
        this(resource, current, changing, checking, 0);
    }

    ResourceStatusAdapter(T resource, ResourceStatus current, ResourceStatus changing, ResourceStatus checking, long checkInterval) {
        this.resource = resource;
        this.current = current;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
    }

    private boolean is(ResourceStatus status) {
        return resource.is(status);
    }

    private boolean set(ResourceStatus status, boolean value) {
        boolean changed = resource.set(status, value);
        if (changed && status == current) resource.statusChanged(current);
        return changed;
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


    public final boolean get() {
        if (isChanging()) return isCurrent();

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

    public final void change(boolean value) {
        if (!isCurrent() && !isChanging() && !get()) {
            synchronized (this) {
                if (!isCurrent() && !isChanging()) {
                    set(changing, true);
                    changeControlled(value);
                }
            }
        }
    }

    private void changeControlled(final boolean value) {
        new SimpleBackgroundTask("change resource status") {
            @Override
            protected void execute() {
                boolean daemon = true;
                if (resource.getResourceType() == ResourceType.CONNECTION && current == ResourceStatus.CLOSED) {
                    // non daemon threads for closing connections
                    daemon = false;
                }

                new SimpleTimeoutTask(10, TimeUnit.SECONDS, daemon) {
                    @Override
                    public void run() {
                        try {
                            if (SettingsUtil.isDebugEnabled) LOGGER.info("Started " + getLogIdentifier());
                            changeInner(value);
                        } catch (Throwable e) {
                            LOGGER.warn("Error " + getLogIdentifier() + ": " + e.getMessage());
                        } finally {
                            set(current, value);
                            set(changing, false);
                            if (SettingsUtil.isDebugEnabled) LOGGER.info("Done " + getLogIdentifier());
                        }
                    }
                }.start();
            }
        }.start();
    }

    @NotNull
    private String getLogIdentifier() {
        return changing.toString().toLowerCase() + " " + resource.getResourceType();
    }

    protected abstract void changeInner(boolean value) throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return resource.toString();
    }
}
