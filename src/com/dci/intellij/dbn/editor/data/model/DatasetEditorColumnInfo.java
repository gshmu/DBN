package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.editor.data.DatasetEditorUtils;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatasetEditorColumnInfo extends ResultSetColumnInfo {
    private static final List<String> EMPTY_LIST = new ArrayList<String>(0);
    private List<String> possibleValues;
    private DBObjectRef<DBColumn> columnRef;
    private int columnIndex;
    private boolean isPrimaryKey;
    private boolean isForeignKey;

    public DatasetEditorColumnInfo(DBColumn column, int columnIndex, int resultSetColumnIndex) {
        super(columnIndex, resultSetColumnIndex);
        this.columnRef = DBObjectRef.from(column);
        this.columnIndex = columnIndex;
        this.isPrimaryKey = column.isPrimaryKey();
        this.isForeignKey = column.isForeignKey();
    }

    @Nullable
    public DBColumn getColumn() {
        return DBObjectRef.get(columnRef);
    }

    public String getName() {
        return columnRef.getName();
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    @Nullable
    public DBDataType getDataType() {
        DBColumn column = getColumn();
        return column == null ? null : column.getDataType();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public synchronized List<String> getPossibleValues() {
        if (possibleValues == null) {
            setPossibleValues(EMPTY_LIST);
            List<String> values;
            DBColumn column = getColumn();
            if (column != null) {
                if (column.isForeignKey()) {
                    DBColumn foreignKeyColumn = column.getForeignKeyColumn();
                    values = DatasetEditorUtils.loadDistinctColumnValues(foreignKeyColumn);
                } else {
                    values = DatasetEditorUtils.loadDistinctColumnValues(column);
                }

                if (values != null) {
                    DataEditorSettings dataEditorSettings = DataEditorSettings.getInstance(column.getProject());
                    int maxElementCount = dataEditorSettings.getValueListPopupSettings().getElementCountThreshold();
                    if (values.size() > maxElementCount) values.clear();
                    possibleValues = values;
                }
            }
        }
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public void dispose() {
        if (possibleValues != null) possibleValues.clear();
    }

    public boolean isSortable() {
        DBColumn column = getColumn();
        DBDataType type = column == null ? null : column.getDataType();
        return type != null && type.isNative() &&
                type.getGenericDataType().is(
                        GenericDataType.LITERAL,
                        GenericDataType.NUMERIC,
                        GenericDataType.DATE_TIME);
    }

}
