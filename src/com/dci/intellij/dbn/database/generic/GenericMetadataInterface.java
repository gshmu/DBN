package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.QueueResultSet;
import com.dci.intellij.dbn.database.common.util.WrappedResultSet;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class GenericMetadataInterface extends DatabaseMetadataInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public GenericMetadataInterface(DatabaseInterfaceProvider provider) {
        super("generic_metadata_interface.xml", provider);
    }

    @Override
    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return null;
    }

    @Override
    public String createDateString(Date date) {
        String dateString = META_DATE_FORMAT.get().format(date);
        return "str_to_date('" + dateString + "', '%Y-%m-%d %T')";
    }

    @Override
    public boolean isValid(DBNConnection connection) {
        return false;
    }

    @Override
    public ResultSet loadSchemas(DBNConnection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet schemasRs = metaData.getSchemas();

        return new WrappedResultSet(schemasRs) {
            /**
             * Metadata translation for SCHEMAS
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBSchemaMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "SCHEMA_NAME": return inner.getString("TABLE_SCHEM");

                    case "IS_PUBLIC": return "N";
                    case "IS_SYSTEM": return "N";
                    case "IS_EMPTY":
                        String schemaName = inner.getString("TABLE_SCHEM");
                        boolean empty =
                                isEmpty(metaData.getTables(null, schemaName, null, null)) &&
                                isEmpty(metaData.getFunctions(null, schemaName, null)) &&
                                isEmpty(metaData.getProcedures(null, schemaName, null));

                        return empty ? "Y" : "N";
                    default: return null;
                }
            }
        };
    }

    @Override
    public ResultSet loadTables(String ownerName, DBNConnection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tablesRs = metaData.getTables(null, ownerName, null, new String[]{"TABLE"});

        return new WrappedResultSet(tablesRs) {
            /**
             * Metadata translation for TABLES
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBTableMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "TABLE_NAME": return inner.getString("TABLE_NAME"); // redundant (for clarity)
                    case "IS_TEMPORARY":
                        String tableType = inner.getString("TABLE_TYPE");
                        return tableType != null && StringUtil.containsIgnoreCase(tableType, "TEMPORARY") ? "Y" : "N";

                    default: return null;
                }
            }
        };
    }

    @Override
    public ResultSet loadViews(String ownerName, DBNConnection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet viewsRs = metaData.getTables(null, ownerName, null, new String[]{"VIEW"});

        return new WrappedResultSet(viewsRs) {
            /**
             * Metadata translation for VIEWS
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBViewMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "VIEW_NAME": return inner.getString("TABLE_NAME");
                    case "IS_SYSTEM_VIEW":
                        String tableType = inner.getString("TABLE_TYPE");
                        return tableType != null &&
                                StringUtil.containsIgnoreCase(tableType, "SYSTEM") &&
                                StringUtil.containsIgnoreCase(tableType, "VIEW") ? "Y" : "N";

                    case "VIEW_TYPE": return null;
                    case "VIEW_TYPE_OWNER": return null;
                    default: return null;
                }
            }
        };
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet indexesRs = metaData.getIndexInfo(null, ownerName, tableName, false, true);

        return new WrappedResultSet(indexesRs) {
            /**
             * Metadata translation for INDEXES
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBIndexMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "INDEX_NAME":
                        return fallback(
                                () -> inner.getString("INDEX_NAME"),
                                () -> inner.getString("TABLE_NAME") + "_INDEX_STATISTIC");

                    case "TABLE_NAME": return inner.getString("TABLE_NAME");
                    case "IS_UNIQUE":
                        return fallback(
                                () -> inner.getBoolean("NON_UNIQUE"),
                                () -> true) ? "N" : "Y";

                    case "IS_VALID": return "Y";
                    default: return null;
                }
            }
        };
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        // TODO check if unqualified call would work here
        //   - metaData.getIndexInfo(null, ownerName, null, false, true);
        //   - IMPORTANT: indexes would have to be sorted by table name
        List<String> tableNames = getTableNames(ownerName, connection);
        QueueResultSet allIndexesRs = new QueueResultSet();
        for (String tableName : tableNames) {
            ResultSet indexesRs = loadIndexes(ownerName, tableName, connection);
            allIndexesRs.add(indexesRs);
        }
        return allIndexesRs;
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet primaryKeysRs = metaData.getPrimaryKeys(null, ownerName, datasetName);
        ResultSet foreignKeysRs = metaData.getImportedKeys(null, ownerName, datasetName);

        primaryKeysRs = new WrappedResultSet(primaryKeysRs) {

            /**
             * Metadata translation for CONSTRAINTS
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "CONSTRAINT_NAME": {
                        return fallback(
                                () -> inner.getString("PK_NAME"),
                                () -> generateUniqueKeyName(
                                        inner.getString("TABLE_NAME"),
                                        inner.getString("COLUMN_NAME")));
                        // TODO support multiple column keys (complication - needs rs scroll / upfront grouping)
                    }

                    case "CONSTRAINT_TYPE": {
                        String pkName = inner.getString("PK_NAME");
                        return pkName == null ? "UNIQUE" : "PRIMARY KEY";
                    }

                    case "DATASET_NAME": return datasetName;
                    case "FK_CONSTRAINT_OWNER": return null;
                    case "FK_CONSTRAINT_NAME": return null;
                    case "CHECK_CONDITION": return "";
                    case "IS_ENABLED": return "Y";
                    default: return null;
                }

            }
        };

        foreignKeysRs = new WrappedResultSet(foreignKeysRs) {
            /**
             * Metadata translation for CONSTRAINTS
             * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
             */
            @Override
            public String getString(String columnLabel) throws SQLException {
                switch (columnLabel) {
                    case "CONSTRAINT_NAME": {
                        return fallback(
                                () -> inner.getString("FK_NAME"),
                                () -> generateForeignKeyName(
                                        inner.getString("FKTABLE_NAME"),
                                        inner.getString("FKCOLUMN_NAME")));
                        // TODO support multiple column keys (complication - needs rs scroll / upfront grouping)
                    }

                    case "CONSTRAINT_TYPE": return "FOREIGN KEY";
                    case "DATASET_NAME": return datasetName;

                    case "FK_CONSTRAINT_OWNER": {
                        return inner.getString("PKTABLE_SCHEM");
                    }

                    case "FK_CONSTRAINT_NAME": {
                        return fallback(
                                () -> inner.getString("PK_NAME"),
                                () -> generateUniqueKeyName(
                                        inner.getString("PKTABLE_NAME"),
                                        inner.getString("PKCOLUMN_NAME")));
                    }
                    case "CHECK_CONDITION": return "";
                    case "IS_ENABLED": return "Y";
                    default: return null;
                }
            }
        };

        return new QueueResultSet(primaryKeysRs, foreignKeysRs);
    }


    @Override
    public ResultSet loadAllConstraints(String ownerName, DBNConnection connection) throws SQLException {
        List<String> tableNames = getTableNames(ownerName, connection);
        QueueResultSet allConstraintsRs = new QueueResultSet();
        for (String tableName : tableNames) {
            ResultSet constraintsRs = loadConstraints(ownerName, tableName, connection);
            allConstraintsRs.add(constraintsRs);
        }
        return allConstraintsRs;
    }


    @NotNull
    private static String generateUniqueKeyName(String tableName, String columnName) {
        // TODO generated key names should not include column name (support multiple column keys)
        return "unq_" + tableName + "_" + columnName;
    }

    @NotNull
    private static String generateForeignKeyName(String tableName, String columnName) {
        // TODO generated key names should not include column name (support multiple column keys)
        return "fk_" + tableName + "_" + columnName;
    }


    /**
     * Cache table names temporarily (used in several places)
     */
    private static List<String> getTableNames(String ownerName, DBNConnection connection) throws SQLException {
        return DatabaseInterface.getMetaDataCache().get(
                ownerName + "." + "TABLE_NAMES",
                () -> {
                    ArrayList<String> tableNames = new ArrayList<>();
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"TABLE"});
                    ResultSetUtil.scroll(resultSet, () -> {
                        String tableName = resultSet.getString("TABLE_NAME");
                        tableNames.add(tableName);
                    });

                    return tableNames;
                });
    }

    private static <T> T fallback(
            ThrowableCallable<T, Throwable> callable,
            ThrowableCallable<T, SQLException> fallback) throws SQLException {
        try {
            T value = callable.call();
            return value == null ? fallback.call() : value;
        } catch (Throwable t) {
            LOGGER.warn("JDBC metadata operation failed", t);
            return fallback.call();
        }
    }

    /**
     * Checking if result set is missing or empty.
     *  - returns true on exception
     *  - always closes the result set
     */
    private static boolean isEmpty(@Nullable ResultSet resultSet) {
        try {
            return resultSet == null || !resultSet.next();
        } catch (Throwable t) {
            LOGGER.warn("JDBC metadata operation failed", t);
            return true;
        } finally {
            ResourceUtil.close(resultSet);
        }
    }
}
