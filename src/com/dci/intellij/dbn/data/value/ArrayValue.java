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

    public ArrayValue() {
    }

    public ArrayValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.array = resultSet.getArray(columnIndex);
        ResultSet arrayResultSet = array.getResultSet();
        while (arrayResultSet.next()) {
            Object object = arrayResultSet.getObject(2);
            values.add(object.toString());
        }
    }

    @Override
    public List<String> read() throws SQLException {
        return values;
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, List<String> values) throws SQLException {
        try {
            this.values = values;
            array = connection.createArrayOf(array.getBaseTypeName(), values.toArray());
            resultSet.updateArray(columnIndex, array);
        } catch (Throwable e) {
            if (e instanceof SQLException) throw e;
            throw new SQLException("Could not write array value. Your JDBC driver may not support this feature", e);
        }
    }

    @Override
    public void copy(Connection connection, ResultSet resultSet, int columnIndex, ValueAdapter<List<String>> source) throws SQLException {
        ArrayValue sourceValue = (ArrayValue) source;
        try {
            this.values = source.read();
            array = connection.createArrayOf(sourceValue.array.getBaseTypeName(), values.toArray());
            resultSet.updateArray(columnIndex, array);
        } catch (Throwable e) {
            if (e instanceof SQLException) throw e;
            throw new SQLException("Could not write array value. Your JDBC driver may not support this feature", e);
        }

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
