package com.dci.intellij.dbn.execution.script;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

import java.util.EventListener;

public interface ScriptExecutionListener extends EventListener {
    Topic<ScriptExecutionListener> TOPIC = Topic.create("Script execution event", ScriptExecutionListener.class);
    void scriptExecuted(VirtualFile virtualFile);
}
