package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;


public class SqliteMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public SqliteMetadataInterface(DatabaseInterfaceProvider provider) {
        super("sqlite_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    public ResultSet loadCharsets(Connection connection) throws SQLException {
        return executeQuery(connection, "charsets");
    }

    @Override
    public ResultSet loadColumns(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(datasetName) {
            @Override
            protected ResultSet loadColumns(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-names");
        return new SqliteColumnsResultSet(resultSet) {
            @Override
            protected ResultSet loadColumns(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException {
        return executeQuery(connection, "view-source-code", viewName);
    }

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public String createDateString(Date date) {
        String dateString = DATE_FORMAT.format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public void killSession(Object sessionId, Object serialNumber, boolean immediate, Connection connection) throws SQLException {
        executeStatement(connection, "kill-session", sessionId);
    }
}