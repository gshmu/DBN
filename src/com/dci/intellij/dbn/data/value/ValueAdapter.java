package com.dci.intellij.dbn.data.value;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.Nullable;

public interface ValueAdapter<T> {
    @Nullable T read() throws SQLException;
    void write(Connection connection, ResultSet resultSet, int columnIndex, @Nullable T value) throws SQLException;
    public String getDisplayValue();
}
