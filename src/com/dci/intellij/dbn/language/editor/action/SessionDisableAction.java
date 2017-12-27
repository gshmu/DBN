package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class SessionDisableAction extends DumbAwareAction {
    private ConnectionHandlerRef connectionHandlerRef;

    SessionDisableAction(ConnectionHandler connectionHandler) {
        super("Disable session support...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = ActionUtil.getEditor(e);
        if (project != null && editor != null) {
            ConnectionHandler connectionHandler = connectionHandlerRef.get();
            MessageUtil.showQuestionDialog(
                    connectionHandler.getProject(),
                    "Disable session support",
                    "Are you sure you want to disable the session support for connection \"" + connectionHandler.getName() + "\"\n(you can re-enable at any time in connection details settings)",
                    MessageUtil.OPTIONS_YES_NO,
                    0, new MessageCallback() {
                        @Override
                        protected void execute() {
                            if (getData() == 0) {
                                ConnectionHandler connectionHandler = connectionHandlerRef.get();
                                connectionHandler.getSettings().getDetailSettings().setEnableSessionManagement(false);
                            }
                        }
                    });
        }
    }
}
