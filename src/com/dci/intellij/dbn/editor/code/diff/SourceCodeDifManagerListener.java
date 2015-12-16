package com.dci.intellij.dbn.editor.code.diff;

import java.util.EventListener;

import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.util.messages.Topic;

public interface SourceCodeDifManagerListener extends EventListener {
    Topic<SourceCodeDifManagerListener> TOPIC = Topic.create("Script execution event", SourceCodeDifManagerListener.class);
    void contentMerged(DBSourceCodeVirtualFile sourceCodeFile, MergeAction mergeAction);
}
