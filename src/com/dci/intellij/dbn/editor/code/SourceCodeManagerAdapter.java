package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;

public abstract class SourceCodeManagerAdapter implements SourceCodeManagerListener {
    @Override
    public void sourceCodeLoadStarted(DBSourceCodeVirtualFile sourceCodeFile) {}

    @Override
    public void sourceCodeLoadFinished(DBSourceCodeVirtualFile sourceCodeFile) {}

    @Override
    public void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean isInitialLoad) {}

    @Override
    public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, SourceCodeEditor fileEditor) {}
}
