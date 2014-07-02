package com.dci.intellij.dbn.ddl;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface DDLMappingListener extends EventListener {
    Topic<DDLMappingListener> TOPIC = Topic.create("DDL Mappings changed", DDLMappingListener.class);

    void ddlFileDetached(VirtualFile virtualFile);

    void ddlFileAttached(VirtualFile virtualFile);
}
