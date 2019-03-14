package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

public class OpenViewDataAction extends DumbAwareAction {
    private DBView view;

    public OpenViewDataAction(DBView view) {
        super("View Data", null, Icons.OBEJCT_VIEW_DATA);
        this.view = view;
        setDefaultIcon(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
        databaseFileSystem.connectAndOpenEditor(view, EditorProviderId.DATA, false, true);
    }
}