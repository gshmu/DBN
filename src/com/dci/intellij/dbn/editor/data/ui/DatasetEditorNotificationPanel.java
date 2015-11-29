package com.dci.intellij.dbn.editor.data.ui;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;

public abstract class DatasetEditorNotificationPanel extends EditorNotificationPanel {
    public DatasetEditorNotificationPanel(MessageType messageType) {
        super(messageType);
    }
}
