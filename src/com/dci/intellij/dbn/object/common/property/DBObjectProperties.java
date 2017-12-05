package com.dci.intellij.dbn.object.common.property;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class DBObjectProperties extends PropertyHolderImpl<DBObjectProperty> {
    @Override
    protected DBObjectProperty[] getProperties() {
        return DBObjectProperty.values();
    }
}
