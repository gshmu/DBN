package com.dci.intellij.dbn.object.common.editor;


import com.dci.intellij.dbn.object.type.DBObjectType;
import lombok.Getter;

@Getter
public class DefaultEditorOption {
    private final DBObjectType objectType;
    private final DefaultEditorType editorType;

    public DefaultEditorOption(DBObjectType objectType, DefaultEditorType editorType) {
        this.objectType = objectType;
        this.editorType = editorType;
    }
}
