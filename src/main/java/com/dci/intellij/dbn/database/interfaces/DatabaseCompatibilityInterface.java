package com.dci.intellij.dbn.database.interfaces;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseAttachmentHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.database.DatabaseCompatibility;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.database.JdbcProperty;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public abstract class DatabaseCompatibilityInterface implements DatabaseInterface{
    private final DatabaseInterfaces interfaces;
    private final Set<DatabaseObjectTypeId> supportedObjectTypes = new HashSet<>(getSupportedObjectTypes());
    private final Set<DatabaseFeature> supportedFeatures = new HashSet<>(getSupportedFeatures());

    public DatabaseCompatibilityInterface(DatabaseInterfaces parent) {
        this.interfaces = parent;
    }

    protected abstract List<DatabaseObjectTypeId> getSupportedObjectTypes();
    protected abstract List<DatabaseFeature> getSupportedFeatures();

    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return supportedObjectTypes.contains(objectTypeId);
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        return supportedFeatures.contains(feature);
    }

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

    public  <T> T attempt(JdbcProperty feature, Callable<T> loader) throws SQLException {
        ConnectionHandler connection = ConnectionHandler.local();
        DatabaseCompatibility compatibility = connection.getCompatibility();
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
