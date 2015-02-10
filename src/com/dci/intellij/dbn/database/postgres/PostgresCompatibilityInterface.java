package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;

public class PostgresCompatibilityInterface extends DatabaseCompatibilityInterface {

    public PostgresCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

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
            objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER ||
            //objectTypeId == DatabaseObjectTypeId.DATABASE_TRIGGER ||
            objectTypeId == DatabaseObjectTypeId.FUNCTION ||
            objectTypeId == DatabaseObjectTypeId.ARGUMENT ||
            objectTypeId == DatabaseObjectTypeId.SEQUENCE ||
            objectTypeId == DatabaseObjectTypeId.SYSTEM_PRIVILEGE ||
            objectTypeId == DatabaseObjectTypeId.GRANTED_PRIVILEGE;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case SESSION_BROWSING: return true;
            case SESSION_KILL: return true;
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

    @Override
    public String getDefaultAlternativeStatementDelimiter() {
        return null;
    }

    @Override
    public String getExplainPlanStatementPrefix() {
        return "explain analyze verbose ";
    }
}
