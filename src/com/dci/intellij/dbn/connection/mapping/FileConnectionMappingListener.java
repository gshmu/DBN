package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface FileConnectionMappingListener extends EventListener {
    Topic<FileConnectionMappingListener> TOPIC = Topic.create("Connection mapping changed", FileConnectionMappingListener.class);
    default void connectionChanged(VirtualFile virtualFile, ConnectionHandler connectionHandler){}
    default void schemaChanged(VirtualFile virtualFile, SchemaId schema){}
    default void sessionChanged(VirtualFile virtualFile, DatabaseSession session){}
}
