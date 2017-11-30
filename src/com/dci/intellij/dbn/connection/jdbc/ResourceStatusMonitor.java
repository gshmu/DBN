package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

public abstract class ResourceStatusMonitor<T extends Resource>{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long lastAccess;
    private boolean reserved;
    private boolean active;

    public void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        if (active) {
            LOGGER.warn("Busy connection unreserved");
        }
        this.reserved = reserved;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
