package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public class SourceCodeLoadErrorNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeLoadErrorNotificationPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        super(MessageType.ERROR);
        setText("Could not load source for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
    }
}
