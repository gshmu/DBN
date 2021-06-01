package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.util.TransientId;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseCompatibility extends PropertyHolderImpl<JdbcProperty> {

    private String identifierQuote;
    private final Map<TransientId, DatabaseActivityTrace> activityTraces = new HashMap<>();

    private DatabaseCompatibility() {}

    public static DatabaseCompatibility allFeatures() {
        DatabaseCompatibility compatibility = new DatabaseCompatibility();
        // mark all features as supported
        for (JdbcProperty property : JdbcProperty.values()) {
            if (property.isFeature()) {
                compatibility.set(property, true);
            }
        }
        return compatibility;
    }

    public static DatabaseCompatibility noFeatures() {
        return new DatabaseCompatibility();
    }

    public void markUnsupported(JdbcProperty feature) {
        set(feature, false);
    }

    public boolean isSupported(JdbcProperty feature) {
        return is(feature);
    };

    public DatabaseActivityTrace getActivityTrace(TransientId operationId) {
        DatabaseActivityTrace trace = activityTraces.get(operationId);
        if (trace == null) {
            synchronized (this) {
                trace = activityTraces.get(operationId);
                if (trace == null) {
                    trace = new DatabaseActivityTrace();
                    activityTraces.put(operationId, trace);
                }
            }
        }
        return trace;
    }

    public String getIdentifierQuote() {
        return identifierQuote;
    }

    @Override
    protected JdbcProperty[] properties() {
        return JdbcProperty.values();
    }

    public void read(DatabaseMetaData metaData) throws SQLException {
        String quoteString = metaData.getIdentifierQuoteString();
        identifierQuote = quoteString == null ? "" : quoteString.trim();

        //TODO JdbcProperty.SQL_DATASET_ALIASING (identify by database type?)
    }
}
