package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.object.common.DBObject;

public abstract class DatabaseCompatibilityInterface {
    private DatabaseInterfaceProvider provider;

    public DatabaseCompatibilityInterface(DatabaseInterfaceProvider parent) {
        this.provider = parent;
    }

    public static DatabaseCompatibilityInterface getInstance(DBObject object) {
        return getInstance(object.getConnectionHandler());
    }

    public static DatabaseCompatibilityInterface getInstance(ConnectionHandler connectionHandler) {
        return connectionHandler.getInterfaceProvider().getCompatibilityInterface();
    }

    public abstract boolean supportsObjectType(DatabaseObjectTypeId objectTypeId);

    public abstract boolean supportsFeature(DatabaseFeature feature);

    public abstract char getIdentifierQuotes();

    public abstract String getDefaultAlternativeStatementDelimiter();

    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        return columnName + " " + sortDirection.getSqlToken() + " nulls " + (nullsFirst ? " first" : " last");
    }

    public abstract String getExplainPlanStatementPrefix();
}
