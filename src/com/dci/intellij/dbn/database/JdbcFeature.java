package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.Nullable;

public enum JdbcFeature implements Property {
    MD_CATALOGS("Catalogs"),
    MD_SCHEMAS("Schemas"),
    MD_TABLES("Tables"),
    MD_VIEWS("Views"),
    MD_COLUMNS("Columns"),
    MD_PSEUDO_COLUMNS("Pseudo columns"),
    MD_INDEXES("Indexes"),
    MD_PRIMARY_KEYS("Primary Keys"),
    MD_IMPORTED_KEYS("Imported Keys"),
    MD_FUNCTIONS("Functions"),
    MD_FUNCTION_COLUMNS("Function columns"),
    MD_PROCEDURES("Procedures"),
    MD_PROCEDURE_COLUMNS("Procedure columns"),
    SQL_DATASET_ALIASING("Dataset aliasing"),
    ;

    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }

    private String description;

    JdbcFeature(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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
