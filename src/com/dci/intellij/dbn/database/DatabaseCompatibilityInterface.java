package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseAttachmentHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;
import com.dci.intellij.dbn.object.common.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

@Slf4j
public abstract class DatabaseCompatibilityInterface implements DatabaseInterface{
    private final DatabaseInterfaceProvider provider;

    public DatabaseCompatibilityInterface(DatabaseInterfaceProvider parent) {
        this.provider = parent;
    }

    @NotNull
    public static DatabaseCompatibilityInterface getInstance(DBObject object) {
        ConnectionHandler connectionHandler = object.getConnection();
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

    public  <T> T attempt(JdbcProperty feature, ThrowableCallable<T, SQLException> loader) throws SQLException {
        ConnectionHandler connectionHandler = DatabaseInterface.getConnectionHandler();
        DatabaseCompatibility compatibility = connectionHandler.getCompatibility();
        try {
            if (compatibility.isSupported(feature)) {
                return loader.call();
            }
        } catch (SQLFeatureNotSupportedException | AbstractMethodError e) {
            log.warn("JDBC feature not supported " + feature + " (" + e.getMessage() + ")");
            compatibility.markUnsupported(feature);
        }
        return null;
    }
}
