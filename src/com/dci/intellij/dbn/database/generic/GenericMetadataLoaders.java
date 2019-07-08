package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibility;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.JdbcProperty;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.CachedResultSetRow;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.database.DatabaseInterface.cached;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.resolve;

public interface GenericMetadataLoaders {
    CachedResultSet.Mapper<String> METHOD_COLUMNS = original -> {
        switch (original) {
            case "FUNCTION_CAT":
            case "PROCEDURE_CAT": return "METHOD_CAT";
            case "FUNCTION_SCHEM":
            case "PROCEDURE_SCHEM": return "METHOD_SCHEM";
            case "FUNCTION_NAME":
            case "PROCEDURE_NAME": return "METHOD_NAME";
            case "FUNCTION_TYPE":
            case "PROCEDURE_TYPE": return "METHOD_TYPE";
            default: return null;
        }
    };

    /**************************************************************
     *                     Raw cached meta-data                   *
     **************************************************************/
    static CachedResultSet loadCatalogsRaw(DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_CATALOGS,
                "CATALOGS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getCatalogs();
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadSchemasRaw(DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_SCHEMAS,
                "SCHEMAS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getSchemas();
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadTablesRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_TABLES,
                "TABLES." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(owner[0], owner[1], null, new String[]{"TABLE", "SYSTEM TABLE"});
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadViewsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_VIEWS,
                "VIEWS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(owner[0], owner[1], null, new String[]{"VIEW", "SYSTEM VIEW"});
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_COLUMNS,
                "COLUMNS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(owner[0], owner[1], datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadPseudoColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PSEUDO_COLUMNS,
                "PSEUDO_COLUMNS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(owner[0], owner[1], datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadAllColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_COLUMNS,
                "ALL_COLUMNS" + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadAllPseudoColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PSEUDO_COLUMNS,
                "ALL_PSEUDO_COLUMNS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadIndexesRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_INDEXES,
                "INDEXES." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(owner[0], owner[1], datasetName, false, true);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadPrimaryKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PRIMARY_KEYS,
                "PRIMARY_KEYS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(owner[0], owner[1], datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadForeignKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_IMPORTED_KEYS,
                "FOREIGN_KEYS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(owner[0], owner[1], datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    static CachedResultSet loadFunctionsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTIONS,
                "FUNCTIONS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctions(owner[0], owner[1], null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }

    static CachedResultSet loadFunctionArgumentsRaw(String ownerName, String functionName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTION_COLUMNS,
                "FUNCTION_ARGUMENTS." + ownerName + "." + functionName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctionColumns(owner[0], owner[1], functionName, null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }

    static CachedResultSet loadAllFunctionArgumentsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTION_COLUMNS,
                "ALL_FUNCTION_ARGUMENTS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctionColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }

    static CachedResultSet loadProceduresRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURES,
                "PROCEDURES." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedures(owner[0], owner[1], null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }

    static CachedResultSet loadProcedureArgumentsRaw(String ownerName, String procedureName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURE_COLUMNS,
                "PROCEDURE_ARGUMENTS." + ownerName + "." + procedureName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedureColumns(owner[0], owner[1], procedureName, null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }

    static CachedResultSet loadAllProcedureArgumentsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURE_COLUMNS,
                "ALL_PROCEDURE_ARGUMENTS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName, connection);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedureColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet, rs -> isDeclaredMethod(rs, connection)).normalize(METHOD_COLUMNS);
                });
    }


    static String[] lookupOwner(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                "CATALOG_SCHEMA." + ownerName,
                () -> {
                    if (is(JdbcProperty.CATALOG_AS_OWNER)) {
                        return new String[]{ownerName, null};
                    } else {
                        CachedResultSet schemasRs = loadSchemasRaw(connection);
                        CachedResultSetRow schemaRow = schemasRs.first(row -> ownerName.equals(row.get("TABLE_SCHEM")));
                        String catalogName = schemaRow == null ? null : (String) schemaRow.get("TABLE_CATALOG");
                        return new String[]{catalogName, ownerName};
                    }
                });
    }

    static CachedResultSet attemptCached(JdbcProperty feature, String key, ThrowableCallable<CachedResultSet, SQLException> loader) throws SQLException{
        return cached(key, () -> {
            ConnectionHandler connectionHandler = DatabaseInterface.getConnectionHandler();
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            CachedResultSet resultSet = compatibilityInterface.attempt(feature, loader);
            return CommonUtil.nvl(resultSet, CachedResultSet.EMPTY);
        });
    }

    static DatabaseCompatibility getCompatibility() {
        return DatabaseInterface.getConnectionHandler().getCompatibility();
    }

    static boolean is(JdbcProperty property) {
        return getCompatibility().is(property);
    }

    static boolean isDeclaredMethod(ResultSet rs, DBNConnection connection) throws SQLException {
        if (is(JdbcProperty.CATALOG_AS_OWNER)) {
            return true;
        } else {
            String catalog = resolve(
                    () -> rs.getString("FUNCTION_CAT"),
                    () -> rs.getString("PROCEDURE_CAT"),
                    () -> null);
            if (StringUtil.isEmpty(catalog)) {
                return true;
            } else {
                CachedResultSet catalogsRs = loadCatalogsRaw(connection);
                return catalogsRs.exists(row -> catalog.equals(row.get("TABLE_CAT")));
            }
        }
    }

}
