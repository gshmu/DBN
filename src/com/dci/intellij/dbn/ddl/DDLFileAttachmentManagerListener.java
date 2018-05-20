package com.dci.intellij.dbn.ddl;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DDLFileAttachmentManagerListener extends EventListener {
    Topic<DDLFileAttachmentManagerListener> TOPIC = Topic.create("DDL File Attachment Event", DDLFileAttachmentManagerListener.class);

    void ddlFileDetached(VirtualFile virtualFile);

    void ddlFileAttached(VirtualFile virtualFile);
}
