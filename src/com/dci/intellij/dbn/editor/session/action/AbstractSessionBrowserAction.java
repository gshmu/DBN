package com.dci.intellij.dbn.editor.session.action;

import com.dci.intellij.dbn.common.action.DataKeys;
import com.dci.intellij.dbn.editor.session.SessionBrowser;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.util.ActionUtil.getFileEditor;

public abstract class AbstractSessionBrowserAction extends DumbAwareAction {
    public AbstractSessionBrowserAction(String text) {
        super(text);
    }

    public AbstractSessionBrowserAction(String text, Icon icon) {
        super(text, null, icon);
    }

    @Nullable
    public static SessionBrowser getSessionBrowser(AnActionEvent e) {
        SessionBrowser sessionBrowser = e.getData((DataKeys.SESSION_BROWSER));
        if (sessionBrowser == null) {
            FileEditor fileEditor = getFileEditor(e);
            if (fileEditor instanceof SessionBrowser) {
                return (SessionBrowser) fileEditor;
            }
        } else {
            return sessionBrowser;
        }
        return null;
    }
}
