package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventListener;

public interface SourceCodeManagerListener extends EventListener {
    Topic<SourceCodeManagerListener> TOPIC = Topic.create("Source Code Manager Event", SourceCodeManagerListener.class);

    void sourceCodeLoading(@NotNull DBSourceCodeVirtualFile sourceCodeFile);

    void sourceCodeLoaded(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad);

    void sourceCodeSaved(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor);
}
