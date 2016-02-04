package com.dci.intellij.dbn.database.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteColumnsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintsResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteDatasetNamesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteIndexesResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteTriggerSourceResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteTriggersResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteViewSourceResultSet;


public class SqliteMetadataInterface extends DatabaseMetadataInterfaceImpl {

    public SqliteMetadataInterface(DatabaseInterfaceProvider provider) {
        super("sqlite_metadata_interface.xml", provider);
    }

    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, Connection connection) throws SQLException {
        return null;
    }

    @Override
    public ResultSet loadColumns(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(datasetName, connection) {
            @Override
            protected ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadTableInfo(datasetName, connection);
            }

            @Override
            protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadForeignKeyInfo(datasetName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, Connection connection) throws SQLException {
        return new SqliteColumnsResultSet(getDatasetNames(connection), connection) {
            @Override
            protected ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadTableInfo(datasetName, connection);
            }

            @Override
            protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteMetadataInterface.this.loadForeignKeyInfo(datasetName, connection);
            }
        };
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(tableName, connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(tableName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, final Connection connection) throws SQLException {
        return new SqliteIndexesResultSet(getDatasetNames(connection), connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(tableName, connection);
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
        return new SqliteColumnIndexesResultSet(tableName, connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(tableName, connection);
            }

            @Override
            protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexDetailInfo(indexName, connection);
            }
        };
    }

    @Override
    public ResultSet loadAllIndexRelations(String ownerName, Connection connection) throws SQLException {
        return new SqliteColumnIndexesResultSet(getDatasetNames(connection), connection) {
            @Override
            protected ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexInfo(tableName, connection);
            }

            @Override
            protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteMetadataInterface.this.loadIndexDetailInfo(indexName, connection);
            }
        };
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new ConstraintsResultSet(datasetName, connection);
    }

    @Override
    public ResultSet loadAllConstraints(String ownerName, Connection connection) throws SQLException {
        return new ConstraintsResultSet(getDatasetNames(connection), connection);
    }

    @Override
    public ResultSet loadConstraintRelations(String ownerName, String datasetName, Connection connection) throws SQLException {
        return new ColumnConstraintsResultSet(datasetName, connection);
    }

    @Override
    public ResultSet loadAllConstraintRelations(String ownerName, Connection connection) throws SQLException {
        return new ColumnConstraintsResultSet(getDatasetNames(connection), connection);
    }

    private class ConstraintsResultSet extends SqliteConstraintsResultSet {

        public ConstraintsResultSet(SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
            super(datasetNames, connection);
        }

        public ConstraintsResultSet(String datasetName, Connection connection) throws SQLException {
            super(datasetName, connection);
        }

        @Override
        protected ResultSet loadTableInfo(String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadTableInfo(datasetName, getConnection());
        }

        @Override
        protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadForeignKeyInfo(datasetName, getConnection());
        }

        @Override
        protected ResultSet loadIndexInfo(String tableName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexInfo(tableName, getConnection());
        }

        @Override
        protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexDetailInfo(indexName, getConnection());
        }
    }

    private class ColumnConstraintsResultSet extends SqliteColumnConstraintsResultSet {

        public ColumnConstraintsResultSet(SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
            super(datasetNames, connection);
        }

        public ColumnConstraintsResultSet(String datasetName, Connection connection) throws SQLException {
            super(datasetName, connection);
        }

        @Override
        protected ResultSet loadTableInfo(String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadTableInfo(datasetName, getConnection());
        }

        @Override
        protected ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
            return SqliteMetadataInterface.this.loadForeignKeyInfo(datasetName, getConnection());
        }

        @Override
        protected ResultSet loadIndexInfo(String tableName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexInfo(tableName, getConnection());
        }

        @Override
        protected ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
            return SqliteMetadataInterface.this.loadIndexDetailInfo(indexName, getConnection());
        }
    }

    private ResultSet loadTableInfo(String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "dataset-columns", datasetName);
    }

    private ResultSet loadForeignKeyInfo(String datasetName, Connection connection) throws SQLException {
        return executeQuery(connection, "foreign-key-constraints", datasetName);
    }

    private ResultSet loadIndexInfo(String tableName, Connection connection) throws SQLException {
        return executeQuery(connection, "indexes", tableName);
    }

    private ResultSet loadIndexDetailInfo(String indexName, Connection connection) throws SQLException {
        return executeQuery(connection, "index-info", indexName);
    }

    @NotNull
    SqliteDatasetNamesResultSet getDatasetNames(final Connection connection) throws SQLException {
        return new SqliteDatasetNamesResultSet() {
            @Override
            protected ResultSet loadTableNames() throws SQLException {
                return executeQuery(connection, "dataset-names");
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