package com.dci.intellij.dbn.object.common.property;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DBObjectProperty implements Property {
    // generic
    TEMPORARY,
    NAVIGABLE,
    EDITABLE,
    COMPILABLE,
    DISABLEABLE,
    REFERENCEABLE,
    SCHEMA_OBJECT,
    SYSTEM_OBJECT,

    DETERMINISTIC,
    COLLECTION,

    // schema
    USER_SCHEMA,
    EMPTY_SCHEMA,
    PUBLIC_SCHEMA,
    SYSTEM_SCHEMA,

    // column
    PRIMARY_KEY,
    FOREIGN_KEY,
    UNIQUE_KEY,
    NULLABLE,
    HIDDEN,
    UNIQUE,

    // argument
    INPUT,
    OUTPUT,

    // user, privileges
    EXPIRED,
    LOCKED,
    ADMIN_OPTION,
    DEFAULT_ROLE,

    // trigger
    FOR_EACH_ROW
    ;

    @Override
    public int index() {
        return PropertyHolderImpl.idx(this);
    }

    @Override
    public PropertyGroup group() {
        return null;
    }

    @Override
    public boolean implicit() {
        return false;
    }
}
