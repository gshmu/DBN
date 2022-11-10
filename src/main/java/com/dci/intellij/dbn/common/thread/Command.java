package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;

public final class Command {
    private Command() {}

    public static void run(Project project, String commandName, Runnable command) {
        Write.run(() -> {
            CommandProcessor commandProcessor = CommandProcessor.getInstance();
            commandProcessor.executeCommand(project, command, commandName, null);
        });

    }
}
