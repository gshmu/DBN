package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class JdbcFeatures extends PropertyHolderImpl<JdbcFeature> {
    private JdbcFeatures() {}

    public static JdbcFeatures all() {
        JdbcFeatures features = new JdbcFeatures();
        // mark all features as supported
        for (JdbcFeature value : JdbcFeature.values()) {
            features.set(value, true);
        }
        return features;
    }

    public static JdbcFeatures none() {
        return new JdbcFeatures();
    }


    public boolean supportsJdbcFeature(JdbcFeature feature) {
        return is(feature);
    };

    @Override
    protected JdbcFeature[] properties() {
        return JdbcFeature.values();
    }
}
