package com.dci.intellij.dbn.database.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;


public class PostgresMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public PostgresMetadataInterface(DatabaseInterfaceProvider provider) {
        super("postgres_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return null;
    }

    public String createDateString(Date date) {
        String dateString = META_DATE_FORMAT.get().format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, DBNConnection connection) throws SQLException {
        executeStatement(connection, "kill-session", sessionId);
    }

    @Override
    public boolean hasPendingTransactions(@NotNull DBNConnection connection) {
        try {
            Integer state = (Integer) connection.getClass().getMethod("getTransactionState").invoke(connection);
            return state != 0;
        } catch (Exception e) {
            return true;
        }
    }
}