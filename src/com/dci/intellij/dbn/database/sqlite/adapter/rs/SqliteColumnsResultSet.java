package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.cache.CacheAdapter;
import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.ForeignKeyInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.TableInfo;

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

public abstract class SqliteColumnsResultSet extends SqliteResultSetAdapter<SqliteColumnsResultSet.Column> {
    public SqliteColumnsResultSet(ResultSet datasetNames) throws SQLException {
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String parentName = resultSet.getString(1);
                init(parentName);

            }
        };
    }
    public SqliteColumnsResultSet(String datasetName) throws SQLException {
        init(datasetName);
    }

    void init(String datasetName) throws SQLException {
        TableInfo tableInfo = getTableInfo(datasetName);

        for (TableInfo.Row row : tableInfo.getRows()) {
            Column element = new Column();
            element.setDatasetName(datasetName);
            element.setColumnName(row.getName());
            element.setDataTypeName(row.getType());
            element.setNullable(row.getNotnull() == 0);
            element.setPrimaryKey(row.getPk() > 0);
            element.setPosition(row.getCid() + 1);
            addElement(element);
        }

        ForeignKeyInfo foreignKeyInfo = getForeignKeyInfo(datasetName);
        for (ForeignKeyInfo.Row row : foreignKeyInfo.getRows()) {
            String columnName = row.getFrom();
            Column column = getElement(datasetName + "." + columnName);
            column.setForeignKey(true);
        }
    }

    private ForeignKeyInfo getForeignKeyInfo(final String datasetName) throws SQLException {
        return new CacheAdapter<ForeignKeyInfo>(getCache()) {
            @Override
            protected ForeignKeyInfo load() throws SQLException {
                return new ForeignKeyInfo(loadForeignKeyInfo(datasetName));
            }
        }.get(datasetName + ".FOREIGN_KEY_INFO");
    }

    private TableInfo getTableInfo(final String datasetName) throws SQLException {
        return new CacheAdapter<TableInfo>(getCache()) {
            @Override
            protected TableInfo load() throws SQLException {
                return new TableInfo(loatTableInfo(datasetName));
            }
        }.get(datasetName + ".TABLE_INFO");
    }

    protected abstract ResultSet loatTableInfo(String datasetName) throws SQLException;
    protected abstract ResultSet loadForeignKeyInfo(String datasetName) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        Column element = getCurrentElement();
            return
                columnLabel.equals("DATASET_NAME") ? element.getDatasetName() :
                columnLabel.equals("COLUMN_NAME") ? element.getColumnName() :
                columnLabel.equals("DATA_TYPE_NAME") ? element.getDataTypeName() :
                columnLabel.equals("IS_FOREIGN_KEY") ? toFlag(element.isForeignKey()) :
                columnLabel.equals("IS_UNIQUE_KEY") ? toFlag(element.isUniqueKey()) :
                columnLabel.equals("IS_HIDDEN") ? "N" :
                columnLabel.equals("IS_SET") ? "N" :
                columnLabel.equals("IS_NULLABLE") ? toFlag(element.isNullable()) :
                columnLabel.equals("IS_PRIMARY_KEY") ? toFlag(element.isPrimaryKey()) : null;
    }

    public int getInt(String columnLabel) throws SQLException {
        Column element = getCurrentElement();
        return
            columnLabel.equals("POSITION") ? element.getPosition() :
            columnLabel.equals("DATA_LENGTH") ? element.getDataLength() :
            columnLabel.equals("DATA_PRECISION") ? element.getDataPrecision() :
            columnLabel.equals("DATA_SCALE") ? element.getDataScale() : 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }

    public static class Column implements ResultSetElement<Column> {
        String datasetName;
        String columnName;
        String dataTypeName;
        int dataLength;
        int dataPrecision;
        int dataScale;
        int position;

        boolean nullable;
        boolean primaryKey;
        boolean foreignKey;
        boolean uniqueKey;

        public String getDatasetName() {
            return datasetName;
        }

        public void setDatasetName(String datasetName) {
            this.datasetName = datasetName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataTypeName() {
            return dataTypeName;
        }

        public void setDataTypeName(String dataTypeName) {
            this.dataTypeName = dataTypeName;
        }

        public int getDataLength() {
            return dataLength;
        }

        public void setDataLength(int dataLength) {
            this.dataLength = dataLength;
        }

        public int getDataPrecision() {
            return dataPrecision;
        }

        public void setDataPrecision(int dataPrecision) {
            this.dataPrecision = dataPrecision;
        }

        public int getDataScale() {
            return dataScale;
        }

        public void setDataScale(int dataScale) {
            this.dataScale = dataScale;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public boolean isPrimaryKey() {
            return primaryKey;
        }

        public void setPrimaryKey(boolean primaryKey) {
            this.primaryKey = primaryKey;
        }

        public boolean isForeignKey() {
            return foreignKey;
        }

        public void setForeignKey(boolean foreignKey) {
            this.foreignKey = foreignKey;
        }

        public boolean isUniqueKey() {
            return uniqueKey;
        }

        public void setUniqueKey(boolean uniqueKey) {
            this.uniqueKey = uniqueKey;
        }

        @Override
        public String getName() {
            return getDatasetName() + "." + getColumnName();
        }

        @Override
        public int compareTo(Column column) {
            return (datasetName + "." + columnName).compareTo(column.datasetName + "." + column.columnName);
        }
    }
}
