package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;

public interface Invalidable extends Resource {
    boolean isValid() throws SQLException;

    boolean isInvalid() throws SQLException;

    boolean isInvalidInner() throws SQLException;

    void invalidate() throws SQLException;

    void invalidateInner() throws SQLException;
}
