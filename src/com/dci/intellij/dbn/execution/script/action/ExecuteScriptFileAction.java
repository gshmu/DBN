package com.dci.intellij.dbn.execution.script.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ExecuteScriptFileAction extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (project != null && isAvailableFor(virtualFile)) {
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(project);
            scriptExecutionManager.executeScript(virtualFile);
        }
    }

    private boolean isAvailableFor(VirtualFile virtualFile) {
        return virtualFile != null && (virtualFile.getFileType() == SQLFileType.INSTANCE || virtualFile.getFileType() == PSQLFileType.INSTANCE);
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = ActionUtil.getProject(e);
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        presentation.setVisible(isAvailableFor(virtualFile));
        presentation.setIcon(Icons.EXECUTE_SQL_SCRIPT);
        presentation.setText("Execute SQL Script");
    }
}
