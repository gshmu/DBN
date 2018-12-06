package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetEditorStatus implements Property {
    CONNECTED,
    LOADING,
    LOADED;


    private final int index = Property.idx(this);

    @Override
    public int index() {
        return index;
    }
}
