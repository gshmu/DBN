package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSessionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class CreateDatabaseSessionAction extends DumbAwareAction {
    private ConnectionHandlerRef connectionHandlerRef;

    public CreateDatabaseSessionAction(ConnectionHandler connectionHandler) {
        super("Create session...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            DatabaseSessionManager sessionManager = DatabaseSessionManager.getInstance(project);
            ConnectionHandler connectionHandler = connectionHandlerRef.get();
            sessionManager.showCreateSessionDialog(connectionHandler);
        }
    }
}
