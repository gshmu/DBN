package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.Property;

public enum DatasetEditorStatus implements Property.IntBase {
    CONNECTED,
    LOADING,
    LOADED;

    public static final DatasetEditorStatus[] VALUES = values();

    private final IntMasks masks = new IntMasks(this);

    @Override
    public IntMasks masks() {
        return masks;
    }
}
