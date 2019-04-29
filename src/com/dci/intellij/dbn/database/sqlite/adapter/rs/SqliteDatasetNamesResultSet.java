package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.TableNames;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DATASET_NAME
 */

public abstract class SqliteDatasetNamesResultSet extends SqliteResultSetAdapter<SqliteDatasetNamesResultSet.Dataset> {
    protected String ownerName;
    protected SqliteDatasetNamesResultSet(String ownerName) throws SQLException {
        this.ownerName = ownerName;
        TableNames tableNames = getTableNames();

        for (TableNames.Row row : tableNames.getRows()) {
            Dataset element = new Dataset();
            element.setDatasetName(row.getName());
            addElement(element);
        }
    }

    private TableNames getTableNames() throws SQLException {
        return DatabaseInterface.getMetaDataCache().get(
                ownerName + "." + "DATASET_NAMES",
                () -> new TableNames(loadTableNames()));
    }

    protected abstract ResultSet loadTableNames() throws SQLException;

    @Override
    public String getString(String columnLabel) throws SQLException {
        Dataset element = getCurrentElement();
        if (columnLabel.equals("DATASET_NAME")) {
            return element.getDatasetName();
        }
        return null;
    }

    static class Dataset implements ResultSetElement<Dataset> {
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
