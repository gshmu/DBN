package com.dci.intellij.dbn.execution.script;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface ScriptExecutionManagerListener extends EventListener {
    Topic<ScriptExecutionManagerListener> TOPIC = Topic.create("Script execution event", ScriptExecutionManagerListener.class);
    void scriptExecuted(VirtualFile virtualFile);
}
