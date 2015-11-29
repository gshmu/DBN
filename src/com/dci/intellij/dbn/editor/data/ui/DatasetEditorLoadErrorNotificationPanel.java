package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;

public class DatasetEditorLoadErrorNotificationPanel extends DatasetEditorNotificationPanel {
    public DatasetEditorLoadErrorNotificationPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        super(MessageType.ERROR);
        setText("Could not load data for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
    }
}
