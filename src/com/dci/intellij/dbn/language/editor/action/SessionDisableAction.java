package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.routine.ParametricCallback.conditional;

public class SessionDisableAction extends DumbAwareAction {
    private ConnectionHandlerRef connectionHandlerRef;

    SessionDisableAction(ConnectionHandler connectionHandler) {
        super("Disable session support...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        Editor editor = ActionUtil.getEditor(e);
        if (editor != null) {
            ConnectionHandler connectionHandler = connectionHandlerRef.getnn();
            MessageUtil.showQuestionDialog(
                    project,
                    "Disable session support",
                    "Are you sure you want to disable the session support for connection \"" + connectionHandler.getName() + "\"\n(you can re-enable at any time in connection details settings)",
                    MessageUtil.OPTIONS_YES_NO,
                    0,
                    (option) -> conditional(option == 0,
                            () -> {
                                ConnectionDetailSettings detailSettings = connectionHandler.getSettings().getDetailSettings();
                                detailSettings.setEnableSessionManagement(false);
                            }));
        }
    }
}
