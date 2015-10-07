package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.message.MessageType;

public class SourceCodeLoadErrorNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeLoadErrorNotificationPanel() {
        super(MessageType.ERROR);
    }
}
