package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetEditorStatus implements Property.IntBase {
    CONNECTED,
    LOADING,
    LOADED;

    private final Computed computed = new Computed(this);

    @Override
    public Computed computed() {
        return computed;
    }
}
