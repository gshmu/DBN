package com.dci.intellij.dbn.editor.code.ui;

import javax.swing.JLabel;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;

public class SourceCodeLoadErrorNotificationPanel extends EditorNotificationPanel{
    protected final JLabel label = new JLabel();

    public SourceCodeLoadErrorNotificationPanel() {
        super(MessageType.ERROR);
    }
}
