package com.dci.intellij.dbn.editor.code.diff;

import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface SourceCodeDifManagerListener extends EventListener {
    Topic<SourceCodeDifManagerListener> TOPIC = Topic.create("Script execution event", SourceCodeDifManagerListener.class);
    void contentMerged(DBSourceCodeVirtualFile sourceCodeFile, MergeAction mergeAction);
}
