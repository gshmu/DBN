package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.connection.console.DatabaseConsoleManager;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.util.ActionUtil.ensureProject;
import static com.dci.intellij.dbn.common.util.ActionUtil.getVirtualFile;

public class ConsoleCreateAction extends DumbAwareAction {
    private DBConsoleType consoleType;
    ConsoleCreateAction(DBConsoleType consoleType) {
        super("New " + consoleType.getName() + "...");
        this.consoleType = consoleType;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ensureProject(e);
        VirtualFile virtualFile = getVirtualFile(e);
        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            DatabaseConsoleManager consoleManager = DatabaseConsoleManager.getInstance(project);
            consoleManager.showCreateConsoleDialog(consoleVirtualFile.getConnectionHandler(), consoleType);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("New " + consoleType.getName() + "...");
    }


}