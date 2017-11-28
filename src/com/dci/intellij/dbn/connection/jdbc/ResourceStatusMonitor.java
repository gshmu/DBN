package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

@Deprecated
public abstract class ResourceStatusMonitor<T extends Resource>{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private boolean busy = false;
    private boolean valid = true;
    private boolean checkingValid = false;

    private long lastCheckTimestamp;
    private long lastAccessTimestamp;

    protected abstract boolean checkValid(int timeout) throws SQLException;

    public boolean isValid() {
        return isValid(2);
    }

    public boolean isValid(int timeout) {
        if (!valid) return false;
        if (busy || checkingValid) return true;

        try {
            checkingValid = true;
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUtil.isOlderThan(lastCheckTimestamp, TimeUtil.THIRTY_SECONDS)) {
                lastCheckTimestamp = currentTimeMillis;
                valid = checkValid(timeout);
            }

        } catch (SQLException e){
            LOGGER.warn("Failed to check resource status", e);
            valid = false;
        } finally {
            checkingValid = false;
        }
        return valid;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isBusy() {
        return busy;
    }

    public void keepAlive() {
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccessTimestamp;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }


}
