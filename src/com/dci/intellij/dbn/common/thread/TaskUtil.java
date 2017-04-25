package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;

class TaskUtil {
    static void startTask(final Task task, final Project project) {
        Application application = ApplicationManager.getApplication();

        if (application.isDispatchThread()) {
            executeTask(task, project);
        } else {
            Runnable runnable = new Runnable() {
                public void run() {
                    executeTask(task, project);
                }
            };
            application.invokeLater(runnable, ModalityState.NON_MODAL);
        }
    }
    private static void executeTask(Task task, Project project) {
        if (project == null || !project.isDisposed()) {
            ProgressManager progressManager = ProgressManager.getInstance();
            progressManager.run(task);
        }
    }
}
