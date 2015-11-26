package com.dci.intellij.dbn.editor.code;

import java.util.EventListener;

import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.util.messages.Topic;

public interface SourceCodeManagerListener extends EventListener {
    Topic<SourceCodeManagerListener> TOPIC = Topic.create("Source Code Manager Event", SourceCodeManagerListener.class);

    void sourceCodeLoadStarted(DBSourceCodeVirtualFile sourceCodeFile);
    void sourceCodeLoadFinished(DBSourceCodeVirtualFile sourceCodeFile);

    void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean isInitialLoad);

    void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, SourceCodeEditor fileEditor);
}
