package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.dci.intellij.dbn.database.common.util.CachedResultSetRow;
import com.dci.intellij.dbn.database.common.util.WrappedResultSet;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import static java.sql.DatabaseMetaData.functionColumnIn;
import static java.sql.DatabaseMetaData.functionColumnInOut;
import static java.sql.DatabaseMetaData.functionColumnOut;
import static java.sql.DatabaseMetaData.functionColumnResult;

public interface GenericMetadataTranslators {
    Logger LOGGER = LoggerFactory.createLogger();

    Latent<Map<Integer, String>> DATA_TYPE_NAMES =
            Latent.basic(
                    () -> Unsafe.call(() -> {
                        Map<Integer, String> result = new HashMap<>();
                        for (Field field : Types.class.getFields()) {
                            result.put((Integer) field.get(null), field.getName());
                        }
                        return result;
                    }));


    /**
     * Metadata translation for SCHEMAS
     *  - from {@link java.sql.DatabaseMetaData#getSchemas(String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBSchemaMetadataImpl}
     */
    abstract class SchemasResultSet extends WrappedResultSet {
        SchemasResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            String schemaName = owner(inner, "TABLE_CAT", "TABLE_SCHEM");
            switch (columnLabel) {
                case "SCHEMA_NAME": return schemaName;
                case "IS_PUBLIC": return literalBoolean(false);
                case "IS_SYSTEM": return literalBoolean("information_schema".equalsIgnoreCase(schemaName));
                case "IS_EMPTY": return literalBoolean(isEmpty(schemaName));
                default: return null;
            }
        }

