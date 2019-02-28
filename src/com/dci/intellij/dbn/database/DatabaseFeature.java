package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.Nullable;

public enum DatabaseFeature {
    OBJECT_REPLACING("Replacing existing objects via DDL"),
    OBJECT_DEPENDENCIES("Object dependencies"),
    OBJECT_DDL_EXTRACTION("Object DDL extraction"),
    OBJECT_INVALIDATION("Object invalidation"),
    OBJECT_DISABLING("Disabling objects"),
    OBJECT_CHANGE_TRACING("Tracing objects changes"),
    AUTHID_METHOD_EXECUTION("AUDHID method execution (execution on different schema)"),
    FUNCTION_OUT_ARGUMENTS("OUT arguments for functions"),
    DEBUGGING("Program execution debugging"),
    EXPLAIN_PLAN("Statement explain plan"),
    DATABASE_LOGGING("Database logging"),
    SESSION_CURRENT_SQL("Session current SQL"),
    SESSION_BROWSING("Session browsing"),
    SESSION_KILL("Kill session"),
    SESSION_DISCONNECT("Disconnect session"),
    SESSION_INTERRUPTION_TIMING("Session interruption timing"),
    CONNECTION_ERROR_RECOVERY("Recover connection transaction after error"),
    UPDATABLE_RESULT_SETS("Updatable result sets"),
    CURRENT_SCHEMA("Current schema initializing"),
    CONSTRAINT_MANIPULATION("Constraint manipulation"),
    ;

    private String description;

    DatabaseFeature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSupported(@Nullable ConnectionProvider connectionProvider) {
        return connectionProvider != null && isSupported(connectionProvider.getConnectionHandler());
    }

    public boolean isSupported(@Nullable DBObject object) {
        return Failsafe.check(object) && isSupported(object.getConnectionHandler());
    }
    public boolean isSupported(@Nullable ConnectionHandler connectionHandler) {
        if (Failsafe.check(connectionHandler)) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            return compatibilityInterface != null && compatibilityInterface.supportsFeature(this);
        }
        return false;
    }
}
