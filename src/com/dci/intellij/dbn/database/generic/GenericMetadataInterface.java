package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.MultipartResultSet;
import com.dci.intellij.dbn.database.common.util.WrappedResultSet;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.*;


public class GenericMetadataInterface extends DatabaseMetadataInterfaceImpl {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    // TODO review (ORACLE behavior - package methods come back with FUNCTION_CAT / PROCEDURE_CAT as package name)
    //  filtering them out for now
    private static final CachedResultSet.Where FUNCTION_CAT_IS_NULL = row -> row.get("FUNCTION_CAT") == null;
    private static final CachedResultSet.Where PROCEDURE_CAT_IS_NULL = row -> row.get("PROCEDURE_CAT") == null;

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

        columnsRs = new ColumnsResultSet(columnsRs);
        pseudoColumnsRs = new ColumnsResultSet(pseudoColumnsRs);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet columnsRs = loadAllColumnsRaw(ownerName, connection).open();
        ResultSet pseudoColumnsRs = loadAllPseudoColumnsRaw(ownerName, connection).open();

        columnsRs = new ColumnsResultSet(columnsRs);
        pseudoColumnsRs = new ColumnsResultSet(pseudoColumnsRs);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        ResultSet indexesRs = loadIndexesRaw(ownerName, tableName, connection).groupBy(GROUP_BY_IDX_IDENTIFIER);
        return new IndexesResultSet(indexesRs);
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        // try fast load (may not be supported)
        try {
            CachedResultSet allIndexesRs = loadAllIndexesRaw(ownerName, connection).groupBy(GROUP_BY_IDX_IDENTIFIER);
            if (!allIndexesRs.isEmpty()) {
                return new IndexesResultSet(allIndexesRs);
            }
        } catch (Throwable ignore) {}


        // fallback to slow load (table loop)
        MultipartResultSet allIndexesRs = new MultipartResultSet();

        CachedResultSet tablesRs = loadTablesRaw(ownerName, connection).open();
        tablesRs.forEachRow("TABLE_NAME", String.class, (tableName) -> {
            ResultSet indexesRs = loadIndexes(ownerName, tableName, connection);
            allIndexesRs.add(indexesRs);
        });

        return allIndexesRs;
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
        // try fast load (may not be supported)
        try {
            CachedResultSet primaryKeysRs = loadAllPrimaryKeysRaw(ownerName, connection).groupBy(GROUP_BY_PK_IDENTIFIER);
            CachedResultSet foreignKeysRs = loadAllForeignKeysRaw(ownerName, connection).groupBy(GROUP_BY_FK_IDENTIFIER);
            if (!primaryKeysRs.isEmpty() && !foreignKeysRs.isEmpty()) {
                WrappedResultSet wrappedPrimaryKeysRs = new PrimaryKeysResultSet(primaryKeysRs);
                WrappedResultSet wrappedForeignKeysRs = new ForeignKeysResultSet(foreignKeysRs);
                return new MultipartResultSet(wrappedPrimaryKeysRs, wrappedForeignKeysRs);
            }
        } catch (Throwable ignore) {}

        // fallback to slow load (table loop)
        MultipartResultSet allConstraintsRs = new MultipartResultSet();

        CachedResultSet tablesRs = loadTablesRaw(ownerName, connection).open();
        tablesRs.forEachRow("TABLE_NAME", String.class, (tableName) -> {
            ResultSet constraintsRs = loadConstraints(ownerName, tableName, connection);
            allConstraintsRs.add(constraintsRs);
        });
        return allConstraintsRs;
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


    private CachedResultSet loadAllIndexesRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_INDEXES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(null, ownerName, null, false, true);
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

    private CachedResultSet loadAllPrimaryKeysRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_PRIMARY_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(null, ownerName, null);
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

    private CachedResultSet loadAllForeignKeysRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_FOREIGN_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(null, ownerName, null);
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
