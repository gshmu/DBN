package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SourceCodeManagerAdapter implements SourceCodeManagerListener {
    @Override
    public void sourceCodeLoading(@NotNull DBSourceCodeVirtualFile sourceCodeFile) {}

    @Override
    public void sourceCodeLoaded(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {}

    @Override
    public void sourceCodeSaved(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {}
}
