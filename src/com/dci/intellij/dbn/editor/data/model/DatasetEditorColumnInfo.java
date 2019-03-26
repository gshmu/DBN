package com.dci.intellij.dbn.editor.data.model;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.common.util.RefreshableValue;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetColumnInfo;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.editor.data.DatasetEditorUtils;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DatasetEditorColumnInfo extends ResultSetColumnInfo {
    private static final List<String> EMPTY_LIST = new ArrayList<String>(0);
    private List<String> possibleValues;
    private DBObjectRef<DBColumn> columnRef;
    private int columnIndex;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private RefreshableValue<Boolean> isTrackingColumn = new RefreshableValue<Boolean>(2000) {
        @Override
        protected Boolean load() {
            DBColumn column = getColumn();
            Project project = column.getProject();
            return DataGridSettings.getInstance(project).getTrackingColumnSettings().isTrackingColumn(column.getName());
        }
    };

    DatasetEditorColumnInfo(DBColumn column, int columnIndex, int resultSetColumnIndex) {
        super(columnIndex, resultSetColumnIndex);
        this.columnRef = DBObjectRef.from(column);
        this.columnIndex = columnIndex;
        this.isPrimaryKey = column.isPrimaryKey();
        this.isForeignKey = column.isForeignKey();
    }

    @NotNull
    public DBColumn getColumn() {
        return Failsafe.nn(DBObjectRef.get(columnRef));
    }

    @Override
    public String getName() {
        return columnRef.objectName;
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    @NotNull
    public DBDataType getDataType() {
        return getColumn().getDataType();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public boolean isTrackingColumn() {
        return isTrackingColumn.get();
    }

    public List<String> getPossibleValues() {
        Synchronized.run(this,
                () -> possibleValues == null,
                () -> {
                    possibleValues = EMPTY_LIST;
                    List<String> values = null;
                    DBColumn column = getColumn();
                    if (column.isForeignKey()) {
                        DBColumn foreignKeyColumn = column.getForeignKeyColumn();
                        if (foreignKeyColumn != null) {
                            values = DatasetEditorUtils.loadDistinctColumnValues(foreignKeyColumn);
                        }
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
            );
        return possibleValues;
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = possibleValues;
    }

    @Override
    public void dispose() {
        if (possibleValues != null) possibleValues.clear();
    }

    @Override
    public boolean isSortable() {
        DBDataType type = getColumn().getDataType();
        return type != null && type.isNative() &&
                type.getGenericDataType().is(
                        GenericDataType.LITERAL,
                        GenericDataType.NUMERIC,
                        GenericDataType.DATE_TIME);
    }

}
