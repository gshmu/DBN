package com.dci.intellij.dbn.vfs.file.status;

import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DBFileStatusListener extends EventListener {
    Topic<DBFileStatusListener> TOPIC = Topic.create("File status change event", DBFileStatusListener.class);

    void statusChanged(DBContentVirtualFile file, DBFileStatus status, boolean value);
}
