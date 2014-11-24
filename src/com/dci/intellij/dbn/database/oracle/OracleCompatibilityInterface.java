package com.dci.intellij.dbn.database.oracle;

import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;

public class OracleCompatibilityInterface extends DatabaseCompatibilityInterface {

    public OracleCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

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
            case EXPLAIN_PLAN: return true;
            case DATABASE_LOGGING: return true;
            default: return false;
        }
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
        return "explain plan for ";
    }

    @Override
    public String getDatabaseLogName() {
        return "DBMS Output";
    }
}