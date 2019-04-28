package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.ResultSetWrapper;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;


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
        ResultSet resultSet = metaData.getSchemas();
        return new ResultSetWrapper(resultSet) {
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
        ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"TABLE"});
        return new ResultSetWrapper(resultSet) {
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
        ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"\"VIEW\""});

        return new ResultSetWrapper(resultSet) {
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
        ResultSet resultSet = metaData.getIndexInfo(null, ownerName, tableName, false, true);
        return new ResultSetWrapper(resultSet) {
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
