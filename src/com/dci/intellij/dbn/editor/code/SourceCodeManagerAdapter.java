package com.dci.intellij.dbn.editor.code;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;

public abstract class SourceCodeManagerAdapter implements SourceCodeManagerListener {
    @Override
    public void sourceCodeLoading(DBSourceCodeVirtualFile sourceCodeFile) {}

    @Override
    public void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {}

    @Override
    public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {}
}
