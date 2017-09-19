package com.dci.intellij.dbn.execution.script;

import java.util.EventListener;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;

public interface ScriptExecutionListener extends EventListener {
    Topic<ScriptExecutionListener> TOPIC = Topic.create("Script execution event", ScriptExecutionListener.class);
    void scriptExecuted(VirtualFile virtualFile);
}
