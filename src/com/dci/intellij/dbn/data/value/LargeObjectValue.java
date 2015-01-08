package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class LargeObjectValue implements ValueAdapter<String> {
    public abstract String read(int maxSize) throws SQLException;
    public abstract long size() throws SQLException;
    public abstract String getContentTypeName();
    public abstract void release();

    public void copy(Connection connection, ResultSet resultSet, int columnIndex, ValueAdapter<String> source) throws SQLException {
        String value = source.read();
        write(connection, resultSet, columnIndex, value);
    }

}
