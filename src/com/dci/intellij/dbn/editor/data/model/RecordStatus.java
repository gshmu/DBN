package com.dci.intellij.dbn.editor.data.model;


import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;

public enum RecordStatus implements Property {
    INSERTING,
    INSERTED,
    DELETED,
    MODIFIED,
    DISPOSED,
    ;

    public enum Group implements PropertyGroup{
        DISTINCT
    }


    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }

    @Override
    public PropertyGroup group() {
        return Group.DISTINCT;
    }


}
