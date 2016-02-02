package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteColumnConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteColumnIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteColumnsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteTriggerSourceResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteTriggersResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteViewSourceResultSet;


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
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(executeQuery(connection, "dataset-names")) {
            @Override
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(tableName) {
            @Override
            protected ResultSet getIndexResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "indexes", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(executeQuery(connection, "dataset-names")) {
            @Override
            protected ResultSet getIndexResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "indexes", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteConstraintsResultSet(datasetName) {
            @Override
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException {
        return new SqliteConstraintsResultSet(executeQuery(connection, "dataset-names")) {
            @Override
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    public ResultSet loadDatasetTriggers(String ownerName, String datasetName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-triggers", datasetName);
        return new SqliteTriggersResultSet(resultSet);
    }

    public ResultSet loadAllDatasetTriggers(String ownerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "all-dataset-triggers");
        return new SqliteTriggersResultSet(resultSet);
    }

    @Override
    public ResultSet loadIndexRelations(String ownerName, String tableName, Connection connection) throws SQLException {
        return new SqliteColumnIndexesResultSet(tableName) {
            @Override
            protected ResultSet getIndexResultSet(String tableName) throws SQLException {
                return executeQuery(connection, "indexes", tableName);
            }

            @Override
            protected ResultSet getIndexInfoResultSet(String indexName) throws SQLException {
                return executeQuery(connection, "index-info", indexName);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexRelations(String ownerName, Connection connection) throws SQLException {
        return new SqliteColumnIndexesResultSet(executeQuery(connection, "dataset-names")) {
            @Override
            protected ResultSet getIndexResultSet(String tableName) throws SQLException {
                return executeQuery(connection, "indexes", tableName);
            }

            @Override
            protected ResultSet getIndexInfoResultSet(String indexName) throws SQLException {
                return executeQuery(connection, "index-info", indexName);
            }
        };
    }

    @Override
    public ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteColumnConstraintsResultSet(datasetName) {
            @Override
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException {
        return new SqliteColumnConstraintsResultSet(executeQuery(connection, "dataset-names")) {
            @Override
            protected ResultSet getColumnsResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "dataset-columns", datasetName);
            }

            @Override
            protected ResultSet getForeignKeyResultSet(String datasetName) throws SQLException {
                return executeQuery(connection, "foreign-key-constraints", datasetName);
            }
        };
    }

    @Override
    public ResultSet loadViewSourceCode(String ownerName, String viewName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "view-source-code", viewName);
        return new SqliteViewSourceResultSet(resultSet);
    }

    public ResultSet loadDatasetTriggerSourceCode(String tableOwner, String tableName, String ownerName, String triggerName, Connection connection) throws SQLException {
        ResultSet resultSet = executeQuery(connection, "dataset-trigger-source-code", tableName, triggerName);
        return new SqliteTriggerSourceResultSet(resultSet);
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