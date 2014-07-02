package com.dci.intellij.dbn.data.model.resultSet;

import com.dci.intellij.dbn.data.model.sortable.SortableDataModelCell;
import com.dci.intellij.dbn.data.type.DBDataType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResultSetDataModelCell extends SortableDataModelCell {
    public ResultSetDataModelCell(ResultSetDataModelRow row, ResultSet resultSet, ResultSetColumnInfo columnInfo) throws SQLException {
        super(row, null, columnInfo.getColumnIndex());
        DBDataType dataType = columnInfo.getDataType();
        Object userValue = dataType.getValueFromResultSet(resultSet, columnInfo.getResultSetColumnIndex());
        setUserValue(userValue);
    }

    @Override
    public ResultSetDataModelRow getRow() {
        return (ResultSetDataModelRow) super.getRow();
    }
}
