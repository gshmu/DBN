package com.dci.intellij.dbn.editor.data.model;

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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DatasetEditorColumnInfo extends ResultSetColumnInfo {
    private final boolean primaryKey;
    private final boolean foreignKey;
    private final boolean identity;

    @EqualsAndHashCode.Exclude
    private final DBObjectRef<DBColumn> columnRef;

    @EqualsAndHashCode.Exclude
    private volatile List<String> possibleValues;

    @EqualsAndHashCode.Exclude
    private final RefreshableValue<Boolean> auditColumn = new RefreshableValue<Boolean>(2000) {
        @Override
        protected Boolean load() {
            DBColumn column = getColumn();
            Project project = column.getProject();
            return DataGridSettings.getInstance(project).getAuditColumnSettings().isAuditColumn(column.getName());
        }
    };

    DatasetEditorColumnInfo(DBColumn column, int columnIndex, int resultSetColumnIndex) {
        super(column.getName(), column.getDataType(), columnIndex, resultSetColumnIndex);
        this.columnRef = DBObjectRef.of(column);
        this.primaryKey = column.isPrimaryKey();
        this.foreignKey = column.isForeignKey();
        this.identity = column.isIdentity();
    }

    @NotNull
    public DBColumn getColumn() {
        return DBObjectRef.ensure(columnRef);
    }

    public boolean isAuditColumn() {
        return auditColumn.get();
    }

    public List<String> getPossibleValues() {
        if (possibleValues == null) {
            synchronized (this) {
                if (possibleValues == null) {
                    possibleValues = Collections.emptyList();
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
            }
        }
        return possibleValues;
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
