package com.dci.intellij.dbn.execution.compiler;

import java.util.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.util.messages.Topic;

public interface CompileManagerListener extends EventListener {
    Topic<CompileManagerListener> TOPIC = Topic.create("Compile Manager Event", CompileManagerListener.class);

    void compileFinished(@NotNull ConnectionHandler connectionHandler, @Nullable DBSchemaObject object);
}
