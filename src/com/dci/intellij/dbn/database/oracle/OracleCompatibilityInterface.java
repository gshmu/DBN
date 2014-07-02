package com.dci.intellij.dbn.database.oracle;

import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;

public class OracleCompatibilityInterface extends DatabaseCompatibilityInterface {

    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return objectTypeId != DatabaseObjectTypeId.CHARSET;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case OBJECT_INVALIDATION: return true;
            case OBJECT_DEPENDENCIES: return true;
            case OBJECT_REPLACING: return true;
            case OBJECT_DDL_EXTRACTION: return true;
            case OBJECT_DISABLING: return true;
            case OBJECT_CHANGE_TRACING: return true;
            case AUTHID_METHOD_EXECUTION: return true;
            case FUNCTION_OUT_ARGUMENTS: return true;
            case DEBUGGING: return true;
            default: return false;
        }
    }

    public char getIdentifierQuotes() {
        return '"';
    }
}