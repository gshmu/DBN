package com.dbn.editor.data.model;

import com.dbn.common.util.Lists;
import com.dbn.connection.ResultSets;
import com.dbn.data.model.ColumnInfo;
import com.dbn.data.model.resultSet.ResultSetDataModelHeader;
import com.dbn.editor.data.DatasetEditor;
import com.dbn.editor.data.state.column.DatasetColumnState;
import com.dbn.object.DBColumn;
import com.dbn.object.DBDataset;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class DatasetEditorModelHeader extends ResultSetDataModelHeader<DatasetEditorColumnInfo> {
    DatasetEditorModelHeader(DatasetEditor datasetEditor, @Nullable ResultSet resultSet) throws SQLException {
        DBDataset dataset = datasetEditor.getDataset();

        List<String> columnNames = resultSet == null ? null : ResultSets.getColumnNames(resultSet);
        List<DatasetColumnState> columnStates = datasetEditor.refreshColumnStates(columnNames);

        int index = 0;
        for (DatasetColumnState columnState : columnStates) {
            DBColumn column = dataset.getColumn(columnState.getName());
            if (column != null && columnState.isVisible()) {
                String columnName = column.getName();
                int resultSetIndex = (columnNames == null ? index : Lists.indexOf(columnNames, columnName, true)) + 1;
                if (resultSetIndex > 0) {
                    DatasetEditorColumnInfo columnInfo = new DatasetEditorColumnInfo(column, index, resultSetIndex);
                    addColumnInfo(columnInfo);
                    index++;
                }
            }
        }
    }

    public int indexOfColumn(DBColumn column) {
        for (int i=0; i<getColumnCount(); i++) {
            ColumnInfo info = getColumnInfo(i);
            DatasetEditorColumnInfo columnInfo = (DatasetEditorColumnInfo) info;
            DBColumn col = columnInfo.getColumn();
            if (col.equals(column)) return i;
        }
        return -1;
    }

    private static final Comparator<DBColumn> COLUMN_POSITION_COMPARATOR = (column1, column2) -> column1.getPosition() - column2.getPosition();
}
