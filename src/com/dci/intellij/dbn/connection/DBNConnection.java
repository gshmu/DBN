package com.dci.intellij.dbn.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.util.TimeUtil;

public class DBNConnection extends DBNConnectionBase {
    private ConnectionType type;
    private boolean busy = false;
    private boolean valid = true;
    private boolean checkingClosed = false;
    private boolean checkingValid = false;
    private boolean closed = false;
    private boolean closing = false;

    private long lastCheckTimestamp;
    private long lastAccessTimestamp;

    public DBNConnection(Connection connection, ConnectionType type) {
        super(connection);
        this.type = type;
    }

    public ConnectionType getType() {
        return type;
    }

    public boolean isPoolConnection() {
        return type == ConnectionType.POOL;
    }

    public boolean isMainConnection() {
        return type == ConnectionType.MAIN;
    }

    public boolean iTestConnection() {
        return type == ConnectionType.TEST;
    }

    @Override
    public boolean isClosed() throws SQLException {
        if (closed || closing) return true;

        if (checkingClosed) return false;

        try {
            checkingClosed = true;
            closed = super.isClosed();
            return closed;
        } finally {
            checkingClosed = false;
        }
    }

    @Override
    public void close() throws SQLException {
        if (!closing) {
            closing = true;
            super.close();
        }
    }

    public boolean isValid() {
        try {
            return isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        if (!valid) return false;
        if (busy || checkingValid) return true;

        try {
            checkingValid = true;
            long currentTimeMillis = System.currentTimeMillis();
            if (TimeUtil.isOlderThan(lastCheckTimestamp, TimeUtil.THIRTY_SECONDS)) {
                lastCheckTimestamp = currentTimeMillis;
                valid = super.isValid(timeout);
            }

            return valid;
        } finally {
            checkingValid = false;
        }
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public boolean isBusy() {
        return busy;
    }

    void keepAlive() {
        lastAccessTimestamp = System.currentTimeMillis();
    }

    int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccessTimestamp;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }
}
