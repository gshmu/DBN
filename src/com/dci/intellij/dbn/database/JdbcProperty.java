package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.Nullable;

public enum JdbcProperty implements Property {
    MD_CATALOGS("Catalogs", true),
    MD_SCHEMAS("Schemas", true),
    MD_TABLES("Tables", true),
    MD_VIEWS("Views", true),
    MD_COLUMNS("Columns", true),
    MD_PSEUDO_COLUMNS("Pseudo columns", true),
    MD_INDEXES("Indexes", true),
    MD_PRIMARY_KEYS("Primary Keys", true),
    MD_IMPORTED_KEYS("Imported Keys", true),
    MD_FUNCTIONS("Functions", true),
    MD_FUNCTION_COLUMNS("Function columns", true),
    MD_PROCEDURES("Procedures", true),
    MD_PROCEDURE_COLUMNS("Procedure columns", true),
    SQL_DATASET_ALIASING("Dataset aliasing", true),

    CATALOG_AS_OWNER("Catalog as owner", false),
    ;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }

    private String description;
    private boolean feature;

    JdbcProperty(String description, boolean feature) {
        this.description = description;
        this.feature = feature;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFeature() {
        return feature;
    }

    public boolean isSupported(@Nullable ConnectionProvider connectionProvider) {
        return connectionProvider != null && isSupported(connectionProvider.getConnectionHandler());
    }

    public boolean isSupported(@Nullable DBObject object) {
        return Failsafe.check(object) && isSupported(object.getConnectionHandler());
    }
    public boolean isSupported(@Nullable ConnectionHandler connectionHandler) {
        if (Failsafe.check(connectionHandler)) {
            return connectionHandler.getCompatibility().is(this);
        }
        return false;
    }
}
