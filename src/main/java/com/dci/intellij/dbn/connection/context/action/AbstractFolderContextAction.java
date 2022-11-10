package com.dci.intellij.dbn.connection.context.action;

import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContext;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFolderContextAction extends DumbAwareProjectAction {

    protected static FileConnectionContext getFileContext(@Nullable VirtualFile file, @NotNull Project project) {
        if (file != null && file.isDirectory()) {
            FileConnectionContextManager contextManager = getContextManager(project);
            FileConnectionContext mapping = contextManager.getMapping(file);
            if (mapping != null && mapping.isForFile(file)) {
                return mapping;
            }
        }
        return null;
    }

    protected static FileConnectionContextManager getContextManager(@NotNull Project project) {
        return FileConnectionContextManager.getInstance(project);
    }
}
