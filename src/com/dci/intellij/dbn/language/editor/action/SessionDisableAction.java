package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.message.MessageCallback.conditional;

public class SessionDisableAction extends DumbAwareProjectAction {
    private ConnectionHandlerRef connectionHandlerRef;

    SessionDisableAction(ConnectionHandler connectionHandler) {
        super("Disable Session Support...");
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookup.getEditor(e);
        if (editor != null) {
            ConnectionHandler connectionHandler = connectionHandlerRef.ensure();
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
