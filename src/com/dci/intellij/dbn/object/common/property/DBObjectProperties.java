package com.dci.intellij.dbn.object.common.property;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class DBObjectProperties extends PropertyHolderBase.LongStore<DBObjectProperty> {
    @Override
    protected DBObjectProperty[] properties() {
        return DBObjectProperty.values();
    }
}
