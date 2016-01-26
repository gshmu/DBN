package com.dci.intellij.dbn.database.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

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

public abstract class SqliteColumnsResultSet extends ResultSetAdapter {
    private ResultSet columnResultSet;
    private ResultSet datasetResultSet;
    private String datasetName;

    public SqliteColumnsResultSet(String datasetName) {
        this.datasetName = datasetName;
    }

    public SqliteColumnsResultSet(ResultSet datasetResultSet) throws SQLException {
        this.datasetResultSet = datasetResultSet;
    }

    public boolean next() throws SQLException {
        if (datasetResultSet != null) {
            if (datasetName == null || columnResultSet == null || !columnResultSet.next()) {
                if (datasetResultSet.next()) {
                    datasetName = datasetResultSet.getString("DATASET_NAME");
                    ConnectionUtil.closeResultSet(columnResultSet);
                    columnResultSet = loadColumns(datasetName);
                    return columnResultSet.next() || next();
                } else {
                    return false;
                }
            } else {
                return true;
            }
        } else {
            if (columnResultSet == null) {
                columnResultSet = loadColumns(datasetName);
            }
            return columnResultSet.next();
        }
    }


    protected abstract ResultSet loadColumns(String datasetName) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        return
                columnLabel.equals("DATASET_NAME") ? datasetName :
                columnLabel.equals("COLUMN_NAME") ? columnResultSet.getString("name") :
                columnLabel.equals("DATA_TYPE_NAME") ? columnResultSet.getString("type") :
                columnLabel.equals("IS_FOREIGN_KEY") ? "N" :
                columnLabel.equals("IS_UNIQUE_KEY") ? "N" :
                columnLabel.equals("IS_HIDDEN") ? "N" :
                columnLabel.equals("IS_SET") ? "N" :
                columnLabel.equals("IS_NULLABLE") ? (columnResultSet.getInt("notnull") == 0 ? "Y": "N") :
                columnLabel.equals("IS_PRIMARY_KEY") ? (columnResultSet.getInt("pk") == 1 ? "Y" : "N") : null;
    }

    public int getInt(String columnLabel) throws SQLException {
        return
            columnLabel.equals("POSITION") ? columnResultSet.getInt("cid") + 1 :
            columnLabel.equals("DATA_LENGTH") ? 0 :
            columnLabel.equals("DATA_PRECISION") ? 0 :
            columnLabel.equals("DATA_SCALE") ? 0 : 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return super.getBoolean(columnLabel);
    }

    @Override
    public void close() throws SQLException {
        if (datasetResultSet!= null) {
            datasetResultSet.close();
        }
        if (columnResultSet != null) {
            columnResultSet.close();
        }

    }
}
