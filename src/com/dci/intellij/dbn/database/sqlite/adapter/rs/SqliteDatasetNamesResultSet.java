package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.cache.CacheAdapter;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.TableNames;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;

/**
 * DATASET_NAME
 */

public abstract class SqliteDatasetNamesResultSet extends SqliteResultSetAdapter<SqliteDatasetNamesResultSet.Dataset> {
    public SqliteDatasetNamesResultSet() throws SQLException {
        TableNames tableNames = getTableNames();

        for (TableNames.Row row : tableNames.getRows()) {
            Dataset element = new Dataset();
            element.setDatasetName(row.getName());
            addElement(element);
        }
    }

    private TableNames getTableNames() throws SQLException {
        return new CacheAdapter<TableNames, SQLException>(getCache()) {
            @Override
            protected TableNames load() throws SQLException {
                return new TableNames(loadTableNames());
            }
        }.get("DATASET_NAMES");
    }

    protected abstract ResultSet loadTableNames() throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        Dataset element = getCurrentElement();
        if (columnLabel.equals("DATASET_NAME")) {
            return element.getDatasetName();
        }
        return null;
    }

    public static class Dataset implements ResultSetElement<Dataset> {
        String datasetName;

        public String getDatasetName() {
            return datasetName;
        }

        public void setDatasetName(String datasetName) {
            this.datasetName = datasetName;
        }

        @Override
        public String getName() {
            return getDatasetName();
        }

        @Override
        public int compareTo(Dataset dataset) {
            return datasetName.compareTo(dataset.datasetName);
        }
    }
}
