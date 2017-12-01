package com.dci.intellij.dbn.connection.jdbc;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

public class ConnectionStatusMonitor {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long lastAccess;
    private boolean reserved;
    private boolean active;
    private boolean resolvingStatus;
    private boolean autoCommit;

    private UncommittedChangeBundle dataChanges;

    public void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    public boolean isResolvingStatus() {
        return resolvingStatus;
    }

    public void setResolvingStatus(boolean resolvingStatus) {
        this.resolvingStatus = resolvingStatus;
    }

    public boolean hasUncommittedChanges() {
        return dataChanges != null && !dataChanges.isEmpty();
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

    public boolean isIdle() {
        return !isActive() && !isReserved();
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    /********************************************************************
     *                             Data changes                         *
     ********************************************************************/


    public void notifyDataChanges(VirtualFile virtualFile) {
        if (!autoCommit) {
            if (dataChanges == null) {
                dataChanges = new UncommittedChangeBundle();
            }
            dataChanges.notifyChange(virtualFile);
        }
    }

    public void resetDataChanges() {
        dataChanges = null;
    }

    @Nullable
    public UncommittedChangeBundle getDataChanges() {
        return dataChanges;
    }


}
