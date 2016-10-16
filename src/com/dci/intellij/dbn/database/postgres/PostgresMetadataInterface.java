package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


public class PostgresMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public PostgresMetadataInterface(DatabaseInterfaceProvider provider) {
        super("postgres_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    public String createDateString(Date date) {
        String dateString = META_DATE_FORMAT.get().format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, Connection connection) throws SQLException {
        executeStatement(connection, "kill-session", sessionId);
    }

    @Override
    public boolean hasPendingTransactions(Connection connection) {
        try {
            Integer state = (Integer) connection.getClass().getMethod("getTransactionState").invoke(connection);
            return state != 0;
        } catch (Exception e) {
            return true;
        }
    }
}