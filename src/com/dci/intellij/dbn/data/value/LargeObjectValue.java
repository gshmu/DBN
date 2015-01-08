package com.dci.intellij.dbn.data.value;

import java.sql.SQLException;

public interface LargeObjectValue extends ValueAdapter<String> {
    String read(int maxSize) throws SQLException;
    long size() throws SQLException;
    String getContentTypeName();
    void release();
}
