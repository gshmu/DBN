package com.dci.intellij.dbn.editor.data;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DatasetLoadListener extends EventListener {
    Topic<DatasetLoadListener> TOPIC = Topic.create("Dataset loaded", DatasetLoadListener.class);

    void datasetLoaded(VirtualFile virtualFile);
}
