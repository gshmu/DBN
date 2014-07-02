package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public interface DatabaseMessageParserInterface {

    @Nullable
    DatabaseObjectIdentifier identifyObject(String message);

    boolean isTimeoutException(SQLException e);

    boolean isModelException(SQLException e);
}
