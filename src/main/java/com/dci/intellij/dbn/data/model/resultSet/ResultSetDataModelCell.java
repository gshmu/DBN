package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nn;
import static com.dci.intellij.dbn.editor.data.model.RecordStatus.INSERTING;

public class ResultSetDataModelCell<
        R extends ResultSetDataModelRow<M, ? extends ResultSetDataModelCell<R, M>>,
        M extends ResultSetDataModel<R, ? extends ResultSetDataModelCell<R, M>>>
        extends SortableDataModelCell<R, M> {

    public ResultSetDataModelCell(R row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, null, columnInfo.getIndex());
        DBDataType dataType = columnInfo.getDataType();
        if (!getModel().is(INSERTING)) {
            Object userValue = dataType.getValueFromResultSet(resultSet, columnInfo.getResultSetIndex());
            setUserValue(userValue);
        }
    }

    @NotNull
    @Override
    public M getModel() {
        return super.getModel();
    }

    @NotNull
    @Override
    public R getRow() {
        return super.getRow();
    }

    protected DBNConnection getConnection() {
        return nn(getRow().getModel().getResultSet().getConnection());
    }
}
