package com.dci.intellij.dbn.database.sqlite;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;

public class SqliteCompatibilityInterface extends DatabaseCompatibilityInterface {

    public SqliteCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return
            objectTypeId == DatabaseObjectTypeId.SCHEMA ||
            objectTypeId == DatabaseObjectTypeId.TABLE ||
            objectTypeId == DatabaseObjectTypeId.VIEW ||
            objectTypeId == DatabaseObjectTypeId.COLUMN ||
            objectTypeId == DatabaseObjectTypeId.CONSTRAINT ||
            objectTypeId == DatabaseObjectTypeId.INDEX;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            default: return false;
        }
    }

    public char getIdentifierQuotes() {
        return '`';
    }

    @Override
    public String getDefaultAlternativeStatementDelimiter() {
        return "$$";
    }

    @Override
    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        nullsFirst = (nullsFirst && sortDirection == SortDirection.ASCENDING) || (!nullsFirst && sortDirection == SortDirection.DESCENDING);
        return "(" + columnName + " is" + (nullsFirst ? "" : " not") + " null), " + columnName + " " + sortDirection.getSqlToken();
    }

    @Override
    public String getExplainPlanStatementPrefix() {
        return null;
    }

    @Override
    public SessionStatus getSessionStatus(String statusName) {
        if (StringUtil.isEmpty(statusName)) return SessionStatus.INACTIVE;
        else return SessionStatus.ACTIVE;
    }

    @Override
    public int getColumnIndexPadding() {
        return 1;
    }
}
