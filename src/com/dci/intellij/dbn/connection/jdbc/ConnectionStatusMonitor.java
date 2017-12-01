package com.dci.intellij.dbn.connection.jdbc;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.transaction.UncommittedChangeBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;

public class ConnectionStatusMonitor {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long lastAccess;

    private PropertyHolder<ConnectionProperty> status = new PropertyHolderImpl<>(ConnectionProperty.class);
    private UncommittedChangeBundle dataChanges;

    public void updateLastAccess() {
        lastAccess = System.currentTimeMillis();
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccess;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }

    public boolean hasUncommittedChanges() {
        return dataChanges != null && !dataChanges.isEmpty();
    }

    boolean isIdle() {
        return !isActive() && !isReserved();
    }

    boolean isReserved() {
        return status.is(ConnectionProperty.RESERVED);
    }

    boolean isActive() {
        return status.is(ConnectionProperty.ACTIVE);
    }

    boolean isAutoCommit() {
        return status.is(ConnectionProperty.AUTO_COMMIT);
    }

    void set(ConnectionProperty status, boolean value) {
        if (status == ConnectionProperty.RESERVED && value && isActive()) {
            LOGGER.warn("Reserving busy connection");
        }
        this.status.set(status, value);
    }

    boolean is(ConnectionProperty status) {
        return this.status.is(status);
    }

    /********************************************************************
     *                             Data changes                         *
     ********************************************************************/


    public void notifyDataChanges(VirtualFile virtualFile) {
        if (!isAutoCommit()) {
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
