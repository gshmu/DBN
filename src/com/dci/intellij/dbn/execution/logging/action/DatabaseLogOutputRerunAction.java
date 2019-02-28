package com.dci.intellij.dbn.execution.logging.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;

public class DatabaseLogOutputRerunAction extends AbstractDatabaseLogOutputAction {
    public DatabaseLogOutputRerunAction() {
        super("Rerun Script", Icons.STMT_EXECUTION_RERUN);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        if (Failsafe.check(loggingResult)) {
            LogOutputContext context = loggingResult.getContext();
            VirtualFile sourceFile = context.getSourceFile();
            if (sourceFile != null) {
                ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
                scriptExecutionManager.executeScript(sourceFile);
            }
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("Rerun Script");

        DatabaseLoggingResult loggingResult = getDatabaseLogOutput(e);
        LogOutputContext context = loggingResult == null ? null : loggingResult.getContext();
        boolean enabled = context != null && context.getSourceFile() != null && !context.isActive();
        presentation.setEnabled(enabled);

    }
}
