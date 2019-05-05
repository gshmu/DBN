package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseAttachmentHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

public abstract class DatabaseCompatibilityInterface {
    private DatabaseInterfaceProvider provider;

    public DatabaseCompatibilityInterface(DatabaseInterfaceProvider parent) {
        this.provider = parent;
    }

    @NotNull
    public static DatabaseCompatibilityInterface getInstance(DBObject object) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        return getInstance(connectionHandler);
    }

    public static DatabaseCompatibilityInterface getInstance(@NotNull ConnectionHandler connectionHandler) {
        return connectionHandler.getInterfaceProvider().getCompatibilityInterface();
    }

    public abstract boolean supportsObjectType(DatabaseObjectTypeId objectTypeId);

    public abstract boolean supportsFeature(DatabaseFeature feature);

    public abstract QuoteDefinition getIdentifierQuotes();
    public QuotePair getDefaultIdentifierQuotes() {
        return getIdentifierQuotes().getDefaultQuotes();
    }

    @Nullable
    public String getDatabaseLogName() {
        return null;
    }

    public abstract String getDefaultAlternativeStatementDelimiter();

    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        return columnName + " " + sortDirection.getSqlToken() + " nulls " + (nullsFirst ? " first" : " last");
    }

    public String getForUpdateClause() {
        return " for update";
    }

    public String getSessionBrowserColumnName(String columnName) {
        return columnName;
    }

    public abstract SessionStatus getSessionStatus(String statusName);

    public abstract String getExplainPlanStatementPrefix();

    @Nullable
    public DatabaseAttachmentHandler getDatabaseAttachmentHandler() {
        return null;
    };

    public  <T> T attempt(ThrowableCallable<T, SQLException> callable) throws SQLException {
        try {
            // TODO check is supported
            return callable.call();
        } catch (SQLFeatureNotSupportedException e) {
            // TODO mark unsupported jdbc
            return null;
        }
    }
}
