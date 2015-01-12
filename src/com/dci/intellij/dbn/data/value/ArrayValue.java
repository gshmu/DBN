package com.dci.intellij.dbn.data.value;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class ArrayValue implements ValueAdapter<List<String>>{
    private Array array;
    private List<String> values;

    public ArrayValue() {
    }

    public ArrayValue(ResultSet resultSet, int columnIndex) throws SQLException {
        this.array = resultSet.getArray(columnIndex);
        if (array != null) {
            ResultSet arrayResultSet = array.getResultSet();
            while (arrayResultSet.next()) {
                if (values == null) values = new ArrayList<String>();
                Object object = arrayResultSet.getObject(2);
                values.add(object.toString());
            }
        }
    }

    @Nullable
    @Override
    public List<String> read() throws SQLException {
        return values;
    }

    @Override
    public void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable List<String> values) throws SQLException {
        try {
            this.values = values;
            if (values == null) {
                resultSet.updateArray(columnIndex, null);
            } else {
                String columnTypeName = resultSet.getMetaData().getColumnTypeName(columnIndex).substring(1);
                array = connection.createArrayOf(columnTypeName, values.toArray());
                resultSet.updateArray(columnIndex, array);
            }
        } catch (Throwable e) {
            if (e instanceof SQLException) throw (SQLException) e;
            throw new SQLException("Could not write array value. Your JDBC driver may not support this feature", e);
        }
    }

    @Override
    public String getDisplayValue() {
        return values == null ? "" : values.toString();
    }

    @Override
    public String toString() {
        return getDisplayValue();
    }
}
