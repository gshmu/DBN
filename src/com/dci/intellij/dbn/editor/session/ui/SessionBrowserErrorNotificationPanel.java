package com.dci.intellij.dbn.editor.session.ui;

import javax.swing.JLabel;

import com.dci.intellij.dbn.common.editor.EditorNotificationPanel;
import com.dci.intellij.dbn.common.message.MessageType;

public class SessionBrowserErrorNotificationPanel extends EditorNotificationPanel{
    protected final JLabel label = new JLabel();

    public SessionBrowserErrorNotificationPanel() {
        super(MessageType.ERROR);
    }
}
