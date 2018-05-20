package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetDataModelCell extends SortableDataModelCell {
    public ResultSetDataModelCell(ResultSetDataModelRow row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, null, columnInfo.getColumnIndex());
        DBDataType dataType = columnInfo.getDataType();
        if (!getRow().getModel().isInserting()) {
            Object userValue = dataType.getValueFromResultSet(resultSet, columnInfo.getResultSetColumnIndex());
            setUserValue(userValue);
        }
    }

    protected DBNConnection getConnection() {
        return getRow().getModel().getResultSet().getConnection();
    }


    @NotNull
    @Override
    public ResultSetDataModelRow getRow() {
        return (ResultSetDataModelRow) super.getRow();
    }
}
