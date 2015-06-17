package com.dci.intellij.dbn.object.common.loader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.diagnostic.Logger;

public class DBObjectTimestampLoader{
    protected Logger logger = Logger.getInstance(getClass().getName());
    private String objectType;

    public DBObjectTimestampLoader(String objectType) {
        this.objectType = objectType;
    }

    @Nullable
    public Timestamp load(final DBSchemaObject object) throws SQLException{
        ProgressMonitor.setTaskDescription("Loading timestamp for " + object.getQualifiedNameWithType());
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        Connection connection = null;
        ResultSet resultSet = null;
        try {
            connection = connectionHandler.getPoolConnection();
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadObjectChangeTimestamp(
                    object.getSchema().getName(),
                    object.getName(), objectType, connection);

            return resultSet.next() ? resultSet.getTimestamp(1) : null;
        }  finally {
            ConnectionUtil.closeResultSet(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }
}