package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.sqlite.rs.SqliteColumnConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.rs.SqliteColumnIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.rs.SqliteColumnsResultSet;
import com.dci.intellij.dbn.database.sqlite.rs.SqliteConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.rs.SqliteIndexesResultSet;


public class SqliteMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public SqliteMetadataInterface(DatabaseInterfaceProvider provider) {
        super("sqlite_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    @Override
    public ResultSet loadColumns(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(datasetName) {
            @Override
            protected ResultSet loadChildren(String parentName) throws SQLException {
                return executeQuery(connection, "dataset-columns", parentName);
            }
        };
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-names");
        return new SqliteColumnsResultSet(resultSet) {
            @Override
            protected ResultSet loadChildren(String parentName) throws SQLException {
                return executeQuery(connection, "dataset-columns", parentName);
            }
        };
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(tableName) {
            @Override
            protected ResultSet loadChildren(String parentName) throws SQLException {
                return executeQuery(connection, "indexes", parentName);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, Connection connection) throws SQLException {
        ResultSet datasetResultSet = executeQuery(connection, "dataset-names");
        return new SqliteIndexesResultSet(datasetResultSet) {
            @Override
            protected ResultSet loadChildren(String parentName) throws SQLException {
                return executeQuery(connection, "indexes", parentName);
            }
        };
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteConstraintsResultSet(datasetName) {
            @Override
            protected ResultSet loadChildren(String datasetName) throws SQLException {
                return executeQuery(connection, "constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException {
        ResultSet datasetResultSet = executeQuery(connection, "dataset-names");
        return new SqliteConstraintsResultSet(datasetResultSet) {
            @Override
            protected ResultSet loadChildren(String datasetName) throws SQLException {
                return executeQuery(connection, "constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadIndexRelations(String ownerName, String tableName, Connection connection) throws SQLException {
        ResultSet indexesResultSet = loadIndexes(ownerName, tableName, connection);
        return new SqliteColumnIndexesResultSet(indexesResultSet) {
            @Override
            protected ResultSet loadChildren(String indexName) throws SQLException {
                return executeQuery(connection, "index-info", indexName);
            }

            @Override
            protected String getParentColumnName() {
                return "INDEX_NAME";
            }
        };
    }

    @Override
    public ResultSet loadAllIndexRelations(String ownerName, Connection connection) throws SQLException {
        ResultSet indexResultSet = loadAllIndexes(ownerName, connection);
        return new SqliteColumnIndexesResultSet(indexResultSet) {
            @Override
            protected ResultSet loadChildren(String indexName) throws SQLException {
                return executeQuery(connection, "index-info", indexName);
            }

            @Override
            protected String getParentColumnName() {
                return "INDEX_NAME";
            }
        };
    }

    @Override
    public ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException {
        ResultSet constraintResultSet = loadConstraints(ownerName, datasetName, connection);
        return new SqliteColumnConstraintsResultSet(constraintResultSet) {
            @Override
            protected ResultSet loadChildren(String constraintName) throws SQLException {
                String indexName = constraintName.substring(0, constraintName.lastIndexOf("_c"));
                return executeQuery(connection, "index-info", indexName);
            }

            @Override
            protected String getParentColumnName() {
                return "CONSTRAINT_NAME";
            }
        };
    }

    @Override
    public ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException {
        ResultSet constraintResultSet = loadAllConstraints(ownerName, connection);
        return new SqliteColumnConstraintsResultSet(constraintResultSet) {
            @Override
            protected ResultSet loadChildren(String constraintName) throws SQLException {
                String indexName = constraintName.substring(0, constraintName.lastIndexOf("_c"));
                return executeQuery(connection, "index-info", indexName);
            }

            @Override
            protected String getParentColumnName() {
                return "CONSTRAINT_NAME";
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