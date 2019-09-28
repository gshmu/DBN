package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawForeignKeyInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawTableInfo;

/**
 * COLUMN_NAME
 * POSITION
 * DATA_TYPE_NAME
 * DATA_TYPE_OWNER
 * DATA_TYPE_PACKAGE
 * DATA_LENGTH
 * DATA_PRECISION
 * DATA_SCALE
 * IS_SET
 * IS_NULLABLE
 * IS_HIDDEN
 * IS_PRIMARY_KEY
 * IS_FOREIGN_KEY
 * IS_UNIQUE_KEY
 */

public abstract class SqliteColumnsResultSet extends SqliteDatasetInfoResultSetStub<SqliteColumnsResultSet.Column> {

    protected SqliteColumnsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    protected SqliteColumnsResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String datasetName) throws SQLException {
        RawTableInfo tableInfo = getTableInfo(datasetName);

        for (RawTableInfo.Row row : tableInfo.getRows()) {
            Column element = new Column();
            element.datasetName =  datasetName;
            element.columnName = row.getName();
            element.dataTypeName = row.getType();
            element.nullable = row.getNotnull() == 0;
            element.primaryKey = row.getPk() > 0;
            element.position = row.getCid() + 1;
            add(element);
        }

        try {
            RawForeignKeyInfo foreignKeyInfo = getForeignKeyInfo(datasetName);
            for (RawForeignKeyInfo.Row row : foreignKeyInfo.getRows()) {
                String columnName = row.getFrom();
                Column column = row(datasetName + "." + columnName);
                column.foreignKey = true;
            }
        } catch (SQLException ignore) {

        }
    }

    private RawForeignKeyInfo getForeignKeyInfo(final String datasetName) throws SQLException {
        return cache().get(
                ownerName + "." + datasetName + ".FOREIGN_KEY_INFO",
                () -> new RawForeignKeyInfo(loadForeignKeyInfo(datasetName)));
    }

    private RawTableInfo getTableInfo(final String datasetName) throws SQLException {
        return cache().get(
                ownerName + "." + datasetName + ".TABLE_INFO",
                () -> new RawTableInfo(loadTableInfo(datasetName)));
    }

    protected abstract ResultSet loadTableInfo(String datasetName) throws SQLException;
    protected abstract ResultSet loadForeignKeyInfo(String datasetName) throws SQLException;

    @Override
    public String getString(String columnLabel) throws SQLException {
        Column element = current();
            return
                columnLabel.equals("DATASET_NAME") ? element.datasetName :
                columnLabel.equals("COLUMN_NAME") ? element.columnName :
                columnLabel.equals("DATA_TYPE_NAME") ? element.dataTypeName :
                columnLabel.equals("IS_FOREIGN_KEY") ? toFlag(element.foreignKey) :
                columnLabel.equals("IS_UNIQUE_KEY") ? toFlag(element.uniqueKey) :
                columnLabel.equals("IS_HIDDEN") ? "N" :
                columnLabel.equals("IS_SET") ? "N" :
                columnLabel.equals("IS_NULLABLE") ? toFlag(element.nullable) :
                columnLabel.equals("IS_PRIMARY_KEY") ? toFlag(element.primaryKey) : null;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        Column element = current();
        return(short) (
                columnLabel.equals("POSITION") ? element.position :
                columnLabel.equals("DATA_LENGTH") ? element.dataLength :
                columnLabel.equals("DATA_PRECISION") ? element.dataPrecision :
                columnLabel.equals("DATA_SCALE") ? element.dataScale : 0);
    }


    @Override
    public int getInt(String columnLabel) throws SQLException {
        Column element = current();
        return
            columnLabel.equals("POSITION") ? element.position :
            columnLabel.equals("DATA_LENGTH") ? element.dataLength :
            columnLabel.equals("DATA_PRECISION") ? element.dataPrecision :
            columnLabel.equals("DATA_SCALE") ? element.dataScale : 0;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }

    static class Column implements SqliteMetadataResultSetRow<Column> {
        private String datasetName;
        private String columnName;
        private String dataTypeName;
        private int dataLength;
        private int dataPrecision;
        private int dataScale;
        private int position;

        private boolean nullable;
        private boolean primaryKey;
        private boolean foreignKey;
        private boolean uniqueKey;

        @Override
        public String identifier() {
            return datasetName + "." + columnName;
        }

        @Override
        public String toString() {
            return "[COLUMN] \"" + datasetName + "\".\"" + columnName + "\"";
        }
    }
}
