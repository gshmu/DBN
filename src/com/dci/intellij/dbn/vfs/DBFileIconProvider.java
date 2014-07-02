package com.dci.intellij.dbn.vfs;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class DBFileIconProvider implements FileIconProvider{
    @Override
    public Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        if (file instanceof DBVirtualFile) {
            DBVirtualFile virtualFile = (DBVirtualFile) file;
            return virtualFile.getIcon();
        }
        return null;
    }
}
