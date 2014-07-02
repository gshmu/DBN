package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;

public class PostgresCompatibilityInterface extends DatabaseCompatibilityInterface {

    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return
            objectTypeId == DatabaseObjectTypeId.CHARSET ||
            objectTypeId == DatabaseObjectTypeId.USER ||
            objectTypeId == DatabaseObjectTypeId.SCHEMA ||
            objectTypeId == DatabaseObjectTypeId.TABLE ||
            objectTypeId == DatabaseObjectTypeId.VIEW ||
            objectTypeId == DatabaseObjectTypeId.COLUMN ||
            objectTypeId == DatabaseObjectTypeId.CONSTRAINT ||
            objectTypeId == DatabaseObjectTypeId.INDEX ||
            objectTypeId == DatabaseObjectTypeId.TRIGGER ||
            objectTypeId == DatabaseObjectTypeId.FUNCTION ||
            objectTypeId == DatabaseObjectTypeId.ARGUMENT ||
            objectTypeId == DatabaseObjectTypeId.PRIVILEGE ||
            objectTypeId == DatabaseObjectTypeId.SEQUENCE ||
            objectTypeId == DatabaseObjectTypeId.GRANTED_PRIVILEGE;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case OBJECT_INVALIDATION: return false;
            case OBJECT_DEPENDENCIES: return false;
            case OBJECT_REPLACING: return false;
            case OBJECT_DDL_EXTRACTION: return false;
            case OBJECT_DISABLING: return false;
            case OBJECT_CHANGE_TRACING: return false;
            case AUTHID_METHOD_EXECUTION: return false;
            case FUNCTION_OUT_ARGUMENTS: return false;
            case DEBUGGING: return false;
            default: return false;
        }
    }

    public boolean supportsInvalidObjects() {
        return false;
    }

    public boolean supportsReplacingObjects() {
        return false;
    }

    public char getIdentifierQuotes() {
        return '"';
    }
}
