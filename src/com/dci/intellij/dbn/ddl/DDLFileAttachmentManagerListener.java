package com.dci.intellij.dbn.ddl;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface DDLFileAttachmentManagerListener extends EventListener {
    Topic<DDLFileAttachmentManagerListener> TOPIC = Topic.create("DDL File Attachment Event", DDLFileAttachmentManagerListener.class);

    void ddlFileDetached(VirtualFile virtualFile);

    void ddlFileAttached(VirtualFile virtualFile);
}
