package com.dci.intellij.dbn.execution.script.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class ExecuteScriptFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (isAvailableFor(virtualFile)) {
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(virtualFile);
        }
    }

    private boolean isAvailableFor(VirtualFile virtualFile) {
        return virtualFile != null && (virtualFile.getFileType() == SQLFileType.INSTANCE || virtualFile.getFileType() == PSQLFileType.INSTANCE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        VirtualFile virtualFile = getVirtualFile(e);
        presentation.setVisible(isAvailableFor(virtualFile));
        presentation.setIcon(Icons.EXECUTE_SQL_SCRIPT);
        presentation.setText("Execute SQL Script");
    }
}
