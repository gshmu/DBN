package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import com.dci.intellij.dbn.common.util.TransientId;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseCompatibility extends PropertyHolderImpl<JdbcFeature> {

    private String identifierQuote;
    private Map<TransientId, DatabaseActivityTrace> activityTraces = new HashMap<>();

    private DatabaseCompatibility() {}

    public static DatabaseCompatibility all() {
        DatabaseCompatibility compatibility = new DatabaseCompatibility();
        // mark all features as supported
        for (JdbcFeature value : JdbcFeature.values()) {
            compatibility.set(value, true);
        }
        return compatibility;
    }

    public static DatabaseCompatibility none() {
        return new DatabaseCompatibility();
    }

    public void markUnsupported(JdbcFeature feature) {
        set(feature, false);
    }

    public boolean isSupported(JdbcFeature feature) {
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
    protected JdbcFeature[] properties() {
        return JdbcFeature.values();
    }

    public void read(DatabaseMetaData metaData) throws SQLException {
        String quoteString = metaData.getIdentifierQuoteString();
        identifierQuote = quoteString == null ? "" : quoteString.trim();

        //TODO JdbcFeature.SQL_DATASET_ALIASING (identify by database type?)
    }
}
