package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelHeader;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.state.column.DatasetColumnState;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class DatasetEditorModelHeader extends ResultSetDataModelHeader<DatasetEditorColumnInfo> {
    public DatasetEditorModelHeader(DatasetEditor datasetEditor, @Nullable ResultSet resultSet) throws SQLException {
        DBDataset dataset = datasetEditor.getDataset();

        List<String> columnNames = resultSet == null ? null : ResultSetUtil.getColumnNames(resultSet);
        List<DatasetColumnState> columnStates = datasetEditor.refreshColumnStates(columnNames);

        int index = 0;
        for (DatasetColumnState columnState : columnStates) {
            DBColumn column = dataset.getColumn(columnState.getName());
            if (column != null && columnState.isVisible()) {
                String columnName = column.getName();
                int resultSetIndex = (columnNames == null ? index : StringUtil.indexOfIgnoreCase(columnNames, columnName)) + 1;
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

    private static final Comparator<DBColumn> COLUMN_POSITION_COMPARATOR = new Comparator<DBColumn>() {
        @Override
        public int compare(DBColumn column1, DBColumn column2) {
            return column1.getPosition()-column2.getPosition();
        }
    };
}
