package com.dci.intellij.dbn.execution.statement.result.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class ExecutionResultSettingsAction extends AbstractExecutionResultAction {
    public ExecutionResultSettingsAction() {
        super("Settings", Icons.EXEC_RESULT_OPTIONS);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        if (project != null) {
            GlobalProjectSettingsDialog globalSettingsDialog = new GlobalProjectSettingsDialog(project);
            ExecutionEngineSettings settings = ExecutionEngineSettings.getInstance(project);
            globalSettingsDialog.focusSettings(settings);
            globalSettingsDialog.show();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Settings");
    }
}
