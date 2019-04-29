package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.MultipartResultSet;
import com.dci.intellij.dbn.database.common.util.ResultSetTranslator;
import com.intellij.openapi.diagnostic.Logger;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.ColumnsResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.ForeignKeysResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.IndexesResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.PrimaryKeysResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.SchemasResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.TablesResultSet;
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.ViewsResultSet;


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

        return new SchemasResultSet(schemasRs) {
            @Override
            protected boolean isEmpty(String schemaName) throws SQLException {
                return
                    loadTablesRaw(schemaName, connection).isEmpty() &&
                    loadFunctionsRaw(schemaName, connection).isEmpty() &&
                    loadProceduresRaw(schemaName, connection).isEmpty();
            }
        };
    }

    @Override
    public ResultSet loadTables(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet tablesRs = loadTablesRaw(ownerName, connection);
        return new TablesResultSet(tablesRs);
    }

    @Override
    public ResultSet loadViews(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet viewsRs = loadViewsRaw(ownerName, connection);
        return new ViewsResultSet(viewsRs);
    }

    @Override
    public ResultSet loadColumns(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet columnsRs = loadColumnsRaw(ownerName, datasetName, connection);
        ResultSet pseudoColumnsRs = loadPseudoColumnsRaw(ownerName, datasetName, connection);

        columnsRs = new ColumnsResultSet(columnsRs);
        pseudoColumnsRs = new ColumnsResultSet(pseudoColumnsRs);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadAllColumns(String ownerName, DBNConnection connection) throws SQLException {
        ResultSet columnsRs = loadAllColumnsRaw(ownerName, connection);
        ResultSet pseudoColumnsRs = loadAllPseudoColumnsRaw(ownerName, connection);

        columnsRs = new ColumnsResultSet(columnsRs);
        pseudoColumnsRs = new ColumnsResultSet(pseudoColumnsRs);

        return new MultipartResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        ResultSet indexesRs = loadIndexesRaw(ownerName, tableName, connection);
        return new IndexesResultSet(indexesRs);
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allIndexesRs = new MultipartResultSet();

        CachedResultSet tablesRs = loadTablesRaw(ownerName, connection);
        tablesRs.forEachRow("TABLE_NAME", String.class, (tableName) -> {
            ResultSet indexesRs = loadIndexes(ownerName, (String) tableName, connection);
            allIndexesRs.add(indexesRs);
        });

        return allIndexesRs;
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection);
        ResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection);

        primaryKeysRs = new PrimaryKeysResultSet(primaryKeysRs);
        foreignKeysRs = new ForeignKeysResultSet(foreignKeysRs);

        return new MultipartResultSet(primaryKeysRs, foreignKeysRs);
    }


    @Override
    public ResultSet loadAllConstraints(String ownerName, DBNConnection connection) throws SQLException {
        MultipartResultSet allConstraintsRs = new MultipartResultSet();

        CachedResultSet tablesRs = loadTablesRaw(ownerName, connection);
        tablesRs.forEachRow("TABLE_NAME", String.class, (tableName) -> {
            ResultSet constraintsRs = loadConstraints(ownerName, (String) tableName, connection);
            allConstraintsRs.add(constraintsRs);
        });
        return allConstraintsRs;
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
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadViewsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".VIEWS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"VIEW"});
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadPseudoColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadAllColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadAllPseudoColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadIndexesRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".INDEXES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(null, ownerName, datasetName, false, true);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }


    private CachedResultSet loadPrimaryKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PRIMARY_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadForeignKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".FOREIGN_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadFunctionsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".FUNCTIONS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctions(null, ownerName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private CachedResultSet loadProceduresRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".PROCEDURES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedures(null, ownerName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }
}
