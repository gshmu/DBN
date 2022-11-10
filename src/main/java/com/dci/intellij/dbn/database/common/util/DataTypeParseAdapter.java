package com.dci.intellij.dbn.database.common.util;

import java.sql.SQLException;

public interface DataTypeParseAdapter<T> {
    String toString(T object);
    T parse(String string) throws SQLException;
}
