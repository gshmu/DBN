package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import lombok.experimental.UtilityClass;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

@UtilityClass
public final class Write {

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
