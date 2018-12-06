package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class DatasetEditorStatusHolder extends PropertyHolderImpl<DatasetEditorStatus> {
    @Override
    protected DatasetEditorStatus[] properties() {
        return DatasetEditorStatus.values();
    }
}
