package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface FileConnectionMappingListener extends EventListener {
    Topic<FileConnectionMappingListener> TOPIC = Topic.create("Connection mapping changed", FileConnectionMappingListener.class);

    default void connectionChanged(Project project, VirtualFile file, ConnectionHandler connection){
        mappingChanged(project, file);
    }

    default void schemaChanged(Project project, VirtualFile file, SchemaId schema){
        mappingChanged(project, file);
    }

    default void sessionChanged(Project project, VirtualFile file, DatabaseSession session){
        mappingChanged(project, file);
    }

    default void mappingChanged(Project project, VirtualFile file){}
}
