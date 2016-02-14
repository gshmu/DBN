package com.dci.intellij.dbn.database.sqlite;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.DatabaseAttachmentHandler;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterfaceProvider;
import com.dci.intellij.dbn.database.DatabaseObjectTypeId;
import com.dci.intellij.dbn.editor.session.SessionStatus;
import com.dci.intellij.dbn.language.common.QuoteDefinition;
import com.dci.intellij.dbn.language.common.QuotePair;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteCompatibilityInterface extends DatabaseCompatibilityInterface {

    public static final QuoteDefinition IDENTIFIER_QUOTE_DEFINITION = new QuoteDefinition(
            new QuotePair('"', '"'),
            new QuotePair('[', ']'),
            new QuotePair('`', '`'));

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
            objectTypeId == DatabaseObjectTypeId.INDEX ||
            objectTypeId == DatabaseObjectTypeId.SAVEPOINT ||
            objectTypeId == DatabaseObjectTypeId.DATASET_TRIGGER;
    }

    public boolean supportsFeature(DatabaseFeature feature) {
        switch (feature) {
            default: return false;
        }
    }

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

    @Override
    public DatabaseAttachmentHandler getDatabaseAttachmentHandler() {
        return new DatabaseAttachmentHandler() {
            @Override
            public void attachDatabase(Connection connection, String filePath, String schemaName) throws SQLException {
                boolean autoCommit = connection.getAutoCommit();
                ConnectionUtil.setAutoCommit(connection, false);
                connection.setAutoCommit(false);
                try {
                    connection.rollback();
                    Statement statement = connection.createStatement();
                    try {
                        statement.execute("end transaction");
                    } catch (SQLException ignore) {}
                    statement.executeUpdate("attach database '" + filePath + "' as " + schemaName);
                } finally {
                    ConnectionUtil.setAutoCommit(connection, autoCommit);
                }
            }
        };
    }
}
