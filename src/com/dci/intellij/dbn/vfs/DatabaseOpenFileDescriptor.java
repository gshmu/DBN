package com.dci.intellij.dbn.vfs;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class DatabaseOpenFileDescriptor extends OpenFileDescriptor {
    public DatabaseOpenFileDescriptor(Project project, @NotNull VirtualFile file, int offset) {
        super(project, file, offset);
    }

    public DatabaseOpenFileDescriptor(Project project, @NotNull VirtualFile file, int line, int col) {
        super(project, file, line, col);
    }

    public DatabaseOpenFileDescriptor(Project project, @NotNull VirtualFile file) {
        super(project, file);
    }
}
