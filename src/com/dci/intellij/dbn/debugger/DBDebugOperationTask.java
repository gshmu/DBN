package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.routine.BasicRunnable;
import com.dci.intellij.dbn.common.thread.AbstractTask;
import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public abstract class DBDebugOperationTask<T> extends AbstractTask<T> implements NotificationSupport {
    private ProjectRef projectRef;
    private String operationDescription;


    private DBDebugOperationTask(Project project, String operationDescription) {
        this.projectRef = ProjectRef.from(project);
        this.operationDescription = operationDescription;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        int initialPriority = currentThread.getPriority();
        currentThread.setPriority(Thread.MIN_PRIORITY);
        try {
            execute();
        } catch (Exception e) {
            handleException(e);
        } finally {
            currentThread.setPriority(initialPriority);
        }

    }

    public abstract void execute() throws Exception;

    @Override
    public final void start() {
        try {
            ExecutorService executorService = ThreadFactory.debugExecutor();
            executorService.submit(this);
        } catch (Exception e) {
            handleException(e);
        }
    }

    @Override
    public Project getProject() {
        return projectRef.getnn();
    }

    private void handleException(Exception e) {
        sendErrorNotification("Debugger", "Error performing debug operation (" + operationDescription + ").", e.getMessage());
    }

    public static <T> void invoke(@NotNull Project project, String title, BasicRunnable<SQLException> runnable) {
        new DBDebugOperationTask<T>(project, title) {
            @Override
            public void execute() throws SQLException {
                runnable.run();
            }
        }.start();
    }


}
