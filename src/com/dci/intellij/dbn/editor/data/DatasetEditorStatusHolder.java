package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.property.PropertyHolderBase;

public class DatasetEditorStatusHolder extends PropertyHolderBase.IntStore<DatasetEditorStatus> {
    @Override
    protected DatasetEditorStatus[] properties() {
        return DatasetEditorStatus.values();
    }
}
