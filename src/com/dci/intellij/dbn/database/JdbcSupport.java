package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class JdbcSupport extends PropertyHolderImpl<JdbcFeature> {

    private String identifierQuote;

    private JdbcSupport() {}

    public static JdbcSupport all() {
        JdbcSupport features = new JdbcSupport();
        // mark all features as supported
        for (JdbcFeature value : JdbcFeature.values()) {
            features.set(value, true);
        }
        return features;
    }

    public static JdbcSupport none() {
        return new JdbcSupport();
    }


    public boolean supports(JdbcFeature feature) {
        return is(feature);
    };

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
