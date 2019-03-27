package com.dci.intellij.dbn.object.common.loader;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

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
        DBNConnection connection = null;
        ResultSet resultSet = null;
        try {
            connection = connectionHandler.getPoolConnection(true);
            resultSet = connectionHandler.getInterfaceProvider().getMetadataInterface().loadObjectChangeTimestamp(
                    object.getSchema().getName(),
                    object.getName(), objectType, connection);

            return resultSet.next() ? resultSet.getTimestamp(1) : null;
        }  finally {
            ResourceUtil.close(resultSet);
            connectionHandler.freePoolConnection(connection);
        }
    }
}