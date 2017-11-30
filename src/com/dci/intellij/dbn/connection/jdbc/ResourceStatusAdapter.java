package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

public abstract class ResourceStatusAdapter<T extends Resource> {
    protected static final Logger LOGGER = LoggerFactory.createLogger();

    private boolean current;
    private boolean changing;
    private boolean checking;
    private long checkTimestamp;
    private long checkInterval;

    public ResourceStatusAdapter() {
    }

    public ResourceStatusAdapter(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean check() {
        if (current || changing) return true;

        if (checking) return false;

        try {
            checking = true;
            if (checkInterval == 0) {
                current = checkInner();
            } else {
                long currentTimeMillis = System.currentTimeMillis();
                if (TimeUtil.isOlderThan(checkTimestamp, checkInterval)) {
                    checkTimestamp = currentTimeMillis;
                    current = checkInner();
                }
            }
        } catch (Exception t){
            LOGGER.warn("Failed to check resource status", t);
            current = true;
        } finally {
            checking = false;
        }
        return current;
    }

    public void attempt() {
        if (!current && !changing) {
            try {
                changing = true;
                attemptInner();
            } catch (Exception t){
                LOGGER.warn("Failed to close resource", t);
            } finally {
                current = true;
            }
        }
    }

    protected abstract void attemptInner() throws SQLException;

    protected abstract boolean checkInner() throws SQLException;
}
