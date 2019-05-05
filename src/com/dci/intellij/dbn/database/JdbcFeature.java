package com.dci.intellij.dbn.database;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.object.common.DBObject;
import org.jetbrains.annotations.Nullable;

public enum JdbcFeature implements Property {
    CATALOGS("Catalogs"),
    SCHEMAS("Schemas"),
    TABLES("Tables"),
    VIEWS("Views"),
    COLUMNS("Columns"),
    PSEUDO_COLUMNS("Pseudo columns"),
    INDEXES("Indexes"),
    PRIMARY_KEYS("Primary Keys"),
    IMPORTED_KEYS("Imported Keys"),
    FUNCTIONS("Functions"),
    FUNCTION_COLUMNS("Function columns"),
    PROCEDURES("Procedures"),
    PROCEDURE_COLUMNS("Procedure columns"),
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
            return connectionHandler.getJdbcFeatures().is(this);
        }
        return false;
    }
}
