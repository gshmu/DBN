package com.dci.intellij.dbn.data.value;

import java.sql.SQLException;

public interface LargeObjectValue extends ValueAdapter<String> {
    public String read(int maxSize) throws SQLException;
    public long size() throws SQLException;
}
