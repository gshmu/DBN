package com.dci.intellij.dbn.data.value;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArrayValue implements ValueAdapter<List<String>>{
    private Array array;
    private List<String> values = new ArrayList<String>();

    public ArrayValue(Array array) throws SQLException {
        this.array = array;
        ResultSet resultSet = array.getResultSet();
        while (resultSet.next()) {
            Object object = resultSet.getObject(2);
            values.add(object.toString());
        }
    }

    @Override
    public List<String> read() throws SQLException {
        return values;
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, List<String> values) throws SQLException {
        this.values = values;
        this.array.getBaseTypeName();
        Array array = connection.createArrayOf(this.array.getBaseTypeName(), values.toArray());
        resultSet.updateArray(columnIndex, array);
    }

    @Override
    public String getDisplayValue() {
        return values.toString();
    }

    @Override
    public String toString() {
        return getDisplayValue();
    }
}
