package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.MultipartResultSet;
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
    private static final CachedResultSet.Condition FUNCTION_CAT_IS_NULL = row -> row.get("FUNCTION_CAT") == null;
    private static final CachedResultSet.Condition PROCEDURE_CAT_IS_NULL = row -> row.get("PROCEDURE_CAT") == null;

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
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet schemasRs = metaData.getSchemas();

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
                        CommonUtil.safeEqual(row.get("TABLE_SCHEM"), ownerName) &&
                        CommonUtil.safeEqual(row.get("TABLE_NAME"), datasetName) &&
                        CommonUtil.safeEqual(row.get("COLUMN_NAME"), columnName));
            }

            @Override
            protected boolean isForeignKey(String ownerName, String datasetName, String columnName) throws SQLException {
                CachedResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection);
                return foreignKeysRs.exists(row ->
                        CommonUtil.safeEqual(row.get("FKTABLE_SCHEM"), ownerName) &&
                        CommonUtil.safeEqual(row.get("FKTABLE_NAME"), datasetName) &&
                        CommonUtil.safeEqual(row.get("FKCOLUMN_NAME"), columnName));
            }

            @Override
            protected boolean isUniqueKey(String ownerName, String datasetName, String columnName) throws SQLException {
                CachedResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection);
                return primaryKeysRs.exists(row ->
                        row.get("PK_NAME") == null &&
                        CommonUtil.safeEqual(row.get("TABLE_SCHEM"), ownerName) &&
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

    /**************************************************************
     *                     Raw cached meta-data                   *
     **************************************************************/
    private CachedResultSet loadTablesRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".TABLES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"TABLE"});
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadViewsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".VIEWS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"VIEW"});
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadPseudoColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadAllPseudoColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadIndexesRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".INDEXES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(null, ownerName, datasetName, false, true);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadPrimaryKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PRIMARY_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadForeignKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".FOREIGN_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadFunctionsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".FUNCTIONS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctions(null, ownerName, null);
                    return CachedResultSet.create(resultSet);
                });
    }

    private CachedResultSet loadProceduresRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".PROCEDURES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedures(null, ownerName, null);
                    return CachedResultSet.create(resultSet);
                });
    }
}
