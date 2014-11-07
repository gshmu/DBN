package com.dci.intellij.dbn.database;

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
    EXECUTION_LOGGING("Execution logging"),
    ;

    private String description;

    DatabaseFeature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
