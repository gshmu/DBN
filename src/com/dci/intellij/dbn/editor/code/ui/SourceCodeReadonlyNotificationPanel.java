package com.dci.intellij.dbn.editor.code.ui;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;

public class SourceCodeReadonlyNotificationPanel extends SourceCodeEditorNotificationPanel{
    public SourceCodeReadonlyNotificationPanel(SourceCodeEditor sourceCodeEditor) {
        super(MessageType.WARNING);
    }
}
