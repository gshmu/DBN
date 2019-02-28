package com.dci.intellij.dbn.database.postgres;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;

public class PostgresCompatibilityInterface extends DatabaseCompatibilityInterface {

    public static final QuoteDefinition IDENTIFIER_QUOTE_DEFINITION = new QuoteDefinition(new QuotePair('"', '"'));

    public PostgresCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

    @Override
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

    @Override
    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case SESSION_BROWSING: return true;
            case SESSION_KILL: return true;
            case SESSION_CURRENT_SQL: return true;
            case UPDATABLE_RESULT_SETS: return true;
            case CURRENT_SCHEMA: return true;
            case CONSTRAINT_MANIPULATION: return true;
            case READONLY_CONNECTIVITY: return true;
            default: return false;
        }
    }

    @Override
    public SessionStatus getSessionStatus(String statusName) {
        if (StringUtil.isEmpty(statusName)) return SessionStatus.INACTIVE;
        if (statusName.equalsIgnoreCase("active")) return SessionStatus.ACTIVE;
        if (statusName.equalsIgnoreCase("idle")) return SessionStatus.INACTIVE;
        return SessionStatus.SNIPED;
    }

    @Override
    public QuoteDefinition getIdentifierQuotes() {
        return IDENTIFIER_QUOTE_DEFINITION;
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
