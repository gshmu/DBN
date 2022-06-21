package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookups;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.message.MessageCallback.when;

public class DatabaseSessionDisableAction extends DumbAwareProjectAction {
    private final ConnectionRef connection;

    DatabaseSessionDisableAction(ConnectionHandler connection) {
        super("Disable Session Support...");
        this.connection = connection.ref();
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        Editor editor = Lookups.getEditor(e);
        if (editor != null) {
            ConnectionHandler connection = this.connection.ensure();
            Messages.showQuestionDialog(
                    project,
                    "Disable session support",
                    "Are you sure you want to disable the session support for connection \"" + connection.getName() + "\"\n(you can re-enable at any time in connection details settings)",
                    Messages.OPTIONS_YES_NO,
                    0,
                    option -> when(option == 0, () -> {
                        ConnectionDetailSettings detailSettings = connection.getSettings().getDetailSettings();
                        detailSettings.setEnableSessionManagement(false);
                    }));
        }
    }
}
