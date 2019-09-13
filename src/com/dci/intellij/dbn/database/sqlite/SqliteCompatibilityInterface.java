package com.dci.intellij.dbn.database.sqlite;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseAttachmentHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

class SqliteCompatibilityInterface extends DatabaseCompatibilityInterface {

    private static final QuoteDefinition IDENTIFIER_QUOTE_DEFINITION = new QuoteDefinition(
            new QuotePair('"', '"'),
            new QuotePair('[', ']'),
            new QuotePair('`', '`'));

    SqliteCompatibilityInterface(DatabaseInterfaceProvider parent) {
        super(parent);
    }

    @Override
    public boolean supportsObjectType(DatabaseObjectTypeId objectTypeId) {
        return
            objectTypeId == DatabaseObjectTypeId.CONSOLE ||
            objectTypeId == DatabaseObjectTypeId.SCHEMA ||
            objectTypeId == DatabaseObjectTypeId.TABLE ||
            objectTypeId == DatabaseObjectTypeId.VIEW ||
            objectTypeId == DatabaseObjectTypeId.COLUMN ||
            objectTypeId == DatabaseObjectTypeId.CONSTRAINT ||
            objectTypeId == DatabaseObjectTypeId.INDEX ||
            objectTypeId == DatabaseObjectTypeId.SAVEPOINT ||
            objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER;
    }

    @Override
    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            case CONNECTION_ERROR_RECOVERY: return true;
            case OBJECT_SOURCE_EDITING: return true;
            default: return false;
        }
    }

    @Override
    public QuoteDefinition getIdentifierQuotes() {
        return IDENTIFIER_QUOTE_DEFINITION;
    }

    @Override
    public String getDefaultAlternativeStatementDelimiter() {
        return ";";
    }

    @Override
    public String getOrderByClause(String columnName, SortDirection sortDirection, boolean nullsFirst) {
        nullsFirst = (nullsFirst && sortDirection == SortDirection.ASCENDING) || (!nullsFirst && sortDirection == SortDirection.DESCENDING);
        return "(" + columnName + " is" + (nullsFirst ? "" : " not") + " null), " + columnName + " " + sortDirection.getSqlToken();
    }

    @Override
    public String getForUpdateClause() {
        return "";
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

    @Nullable
    @Override
    public DatabaseAttachmentHandler getDatabaseAttachmentHandler() {
        return (connection, filePath, schemaName) -> {
            //setAutoCommit(connection, false);
            try {
                //connection.rollback();
                Statement statement = connection.createStatement();
/*
                try {
                    statement.execute("end transaction");
                } catch (SQLException ignore) {}
*/
                statement.executeUpdate("attach database '" + filePath + "' as \"" + schemaName + "\"");
            } finally {
                //setAutoCommit(connection, true);
            }
        };
    }

    private void setAutoCommit(Connection connection, boolean autoCommit) throws SQLException {
        try {
            connection.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            if (connection.getAutoCommit() != autoCommit) {
                throw e;
            }

        }
    }
}
