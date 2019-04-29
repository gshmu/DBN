package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.common.DatabaseMetadataInterfaceImpl;
import com.dci.intellij.dbn.database.common.util.CachedResultSet;
import com.dci.intellij.dbn.database.common.util.QueueResultSet;
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
import static com.dci.intellij.dbn.database.generic.GenericMetadataTranslators.checkEmptyAndClose;


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
                    checkEmptyAndClose(loadTablesRaw(schemaName, connection)) &&
                    checkEmptyAndClose(loadFunctionsRaw(schemaName, connection)) &&
                    checkEmptyAndClose(loadProceduresRaw(schemaName, connection));
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

        return new QueueResultSet(columnsRs, pseudoColumnsRs);
    }

    @Override
    public ResultSet loadIndexes(String ownerName, String tableName, DBNConnection connection) throws SQLException {
        ResultSet indexesRs = loadIndexesRaw(ownerName, tableName, connection);
        return new IndexesResultSet(indexesRs);
    }

    @Override
    public ResultSet loadAllIndexes(String ownerName, DBNConnection connection) throws SQLException {
        QueueResultSet allIndexesRs = new QueueResultSet();

        ResultSet tablesRs = loadTablesRaw(ownerName, connection);
        ResultSetUtil.forEachRow(tablesRs, "TABLE_NAME", String.class, (tableName) -> {
            ResultSet constraintsRs = loadIndexes(ownerName, tableName, connection);
            allIndexesRs.add(constraintsRs);
        });

        return allIndexesRs;
    }

    @Override
    public ResultSet loadConstraints(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        ResultSet primaryKeysRs = loadPrimaryKeysRaw(ownerName, datasetName, connection);
        ResultSet foreignKeysRs = loadForeignKeysRaw(ownerName, datasetName, connection);

        primaryKeysRs = new PrimaryKeysResultSet(primaryKeysRs);
        foreignKeysRs = new ForeignKeysResultSet(foreignKeysRs);

        return new QueueResultSet(primaryKeysRs, foreignKeysRs);
    }


    @Override
    public ResultSet loadAllConstraints(String ownerName, DBNConnection connection) throws SQLException {
        QueueResultSet allConstraintsRs = new QueueResultSet();

        ResultSet tablesRs = loadTablesRaw(ownerName, connection);
        ResultSetUtil.forEachRow(tablesRs, "TABLE_NAME", String.class, (tableName) -> {
            ResultSet constraintsRs = loadConstraints(ownerName, tableName, connection);
            allConstraintsRs.add(constraintsRs);
        });
        return allConstraintsRs;
    }

    /**************************************************************
     *                     Raw cached meta-data                   *
     **************************************************************/
    private ResultSet loadTablesRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".TABLES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"TABLE"});
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadViewsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".VIEWS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getTables(null, ownerName, null, new String[]{"VIEW"});
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadPseudoColumnsRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, datasetName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadAllColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadAllPseudoColumnsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".ALL_PSEUDO_COLUMNS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPseudoColumns(null, ownerName, null, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadIndexesRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".INDEXES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getIndexInfo(null, ownerName, datasetName, false, true);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }


    private ResultSet loadPrimaryKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".PRIMARY_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getPrimaryKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadForeignKeysRaw(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + "." + datasetName + ".FOREIGN_KEYS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getImportedKeys(null, ownerName, datasetName);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadFunctionsRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".FUNCTIONS",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getFunctions(null, ownerName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

    private ResultSet loadProceduresRaw(String ownerName, DBNConnection connection) throws SQLException {
        return cached(
                ownerName + ".PROCEDURES",
                () -> {
                    DatabaseMetaData metaData = connection.getMetaData();
                    ResultSet resultSet = metaData.getProcedures(null, ownerName, null);
                    return CachedResultSet.create(resultSet, ResultSetTranslator.BASIC);
                }).open();
    }

}
