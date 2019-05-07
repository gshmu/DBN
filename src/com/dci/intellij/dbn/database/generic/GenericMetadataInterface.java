package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseCompatibility;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.JdbcProperty;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.MultipartResultSet;
import com.dci.intellij.dbn.object.type.DBMethodType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.*;


public class GenericMetadataInterface extends DatabaseMetadataInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    // TODO review (ORACLE behavior - package methods come back with FUNCTION_CAT / PROCEDURE_CAT as package name)
    //  filtering them out for now
    private static final CachedResultSet.Condition FUNCTION_CAT_IS_NULL = row ->
            row.get("FUNCTION_CAT") == null ||
                    is(JdbcProperty.CATALOG_AS_OWNER);

    private static final CachedResultSet.Condition PROCEDURE_CAT_IS_NULL = row ->
            row.get("PROCEDURE_CAT") == null ||
                    is(JdbcProperty.CATALOG_AS_OWNER);

    private static final CachedResultSet.GroupBy GROUP_BY_IDX_IDENTIFIER = () -> new String[]{
            "TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "NON_UNIQUE",
            "INDEX_QUALIFIER",
            "INDEX_NAME",
            "TYPE"};

    private static final CachedResultSet.GroupBy GROUP_BY_PK_IDENTIFIER = () -> new String[]{
            "TABLE_CAT",
            "TABLE_SCHEM",
            "TABLE_NAME",
            "PK_NAME"};

    private static final CachedResultSet.GroupBy GROUP_BY_FK_IDENTIFIER = () -> new String[]{
            "PKTABLE_CAT",
            "PKTABLE_SCHEM",
            "PKTABLE_NAME",
            "FKTABLE_CAT",
            "FKTABLE_SCHEM",
            "FKTABLE_NAME",
            "FK_NAME",
            "PK_NAME"};

    GenericMetadataInterface(DatabaseInterfaceProvider provider) {
        super("generic_metadata_interface.xml", provider);
    }

    @Override
    public ResultSet loadCompileObjectErrors(String ownerName, String objectName, DBNConnection connection) throws SQLException {
        return null;
    }

    // TODO
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
        CachedResultSet schemasRs = loadSchemasRaw(connection).open();
        if (schemasRs.isEmpty()) {
            schemasRs = loadCatalogsRaw(connection).open();
            compatibility().set(JdbcProperty.CATALOG_AS_OWNER, !schemasRs.isEmpty());
        }

        return new SchemasResultSet(schemasRs) {
            @Override
            protected boolean isEmpty(String schemaName) throws SQLException {
                return false;
/*
                TODO does not scale for large dbs (find another way or disable option)
                return
                    loadTablesRaw(schemaName, connection).isEmpty() &&
                    loadFunctionsRaw(schemaName, connection).isEmpty() &&
                    loadProceduresRaw(schemaName, connection).isEmpty();
*/
            }
        };
    }

    @Override
    public ResultSet loadTables(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet tablesRs = loadTablesRaw(ownerName, connection).open();
        return new TablesResultSet(tablesRs);
    }

    @Override
    public ResultSet loadViews(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet viewsRs = loadViewsRaw(ownerName, connection).open();
        return new ViewsResultSet(viewsRs);
    }

    @Override
    public ResultSet loadColumns(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet columnsRs = loadColumnsRaw(ownerName, datasetName, connection).open();
        ResultSet pseudoColumnsRs = loadPseudoColumnsRaw(ownerName, datasetName, connection).open();

        columnsRs = newColumnsResultSet(columnsRs, connection);
        pseudoColumnsRs = newColumnsResultSet(pseudoColumnsRs, connection);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet columnsRs = loadAllColumnsRaw(ownerName, connection).open();
        ResultSet pseudoColumnsRs = loadAllPseudoColumnsRaw(ownerName, connection).open();

        columnsRs = newColumnsResultSet(columnsRs, connection);
        pseudoColumnsRs = newColumnsResultSet(pseudoColumnsRs, connection);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @NotNull
    private ColumnsResultSet newColumnsResultSet(ResultSet columnsRs, DBNConnection connection) {
        return new ColumnsResultSet(columnsRs) {
            @Override
            protected boolean isPrimaryKey(String ownerName, String datasetName, String columnName) throws SQLException {
                CachedResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection);
                return primaryKeysRs.exists(row ->
                        row.get("PK_NAME") != null &&
                        CommonUtil.safeEqual(owner(row, "TABLE_CAT", "TABLE_SCHEM"), ownerName) &&
                        CommonUtil.safeEqual(row.get("TABLE_NAME"), datasetName) &&
                        CommonUtil.safeEqual(row.get("COLUMN_NAME"), columnName));
            }

            @Override
            protected boolean isForeignKey(String ownerName, String datasetName, String columnName) throws SQLException {
                CachedResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection);
                return foreignKeysRs.exists(row ->
                        CommonUtil.safeEqual(owner(row, "FKTABLE_CAT", "FKTABLE_SCHEM"), ownerName) &&
                        CommonUtil.safeEqual(row.get("FKTABLE_NAME"), datasetName) &&
                        CommonUtil.safeEqual(row.get("FKCOLUMN_NAME"), columnName));
            }

            @Override
            protected boolean isUniqueKey(String ownerName, String datasetName, String columnName) throws SQLException {
                CachedResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection);
                return primaryKeysRs.exists(row ->
                        row.get("PK_NAME") == null &&
                                CommonUtil.safeEqual(owner(row, "TABLE_CAT", "TABLE_SCHEM"), ownerName) &&
                                CommonUtil.safeEqual(row.get("TABLE_NAME"), datasetName) &&
                                CommonUtil.safeEqual(row.get("COLUMN_NAME"), columnName));
            }
        };
    }


    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        ResultSet indexesRs = loadIndexesRaw(ownerName, tableName, connection).groupBy(GROUP_BY_IDX_IDENTIFIER);
        return new IndexesResultSet(indexesRs);
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allIndexesRs = new MultipartResultSet();

        List<String> tableNames = loadTablesRaw(ownerName, connection).list("TABLE_NAME", String.class);
        for (String tableName : tableNames) {
            ResultSet indexesRs = loadIndexes(ownerName, tableName, connection);
            allIndexesRs.add(indexesRs);
        }

        return allIndexesRs;
    }

    @Override
    public ResultSet loadIndexRelations(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        ResultSet indexesRs = loadIndexesRaw(ownerName, tableName, connection).open();
        return new IndexColumnResultSet(indexesRs);
    }

    @Override
    public ResultSet loadAllIndexRelations(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allIndexRelationsRs = new MultipartResultSet();

        List<String> tableNames = loadTablesRaw(ownerName, connection).list("TABLE_NAME", String.class);
        for (String tableName : tableNames) {
            ResultSet indexRelationsRs = loadIndexRelations(ownerName, tableName, connection);
            allIndexRelationsRs.add(indexRelationsRs);
        }

        return allIndexRelationsRs;
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection).groupBy(GROUP_BY_PK_IDENTIFIER);
        ResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection).groupBy(GROUP_BY_FK_IDENTIFIER);

        primaryKeysRs = new PrimaryKeysResultSet(primaryKeysRs);
        foreignKeysRs = new ForeignKeysResultSet(foreignKeysRs);

        return new MultipartResultSet(primaryKeysRs, foreignKeysRs);
    }


    @Override
    public ResultSet loadAllConstraints(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allConstraintsRs = new MultipartResultSet();

        List<String> tableNames = loadTablesRaw(ownerName, connection).list("TABLE_NAME", String.class);
        for (String tableName : tableNames) {
            ResultSet constraintsRs = loadConstraints(ownerName, tableName, connection);
            allConstraintsRs.add(constraintsRs);
        }
        return allConstraintsRs;
    }

    @Override
    public ResultSet loadConstraintRelations(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection).open();
        ResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection).open();

        ResultSet primaryKeyRelationsRs = new PrimaryKeyRelationsResultSet(primaryKeysRs);
        ResultSet foreignKeyRelationsRs = new ForeignKeyRelationsResultSet(foreignKeysRs);

        return new MultipartResultSet(primaryKeyRelationsRs, foreignKeyRelationsRs);
    }

    @Override
    public ResultSet loadAllConstraintRelations(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allConstraintRelationsRs = new MultipartResultSet();

        List<String> tableNames = loadTablesRaw(ownerName, connection).list("TABLE_NAME", String.class);
        for (String tableName : tableNames) {
            ResultSet constraintRelationsRs = loadConstraintRelations(ownerName, tableName, connection);
            allConstraintRelationsRs.add(constraintRelationsRs);
        }

        return allConstraintRelationsRs;
    }

    @Override
    public ResultSet loadFunctions(String ownerName, DBNConnection connection) throws SQLException {
        CachedResultSet functionsRs = loadFunctionsRaw(ownerName, connection).where(FUNCTION_CAT_IS_NULL);
        return new FunctionsResultSet(functionsRs);
    }



    @Override
    public ResultSet loadProcedures(String ownerName, DBNConnection connection) throws SQLException {
        CachedResultSet proceduresRs = loadProceduresRaw(ownerName, connection).where(PROCEDURE_CAT_IS_NULL);
        return new ProceduresResultSet(proceduresRs);
    }

    @Override
    public ResultSet loadMethodArguments(String ownerName, String methodName, DBMethodType methodType, int overload, DBNConnection connection) throws SQLException {
        if (methodType == DBMethodType.FUNCTION) {
            CachedResultSet argumentsRs = loadFunctionArgumentsRaw(ownerName, methodName, connection).open();
            return new FunctionArgumentsResultSet(argumentsRs);
        }

        if (methodType == DBMethodType.PROCEDURE) {
            CachedResultSet argumentsRs = loadProcedureArgumentsRaw(ownerName, methodName, connection).open();
            return new ProcedureArgumentsResultSet(argumentsRs);
        }

        return null;
    }

    @Override
    public ResultSet loadAllMethodArguments(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet functionArgumentsRs = loadAllFunctionArgumentsRaw(ownerName, connection).open();
        functionArgumentsRs = new FunctionArgumentsResultSet(functionArgumentsRs);

        ResultSet procedureArgumentsRs = loadAllProcedureArgumentsRaw(ownerName, connection).open();
        procedureArgumentsRs = new ProcedureArgumentsResultSet(procedureArgumentsRs);

        return new MultipartResultSet(functionArgumentsRs, procedureArgumentsRs);
    }

    /**************************************************************
     *                     Raw cached meta-data                   *
     **************************************************************/
    private CachedResultSet loadCatalogsRaw(DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_CATALOGS,
                "CATALOGS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getCatalogs();
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadSchemasRaw(DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_SCHEMAS,
                "SCHEMAS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getSchemas();
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadTablesRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_TABLES,
                "TABLES." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(owner[0], owner[1], null, new String[]{"TABLE", "SYSTEM TABLE"});
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadViewsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_VIEWS,
                "VIEWS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(owner[0], owner[1], null, new String[]{"VIEW", "SYSTEM VIEW"});
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_COLUMNS,
                "COLUMNS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(owner[0], owner[1], datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadPseudoColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PSEUDO_COLUMNS,
                "PSEUDO_COLUMNS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(owner[0], owner[1], datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_COLUMNS,
                "ALL_COLUMNS" + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllPseudoColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PSEUDO_COLUMNS,
                "ALL_PSEUDO_COLUMNS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadIndexesRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_INDEXES,
                "INDEXES." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(owner[0], owner[1], datasetName, false, true);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadPrimaryKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PRIMARY_KEYS,
                "PRIMARY_KEYS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(owner[0], owner[1], datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadForeignKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_IMPORTED_KEYS,
                "FOREIGN_KEYS." + ownerName + "." + datasetName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(owner[0], owner[1], datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadFunctionsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTIONS,
                "FUNCTIONS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctions(owner[0], owner[1], null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadFunctionArgumentsRaw(String ownerName, String functionName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTION_COLUMNS,
                "FUNCTION_ARGUMENTS." + ownerName + "." + functionName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctionColumns(owner[0], owner[1], functionName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllFunctionArgumentsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_FUNCTION_COLUMNS,
                "ALL_FUNCTION_ARGUMENTS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctionColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadProceduresRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURES,
                "PROCEDURES." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedures(owner[0], owner[1], null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadProcedureArgumentsRaw(String ownerName, String procedureName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURE_COLUMNS,
                "PROCEDURE_ARGUMENTS." + ownerName + "." + procedureName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedureColumns(owner[0], owner[1], procedureName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllProcedureArgumentsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return attemptCached(
                JdbcProperty.MD_PROCEDURE_COLUMNS,
                "ALL_PROCEDURE_ARGUMENTS." + ownerName,
                () -> {
                    String[] owner = lookupOwner(ownerName);
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedureColumns(owner[0], owner[1], null, null);
                    return CachedResultSet.create(resultSet);
                });
    }


    private CachedResultSet attemptCached(JdbcProperty feature, String key, ThrowableCallable<CachedResultSet, SQLException> loader) throws SQLException{
        return cached(key, () -> {
            DatabaseCompatibilityInterface compatibilityInterface = getProvider().getCompatibilityInterface();
            CachedResultSet resultSet = compatibilityInterface.attempt(feature, loader);
            return CommonUtil.nvl(resultSet, CachedResultSet.EMPTY);
        });
    }

    private String[] lookupOwner(String ownerName) throws SQLException {
        return cached(
                "CATALOG_SCHEMA." + ownerName,
                () -> {
                    if (is(JdbcProperty.CATALOG_AS_OWNER)) {
                        return new String[]{ownerName, null};
                    } else {
                        return new String[]{null, ownerName};
                    }
                });
    }

    static DatabaseCompatibility compatibility() {
        return DatabaseInterface.connectionHandler().getCompatibility();
    }

    private static boolean is(JdbcProperty property) {
        return compatibility().is(property);
    }
}
