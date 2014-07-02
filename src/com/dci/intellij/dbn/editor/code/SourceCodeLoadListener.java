package com.dci.intellij.dbn.editor.code;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface SourceCodeLoadListener extends EventListener {
    Topic<SourceCodeLoadListener> TOPIC = Topic.create("Source Code loaded", SourceCodeLoadListener.class);

    void sourceCodeLoaded(VirtualFile virtualFile);
}
