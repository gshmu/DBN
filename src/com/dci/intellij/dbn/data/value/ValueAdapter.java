package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface ValueAdapter<T> {
    T read() throws SQLException;
    void write(Connection connection, ResultSet resultSet, int columnIndex, T value) throws SQLException;
    void copy(Connection connection, ResultSet resultSet, int columnIndex, ValueAdapter<T> source) throws SQLException;
    public String getDisplayValue();
}
