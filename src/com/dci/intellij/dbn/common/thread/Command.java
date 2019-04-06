package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;

public interface Command {
    static void run(Project project, String commandName, Runnable command) {
        Write.run(() -> {
            CommandProcessor commandProcessor = CommandProcessor.getInstance();
            commandProcessor.executeCommand(project, command, commandName, null);
        });

    }
}
