package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.message.MessageType;

public class SourceCodeOutdatedNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeOutdatedNotificationPanel() {
        super(MessageType.WARNING);
    }
}
