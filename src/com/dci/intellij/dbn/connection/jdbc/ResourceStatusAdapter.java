package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeWeakRef;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutTask;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public abstract class ResourceStatusAdapter<T extends Resource> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private final FailsafeWeakRef<T> resource;
    private final ResourceStatus current;
    private final ResourceStatus changing;
    private final ResourceStatus checking;
    private final long checkInterval;
    protected long checkTimestamp;

    ResourceStatusAdapter(T resource, ResourceStatus current, ResourceStatus changing, ResourceStatus checking) {
        this(resource, current, changing, checking, 0);
    }

    ResourceStatusAdapter(T resource, ResourceStatus current, ResourceStatus changing, ResourceStatus checking, long checkInterval) {
        this.resource = new FailsafeWeakRef<T>(resource);
        this.current = current;
        this.changing = changing;
        this.checking = checking;
        this.checkInterval = checkInterval;
    }

    private boolean is(ResourceStatus status) {
        T resource = getResource();
        return resource.is(status);
    }

    private boolean set(ResourceStatus status, boolean value) {
        T resource = getResource();
        boolean changed = resource.set(status, value);
        if (status == current && changed) resource.statusChanged(current);
        return changed;
    }

    @NotNull
    private T getResource() {
        return this.resource.get();
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
        if (canCheck()) {
            synchronized (this) {
                if (canCheck()) {
                    try {
                        set(checking, true);
                        if (checkInterval == 0) {
                            set(current, checkControlled());
                        } else {
                            long currentTimeMillis = System.currentTimeMillis();
                            if (TimeUtil.isOlderThan(checkTimestamp, checkInterval)) {
                                checkTimestamp = currentTimeMillis;
                                set(current, checkControlled());
                            }
                        }
                    } catch (Exception t){
                        LOGGER.warn("Failed to check resource " + current + "status", t);
                        fail();
                    } finally {
                        set(checking, false);
                    }
                }
            }
        }

        return isCurrent();
    }

    protected void fail() {
        set(current, true);
        checkTimestamp = 0;
    }

    public final void change(boolean value) {
        if (canChange(value)) {
            synchronized (this) {
                if (canChange(value)) {
                    set(changing, true);
                    changeControlled(value);
                }
            }
        }
    }

    private boolean canCheck() {
        return !isChecking() && !isChanging();
    }

    private boolean canChange(boolean value) {
        return !isCurrent() && !isChanging() && get() != value;
    }

    private boolean checkControlled() {
        return new SimpleTimeoutCall<Boolean>(5, TimeUnit.SECONDS, is(current), true) {
            @Override
            public Boolean call() throws Exception {
                return checkInner();
            }
        }.start();
    }

    private void changeControlled(final boolean value) {
        new SimpleBackgroundTask("change resource status") {
            @Override
            protected void execute() {
                boolean daemon = true;
                T resource = getResource();
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
                            set(current, value);
                        } catch (Throwable e) {
                            LOGGER.warn("Error " + getLogIdentifier() + ": " + e.getMessage());
                            fail();
                        } finally {
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
        return changing.toString().toLowerCase() + " " + getResource().getResourceType();
    }

    protected abstract void changeInner(boolean value) throws SQLException;

    protected abstract boolean checkInner() throws SQLException;

    @Override
    public String toString() {
        return getResource().toString();
    }
}
