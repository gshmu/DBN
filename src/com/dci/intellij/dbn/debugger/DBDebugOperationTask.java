package com.dci.intellij.dbn.debugger;

import java.util.concurrent.ExecutorService;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.AbstractTask;
import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.intellij.openapi.project.Project;

public abstract class DBDebugOperationTask<T> extends AbstractTask<T> {
    private Project project;
    private String operationDescription;


    public DBDebugOperationTask(Project project, String operationDescription) {
        this.project = project;
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

    public final void start() {
        try {
            ExecutorService executorService = ThreadFactory.debugExecutor();
            executorService.submit(this);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        NotificationUtil.sendErrorNotification(project, "Debugger", "Error performing debug operation (" + operationDescription + ").", e.getMessage());
    }
}
