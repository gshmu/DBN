package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.thread.AbstractTask;
import com.dci.intellij.dbn.common.thread.ThreadFactory;
import com.intellij.openapi.project.Project;

import java.util.concurrent.ExecutorService;

public abstract class DBDebugOperationTask<T> extends AbstractTask<T> implements NotificationSupport {
    private ProjectRef projectRef;
    private String operationDescription;


    public DBDebugOperationTask(Project project, String operationDescription) {
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
}