        protected abstract boolean isEmpty(String schemaName) throws SQLException;
    }


    /**
     * Metadata translation for TABLES
     *  - from {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBTableMetadataImpl}
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
                    boolean temporary = tableType != null && StringUtil.containsIgnoreCase(tableType, "TEMPORARY");
                    return literalBoolean(temporary);

                default: return null;
            }
        }
    }

    /**
     * Metadata translation for VIEWS
     *  - from {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[])}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBViewMetadataImpl}
     */
    class ViewsResultSet extends WrappedResultSet {
        ViewsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "VIEW_NAME": return inner.getString("TABLE_NAME");

                case "IS_SYSTEM_VIEW": {
                    String tableType = inner.getString("TABLE_TYPE");
                    boolean systemView = tableType != null &&
                            StringUtil.containsIgnoreCase(tableType, "SYSTEM") &&
                            StringUtil.containsIgnoreCase(tableType, "VIEW");
                    return literalBoolean(systemView);
                }

                case "VIEW_TYPE": return null;
                case "VIEW_TYPE_OWNER": return null;
                default: return null;
            }
        }
    }

    /**
     * Abstract metadata translation for COLUMNS and ARGUMENTS
     *  - from {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBDataTypeMetadataImpl}
     */

    abstract class DataTypeResultSet extends WrappedResultSet {
        DataTypeResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "DATA_TYPE_NAME":
                    return resolve(
                            () -> {
                                int dataType = inner.getInt("DATA_TYPE");
                                return DATA_TYPE_NAMES.get().get(dataType);
                            },
                            () -> inner.getString("TYPE_NAME"));


                case "DATA_TYPE_OWNER":   return null;
                case "DATA_TYPE_PACKAGE": return null;
                case "IS_SET":            return literalBoolean(false);
                default: return null;
            }
        }

        @Override
        public long getLong(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "DATA_LENGTH":    return resolve(() -> inner.getLong("COLUMN_SIZE"), () -> 0L);
                default: return 0;
            }
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "DATA_PRECISION": return resolve(() -> inner.getInt("COLUMN_SIZE"), () -> 0);
                case "DATA_SCALE":     return resolve(() -> inner.getInt("DECIMAL_DIGITS"),() -> 0);
                default: return 0;
            }
        }
    }

    /**
     * Metadata translation for COLUMNS
     *  - from {@link java.sql.DatabaseMetaData#getColumns(String, String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBColumnMetadataImpl}
     *            and {@link com.dci.intellij.dbn.database.common.metadata.impl.DBDataTypeMetadataImpl}
     *
     */
    abstract class ColumnsResultSet extends DataTypeResultSet {
        ColumnsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "COLUMN_NAME": return inner.getString("COLUMN_NAME");
                case "DATASET_NAME": return inner.getString("TABLE_NAME");
                case "IS_PRIMARY_KEY":
                    return literalBoolean(
                            isPrimaryKey(
                                    owner(inner, "TABLE_CAT", "TABLE_SCHEM"),
                                    inner.getString("TABLE_NAME"),
                                    inner.getString("COLUMN_NAME")));

                case "IS_FOREIGN_KEY":
                    return literalBoolean(
                            isForeignKey(
                                    owner(inner, "TABLE_CAT", "TABLE_SCHEM"),
                                    inner.getString("TABLE_NAME"),
                                    inner.getString("COLUMN_NAME")));

                case "IS_UNIQUE_KEY":
                    return literalBoolean(
                            isUniqueKey(
                                    owner(inner, "TABLE_CAT", "TABLE_SCHEM"),
                                    inner.getString("TABLE_NAME"),
                                    inner.getString("COLUMN_NAME")));

                case "IS_NULLABLE": {
                    boolean nullable = "YES".equals(inner.getString("IS_NULLABLE"));
                    return literalBoolean(nullable);
                }
                case "IS_HIDDEN": return literalBoolean(false);
                default: return super.getString(columnLabel);
            }
        }

        protected abstract boolean isPrimaryKey(String ownerName, String datasetName, String columnName) throws SQLException;

        protected abstract boolean isForeignKey(String ownerName, String datasetName, String columnName) throws SQLException;

        protected abstract boolean isUniqueKey(String ownerName, String datasetName, String columnName) throws SQLException;
    }


    /**
     * Metadata translation for INDEXES
     *  - from {@link java.sql.DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBIndexMetadataImpl}
     */
    class IndexesResultSet extends WrappedResultSet {
        IndexesResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "INDEX_NAME":
                    return resolve(
                            () -> inner.getString("INDEX_NAME"),
                            () -> inner.getString("TABLE_NAME") + "_INDEX_STATISTIC");

                case "TABLE_NAME": return inner.getString("TABLE_NAME");
                case "IS_UNIQUE": {
                    boolean unique = !resolve(
                            () -> inner.getBoolean("NON_UNIQUE"),
                            () -> true);
                    return literalBoolean(unique);
                }

                case "IS_VALID": return "Y";
                default: return null;
            }
        }
    }

    /**
     * Metadata translation for INDEX_COLUMN relations
     *  - from {@link java.sql.DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBIndexColumnMetadataImpl}
     */
    class IndexColumnResultSet extends WrappedResultSet {
        IndexColumnResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "INDEX_NAME":
                    return resolve(
                            () -> inner.getString("INDEX_NAME"),
                            () -> inner.getString("TABLE_NAME") + "_INDEX_STATISTIC");

                case "COLUMN_NAME": return inner.getString("COLUMN_NAME");
                case "TABLE_NAME":  return inner.getString("TABLE_NAME");

            }
            return super.getString(columnLabel);
        }
    }

    /**
     * Metadata translation for PRIMARY KEYS
     *  - from {@link java.sql.DatabaseMetaData#getPrimaryKeys(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
     */
    class PrimaryKeysResultSet extends WrappedResultSet {
        PrimaryKeysResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "CONSTRAINT_NAME":
                    return resolve(
                            () -> inner.getString("PK_NAME"),
                            () -> uniqueKeyName(inner.getString("TABLE_NAME"), null/*TODO what about multiple unique keys (find the additional discriminator)*/));


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
     * Metadata translation for PRIMARY KEY COLUMN relations
     *  - from {@link java.sql.DatabaseMetaData#getPrimaryKeys(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintColumnMetadataImpl}
     */
    class PrimaryKeyRelationsResultSet extends WrappedResultSet {
        PrimaryKeyRelationsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "CONSTRAINT_NAME":
                    return resolve(
                            () -> inner.getString("PK_NAME"),
                            () -> uniqueKeyName(inner.getString("TABLE_NAME"), null/*TODO what about multiple unique keys (find the additional discriminator)*/));
                case "COLUMN_NAME": return inner.getString("COLUMN_NAME");
                case "DATASET_NAME": return inner.getString("TABLE_NAME");

            }
            return super.getString(columnLabel);
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "POSITION": return inner.getInt("KEY_SEQ");
                default: return 0;
            }
        }
    }

    /**
     * Metadata translation for FOREIGN KEYS
     *  - from {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintMetadataImpl}
     */
    class ForeignKeysResultSet extends WrappedResultSet {
        ForeignKeysResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "CONSTRAINT_NAME": {
                    return resolve(
                            () -> inner.getString("FK_NAME"),
                            () -> foreignKeyName(inner.getString("FKTABLE_NAME"), null));
                }

                case "CONSTRAINT_TYPE":     return "FOREIGN KEY";
                case "DATASET_NAME":        return inner.getString("FKTABLE_NAME");
                case "FK_CONSTRAINT_OWNER": return owner(inner, "PKTABLE_CAT", "PKTABLE_SCHEM");

                case "FK_CONSTRAINT_NAME":
                    return resolve(
                            () -> inner.getString("PK_NAME"),
                            () -> uniqueKeyName(
                                    inner.getString("PKTABLE_NAME"),
                                    inner.getString("PKCOLUMN_NAME")));

                case "CHECK_CONDITION": return "";
                case "IS_ENABLED":      return literalBoolean(true);
                default: return null;
            }
        }
    }

    /**
     * Metadata translation for FOREIGN KEY COLUMN relations
     *  - from {@link java.sql.DatabaseMetaData#getImportedKeys(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBConstraintColumnMetadataImpl}
     */
    class ForeignKeyRelationsResultSet extends WrappedResultSet {
        ForeignKeyRelationsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "CONSTRAINT_NAME":
                    return resolve(
                            () -> inner.getString("FK_NAME"),
                            () -> foreignKeyName(inner.getString("PKTABLE_NAME"), null));

                case "COLUMN_NAME":  return inner.getString("FKCOLUMN_NAME");
                case "DATASET_NAME": return inner.getString("FKTABLE_NAME");
                default:             return null;
            }
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "POSITION": return inner.getInt("KEY_SEQ");
                default: return 0;
            }
        }
    }

    /**
     * Metadata translation for FUNCTIONS
     *  - from {@link java.sql.DatabaseMetaData#getFunctions(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBFunctionMetadataImpl}
     *            and {@link com.dci.intellij.dbn.database.common.metadata.impl.DBMethodMetadataImpl}
     */
    class FunctionsResultSet extends WrappedResultSet {
        FunctionsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }
        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "FUNCTION_NAME":
                    return resolve(
                            () -> inner.getString("SPECIFIC_NAME"),
                            () -> inner.getString("FUNCTION_NAME"));

                case "IS_DETERMINISTIC": return literalBoolean(false);
                case "IS_VALID":         return literalBoolean(true);
                case "IS_DEBUG":         return literalBoolean(false);
                case "LANGUAGE":         return "SQL";
                case "TYPE_NAME":        return null;
                case "PACKAGE_NAME":     return null;
                default:                 return null;
            }
        }

        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "OVERLOAD": return 0;
                case "POSITION": return 0;

                default: return 0;
            }
        }
    }

    /**
     * Metadata translation for FUNCTION ARGUMENTS
     *  - from {@link java.sql.DatabaseMetaData#getFunctionColumns(String, String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBArgumentMetadataImpl}
     *            and {@link com.dci.intellij.dbn.database.common.metadata.impl.DBDataTypeMetadataImpl}
     */
    class FunctionArgumentsResultSet extends DataTypeResultSet {
        FunctionArgumentsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "ARGUMENT_NAME":
                    return resolve(
                            () -> inner.getString("COLUMN_NAME"),
                            () -> "return");

                case "METHOD_TYPE":   return "FUNCTION";
                case "PROGRAM_NAME":  return null;

                case "METHOD_NAME":
                    return resolve(
                            () -> inner.getString("SPECIFIC_NAME"),
                            () -> inner.getString("FUNCTION_NAME"));

                case "IN_OUT":
                    return resolve(
                            () -> {
                                int columnType = inner.getInt("COLUMN_TYPE");
                                switch (columnType) {
                                    case functionColumnIn: return "IN";
                                    case functionColumnOut: return "OUT";
                                    case functionColumnInOut: return "IN/OUT";
                                    case functionColumnResult: return "OUT";
                                    default: return "IN";
                                }
                            },
                            () -> "IN");
                default: return super.getString(columnLabel);
            }
        }
        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "OVERLOAD":    return 0;
                case "SEQUENCE":    return 0;
                case "POSITION":    return inner.getInt("ORDINAL_POSITION");
                default:            return super.getInt(columnLabel);
            }
        }
    }

    /**
     * Metadata translation for PROCEDURES
     *  - from {@link java.sql.DatabaseMetaData#getProcedures(String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBProcedureMetadataImpl}
     *            and {@link com.dci.intellij.dbn.database.common.metadata.impl.DBMethodMetadataImpl}
     */
    class ProceduresResultSet extends WrappedResultSet {
        ProceduresResultSet(@Nullable ResultSet inner) {
            super(inner);
        }

        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "PROCEDURE_NAME":
                    return resolve(
                        () -> inner.getString("SPECIFIC_NAME"),
                        () -> inner.getString("PROCEDURE_NAME"));

                case "IS_DETERMINISTIC": return literalBoolean(false);
                case "IS_VALID":         return literalBoolean(true);
                case "IS_DEBUG":         return literalBoolean(false);
                case "LANGUAGE":         return "SQL";
                case "TYPE_NAME":        return null;
                case "PACKAGE_NAME":     return null;
                default:                 return null;
            }
        }
        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "OVERLOAD": return 0;
                case "POSITION": return 0;
                default: return 0;
            }
        }
    }

    /**
     * Metadata translation for PROCEDURE ARGUMENTS
     *  - from {@link java.sql.DatabaseMetaData#getProcedureColumns(String, String, String, String)}
     *  - comply with {@link com.dci.intellij.dbn.database.common.metadata.impl.DBArgumentMetadataImpl}
     *            and {@link com.dci.intellij.dbn.database.common.metadata.impl.DBDataTypeMetadataImpl}
     */
    class ProcedureArgumentsResultSet extends WrappedResultSet {
        ProcedureArgumentsResultSet(@Nullable ResultSet inner) {
            super(inner);
        }
        @Override
        public String getString(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "ARGUMENT_NAME":
                    return resolve(
                            () -> inner.getString("COLUMN_NAME"),
                            () -> "return");

                case "METHOD_TYPE":   return "PROCEDURE";
                case "PROGRAM_NAME":  return null;
                case "METHOD_NAME":   return inner.getString("PROCEDURE_NAME");

                case "IN_OUT":
                    return resolve(
                            () -> {
                                int columnType = inner.getInt("COLUMN_TYPE");
                                switch (columnType) {
                                    case functionColumnIn: return "IN";
                                    case functionColumnOut: return "OUT";
                                    case functionColumnInOut: return "IN/OUT";
                                    case functionColumnResult: return "OUT";
                                    default: return "IN";
                                }
                            },
                            () -> "IN");


                case "DATA_TYPE_NAME":
                    return resolve(
                            () -> inner.getString("TYPE_NAME"),
                            () -> {
                                int dataType = inner.getInt("DATA_TYPE");
                                return DATA_TYPE_NAMES.get().get(dataType);
                            });


                case "DATA_TYPE_OWNER":   return null;
                case "DATA_TYPE_PACKAGE": return null;
                case "IS_SET":            return literalBoolean(false);
                default: return null;
            }
        }
        @Override
        public int getInt(String columnLabel) throws SQLException {
            switch (columnLabel) {
                case "OVERLOAD":       return 0;
                case "SEQUENCE":       return 0;
                case "POSITION":       return inner.getInt("ORDINAL_POSITION");
                case "DATA_LENGTH":    return resolve(() -> inner.getInt("COLUMN_SIZE"), () -> 0);
                case "DATA_PRECISION": return resolve(() -> inner.getInt("COLUMN_SIZE"), () -> 0);
                case "DATA_SCALE":     return resolve(() -> inner.getInt("DECIMAL_DIGITS"),() -> 0);
                default: return 0;
            }
        }
    }

    /**************************************************************
     *                    Static utilities                        *
     **************************************************************/
    @NotNull
    static String uniqueKeyName(String tableName, String qualifier) {
        return "unq_" + tableName;
    }

    @NotNull
    static String foreignKeyName(String tableName, String qualifier) {
        return "fk_" + tableName;
    }

    static String literalBoolean(boolean bool) {
        return bool ? "Y" : "N";
    }

    static <T> T resolve(
            ThrowableCallable<T, Throwable> resolver,
            ThrowableCallable<T, SQLException> fallbackResolver) throws SQLException {
        try {
            T value = resolver.call();
            return value == null ? fallbackResolver.call() : value;
        } catch (Throwable t) {
            LOGGER.warn("JDBC metadata operation failed", t);
            return fallbackResolver.call();
        }
    }

    static String owner(ResultSet resultSet, String catalogCol, String schemaCol) throws SQLException {
        return resolve(
                () -> resultSet.getString(schemaCol),
                () -> resultSet.getString(catalogCol));
    }

    static String owner(CachedResultSetRow row, String catalogCol, String schemaCol) throws SQLException {
        return resolve(
                () -> (String) row.get(schemaCol),
                () -> (String) row.get(catalogCol));
    }
}
