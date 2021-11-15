package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public final class Write {
    private Write() {}

    public static void run(Runnable runnable) {
        Dispatch.run(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(
                    () -> {
                        try {
                            runnable.run();
                        } catch (ProcessCanceledException ignore) {}
                    });
        });
    }

    public static void run(Project project, Runnable runnable) {
        Dispatch.run(() -> {
            WriteCommandAction.writeCommandAction(nd(project)).run(() -> {
                try {
                    nd(project);
                    runnable.run();
                } catch (ProcessCanceledException  ignore) {}
            });
        });
    }
}
