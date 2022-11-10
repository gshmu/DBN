package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;

public abstract class SourceCodeEditorNotificationPanel extends EditorNotificationPanel {
    public SourceCodeEditorNotificationPanel(MessageType messageType) {
        super(messageType);
    }
}
