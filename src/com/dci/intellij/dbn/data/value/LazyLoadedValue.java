package com.dci.intellij.dbn.data.value;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface LazyLoadedValue {
    public void updateValue(ResultSet resultSet, int columnIndex, String value) throws SQLException;
    public String loadValue() throws SQLException;
    public String loadValue(int maxSize) throws SQLException;
    public long size() throws SQLException;
    public String getDisplayValue();
}
