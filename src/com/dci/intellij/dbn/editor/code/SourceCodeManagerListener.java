package com.dci.intellij.dbn.editor.code;

import java.util.EventListener;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.util.messages.Topic;

public interface SourceCodeManagerListener extends EventListener {
    Topic<SourceCodeManagerListener> TOPIC = Topic.create("Source Code Manager Event", SourceCodeManagerListener.class);

    void sourceCodeLoading(DBSourceCodeVirtualFile sourceCodeFile);

    void sourceCodeLoaded(DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad);

    void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor);
}
