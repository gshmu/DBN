package com.dci.intellij.dbn.connection.context.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.common.action.Lookup;
import com.dci.intellij.dbn.connection.ConnectionSelectorOptions;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.connection.ConnectionSelectorOptions.Option.SHOW_VIRTUAL_CONNECTIONS;

public class FolderSchemaLinkAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        VirtualFile file = Lookup.getVirtualFile(e);
        if (isAvailableFor(file)) {
            DataContext dataContext = e.getDataContext();
            ConnectionSelectorOptions options = ConnectionSelectorOptions.options(SHOW_VIRTUAL_CONNECTIONS);

            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
            contextManager.promptSchemaSelector(file, dataContext, null);
        }
    }

    private boolean isAvailableFor(VirtualFile virtualFile) {
        return virtualFile != null && virtualFile.isDirectory();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project) {
        Presentation presentation = e.getPresentation();
        VirtualFile virtualFile = Lookup.getVirtualFile(e);
        presentation.setVisible(isAvailableFor(virtualFile));
        presentation.setText("Associate Schema...");
    }
}
