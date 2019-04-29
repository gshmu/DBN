package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.database.common.util.WrappedResultSet;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface GenericMetadataTranslators {
    Logger LOGGER = LoggerFactory.createLogger();

    /**
     * Metadata translation for COLUMNS
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBColumnMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}
     */
    class ColumnsResultSet extends WrappedResultSet {
        ColumnsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "COLUMN_NAME": return inner.getString("COLUMN_NAME");
                case "DATASET_NAME": return inner.getString("TABLE_NAME");
                case "IS_PRIMARY_KEY": return "N"; // TODO
                case "IS_FOREIGN_KEY": return "N"; // TODO
                case "IS_UNIQUE_KEY": return "N"; // TODO
                case "IS_NULLABLE": return "N"; // TODO
                case "IS_HIDDEN": return "N";
                default: return null;
            }
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "POSITION": return inner.getInt("ORDINAL_POSITION");
                default: return -1;
            }
        }
    }

    /**
     * Metadata translation for SCHEMAS
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBSchemaMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getSchemas(String, String)}
     */
    abstract class SchemasResultSet extends WrappedResultSet {
        SchemasResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "SCHEMA_NAME": return inner.getString("TABLE_SCHEM");

                case "IS_PUBLIC": return "N";
                case "IS_SYSTEM": return "N";
                case "IS_EMPTY":
                    String schemaName = inner.getString("TABLE_SCHEM");
                    return isEmpty(schemaName) ? "Y" : "N";
                default: return null;
            }
        }

        protected abstract boolean isEmpty(String schemaName) throws SQLException;
    }


    /**
     * Metadata translation for TABLES
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBTableMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     */
    class TablesResultSet extends WrappedResultSet {
        TablesResultSet(@Nullable ResultSet inner) {
            super(inner);
        }
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
    }

    /**
     * Metadata translation for VIEWS
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBViewMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     */
    class ViewsResultSet extends WrappedResultSet {
        ViewsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

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
    }


    /**
     * Metadata translation for INDEXES
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBIndexMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)}
     */
    class IndexesResultSet extends WrappedResultSet {
        IndexesResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

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
    }

    /**
     * Metadata translation for PRIMARY KEYS
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getPrimaryKeys(String, String, String)}
     */
    class PrimaryKeysResultSet extends WrappedResultSet {
        PrimaryKeysResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

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

                case "DATASET_NAME": return inner.getString("TABLE_NAME");
                case "FK_CONSTRAINT_OWNER": return null;
                case "FK_CONSTRAINT_NAME": return null;
                case "CHECK_CONDITION": return "";
                case "IS_ENABLED": return "Y";
                default: return null;
            }

        }
    }

    /**
     * Metadata translation for FOREIGN KEYS
     * comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
     * from {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}
     */
    class ForeignKeysResultSet extends WrappedResultSet {
        ForeignKeysResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

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
                case "DATASET_NAME": return inner.getString("FKTABLE_NAME");

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
    }

    /**************************************************************
     *                    Static utilities                        *
     **************************************************************/
    @NotNull
    static String generateUniqueKeyName(String tableName, String columnName) {
        // TODO generated key names should not include column name (support multiple column keys)
        return "unq_" + tableName + "_" + columnName;
    }

    @NotNull
    static String generateForeignKeyName(String tableName, String columnName) {
        // TODO generated key names should not include column name (support multiple column keys)
        return "fk_" + tableName + "_" + columnName;
    }

    static <T> T fallback(
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
    static boolean checkEmptyAndClose(@Nullable ResultSet resultSet) {
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
