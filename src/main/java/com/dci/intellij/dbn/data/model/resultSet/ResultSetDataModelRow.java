package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelRow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ResultSetDataModelRow<
        M extends ResultSetDataModel<? extends ResultSetDataModelRow<M, C>, C>,
        C extends ResultSetDataModelCell<? extends ResultSetDataModelRow<M, C>, M>>
        extends SortableDataModelRow<M, C> {

    private int resultSetRowIndex;

    public ResultSetDataModelRow(M model, ResultSet resultSet, int resultSetRowIndex) throws SQLException {
        super(model);
        this.resultSetRowIndex = resultSetRowIndex;
        for (int i = 0; i < model.getColumnCount(); i++) {
            ResultSetColumnInfo columnInfo = (ResultSetColumnInfo) getModel().getColumnInfo(i);
            C cell = createCell(resultSet, columnInfo);
            addCell(cell);
        }
    }

    @NotNull
    @Override
    public M getModel() {
        return super.getModel();
    }

    public int getResultSetRowIndex() {
        return resultSetRowIndex;
    }

    public void shiftResultSetRowIndex(int delta) {
        resultSetRowIndex = resultSetRowIndex + delta;
    }

    @NotNull
    protected C createCell(ResultSet resultSet, ColumnInfo columnInfo) throws SQLException {
        return (C) new ResultSetDataModelCell(this, resultSet, (ResultSetColumnInfo) columnInfo);
    }

    @Nullable
    @Override
    public C getCellAtIndex(int index) {
        return super.getCellAtIndex(index);
    }
}
