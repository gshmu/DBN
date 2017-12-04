package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.IntervalChecker;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;

import java.util.List;

public class ConnectionHandlerStatus {
    ConnectionHandlerRef connectionHandlerRef;

    public ConnectionHandlerStatus(ConnectionHandlerRef connectionHandlerRef) {
        this.connectionHandlerRef = connectionHandlerRef;
    }

    private IntervalChecker valid = new IntervalChecker(TimeUtil.TEN_SECONDS) {
        @Override
        protected boolean doCheck() {
            try {
                ConnectionHandler connectionHandler = connectionHandlerRef.get();
                ConnectionPool connectionPool = connectionHandler.getConnectionPool();
                DBNConnection mainConnection = connectionPool.getMainConnection(false);
                if (mainConnection != null && mainConnection.isValid()) {
                    return true;
                }

                List<DBNConnection> poolConnections = connectionPool.getPoolConnections();
                for (DBNConnection poolConnection : poolConnections) {
                    if (poolConnection.isValid()) {
                        return true;
                    }
                }

            } catch (Exception e) {
                return false;
            }
            return false;
        }
    };

    public boolean isValid() {
        return valid.check();
    }
}
