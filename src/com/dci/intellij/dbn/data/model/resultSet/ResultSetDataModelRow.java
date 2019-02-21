package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelRow;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ResultSetDataModelRow<T extends ResultSetDataModelCell> extends SortableDataModelRow<T> {
    private int resultSetRowIndex;

    public ResultSetDataModelRow(ResultSetDataModel model, ResultSet resultSet, int resultSetRowIndex) throws SQLException {
        super(model);
        this.resultSetRowIndex = resultSetRowIndex;
        for (int i = 0; i < model.getColumnCount(); i++) {
            ResultSetColumnInfo columnInfo = (ResultSetColumnInfo) getModel().getColumnInfo(i);
            T cell = createCell(resultSet, columnInfo);
            addCell(cell);
        }
    }

    public int getResultSetRowIndex() {
        return resultSetRowIndex;
    }

    public void shiftResultSetRowIndex(int delta) {
        resultSetRowIndex = resultSetRowIndex + delta;
    }

    @NotNull
    @Override
    public ResultSetDataModel getModel() {
        return (ResultSetDataModel) super.getModel();
    }

    @NotNull
    protected  T createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return (T) new ResultSetDataModelCell(this, resultSet, (ResultSetColumnInfo) columnInfo);
    }
}
