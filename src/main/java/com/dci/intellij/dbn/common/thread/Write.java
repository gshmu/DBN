package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public final class Write {
    private Write() {}

    public static void run(Runnable runnable) {
        Dispatch.run(() -> {
            Application application = ApplicationManager.getApplication();
            application.runWriteAction(() -> Failsafe.guarded(runnable, r -> r.run()));
        });
    }

    public static void run(Project project, Runnable runnable) {
        Dispatch.run(() -> {
            WriteCommandAction.Builder builder = WriteCommandAction.writeCommandAction(nd(project));
            builder.run(() -> Failsafe.guarded(() -> {nd(project); runnable.run();}));
        });
    }
}
