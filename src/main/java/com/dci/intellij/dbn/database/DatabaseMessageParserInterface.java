package com.dci.intellij.dbn.database;

import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public interface DatabaseMessageParserInterface extends DatabaseInterface{

    @Nullable
    DatabaseObjectIdentifier identifyObject(SQLException exception);

    boolean isTimeoutException(SQLException e);

    boolean isModelException(SQLException e);

    boolean isAuthenticationException(SQLException e);

    boolean isSuccessException(SQLException exception);
}
