package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.thread.ThreadPool;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public interface DBDebugOperation {

    static <T> void run(@NotNull Project project, String title, ThrowableRunnable<SQLException> runnable) {
        ExecutorService executorService = ThreadPool.debugExecutor();
        executorService.submit( () -> {
            Thread currentThread = Thread.currentThread();
            int initialPriority = currentThread.getPriority();
            currentThread.setPriority(Thread.MIN_PRIORITY);
            try {
                runnable.run();
            } catch (Exception e) {
                NotificationSupport.sendErrorNotification(
                        project,
                        NotificationGroup.DEBUGGER,
                        "Error performing debug operation ({0}): {1}", title, e);
            } finally {
                currentThread.setPriority(initialPriority);
            }
        });
    }


}
