package com.dci.intellij.dbn.database.generic;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;

public class GenericCompatibilityInterface extends DatabaseCompatibilityInterface {
    private static final QuoteDefinition IDENTIFIER_QUOTE_DEFINITION = new QuoteDefinition(new QuotePair('"', '"'));

    GenericCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

    @Override
    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return
            objectTypeId == DatabaseObjectTypeId.SCHEMA ||
            objectTypeId == DatabaseObjectTypeId.TABLE ||
            objectTypeId == DatabaseObjectTypeId.VIEW ||
            objectTypeId == DatabaseObjectTypeId.COLUMN ||
            objectTypeId == DatabaseObjectTypeId.CONSTRAINT ||
            objectTypeId == DatabaseObjectTypeId.INDEX ||
            objectTypeId == DatabaseObjectTypeId.TRIGGER ||
            objectTypeId == DatabaseObjectTypeId.FUNCTION ||
            objectTypeId == DatabaseObjectTypeId.PROCEDURE ||
            objectTypeId == DatabaseObjectTypeId.ARGUMENT;
    }

    @Override
    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case OBJECT_INVALIDATION: return false;
            case OBJECT_DEPENDENCIES: return false;
            case OBJECT_REPLACING: return false;
            case OBJECT_DDL_EXTRACTION: return false;
            case OBJECT_DISABLING: return false;
            case AUTHID_METHOD_EXECUTION: return false;
            case FUNCTION_OUT_ARGUMENTS: return false;
            case DEBUGGING: return false;
            default: return false;
        }
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
    public SessionStatus getSessionStatus(String statusName) {
        return StringUtil.isEmpty(statusName) ? SessionStatus.INACTIVE : SessionStatus.ACTIVE;
    }

    @Override
    public String getExplainPlanStatementPrefix() {
        return null;
    }
}
