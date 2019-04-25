package com.dci.intellij.dbn.execution.common.message.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.common.message.ui.tree.MessagesTree;
import com.dci.intellij.dbn.options.ConfigId;
import com.dci.intellij.dbn.options.ProjectSettingsManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecutionEngineSettingsAction extends ExecutionMessagesAction {
    public ExecutionEngineSettingsAction(MessagesTree messagesTree) {
        super(messagesTree, "Settings", Icons.EXEC_RESULT_OPTIONS);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull MessagesTree messagesTree) {
        ProjectSettingsManager settingsManager = ProjectSettingsManager.getInstance(project);
        settingsManager.openProjectSettings(ConfigId.EXECUTION_ENGINE);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable MessagesTree messagesTree) {

    }
}