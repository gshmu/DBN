package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public interface DBDebugOperation {

    static <T> void run(@NotNull Project project, String title, ThrowableRunnable<SQLException> runnable) {
        ExecutorService executorService = ThreadFactory.debugExecutor();
        executorService.submit( () -> {
            Thread currentThread = Thread.currentThread();
            int initialPriority = currentThread.getPriority();
            currentThread.setPriority(Thread.MIN_PRIORITY);
            try {
                runnable.run();
            } catch (Exception e) {
                NotificationUtil.sendErrorNotification(
                        project,
                        "Debugger",
                        "Error performing debug operation (" + title + ").",
                        e.getMessage());
            } finally {
                currentThread.setPriority(initialPriority);
            }
        });
    }


}
