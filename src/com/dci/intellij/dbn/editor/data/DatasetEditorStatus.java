package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetEditorStatus implements Property {
    CONNECTED,
    LOADING,
    LOADED;


    private final long index = Property.idx(this);

    @Override
    public long index() {
        return index;
    }
}
