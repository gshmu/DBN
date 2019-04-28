package com.dci.intellij.dbn.object.common.editor;


import com.dci.intellij.dbn.object.type.DBObjectType;

public class DefaultEditorOption {
    private DBObjectType objectType;
    private DefaultEditorType editorType;

    public DefaultEditorOption(DBObjectType objectType, DefaultEditorType editorType) {
        this.objectType = objectType;
        this.editorType = editorType;
    }

    public DBObjectType getObjectType() {
        return objectType;
    }

    public DefaultEditorType getEditorType() {
        return editorType;
    }
}
