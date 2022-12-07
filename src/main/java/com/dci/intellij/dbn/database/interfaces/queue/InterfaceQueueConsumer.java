package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.ref.WeakRef;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.ThreadMonitor;
import com.dci.intellij.dbn.common.thread.ThreadProperty;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

public class InterfaceQueueConsumer implements Consumer<InterfaceTask<?>>{
    private final WeakRef<InterfaceQueue> queue;

    public InterfaceQueueConsumer(InterfaceQueue queue) {
        this.queue = WeakRef.of(queue);
    }

    @Override
    public void accept(InterfaceTask<?> task) {
        ThreadMonitor.surround(
                getProject(),
                ThreadProperty.DATABASE_INTERFACE,
                () -> schedule(task, getQueue()));
    }

    private static void schedule(InterfaceTask<?> task, InterfaceQueue queue) {
        Project project = queue.getProject();
        if (progressBackgroundSupported(task)) {
            Progress.background(project, queue.getConnection(), true,
                    task.getTitle(),
                    task.getText(),
                    indicator -> queue.executeTask(task));
        } else {
            Background.run(project, () -> queue.executeTask(task));
        }
    }

    private static boolean progressBackgroundSupported(InterfaceTask<?> task) {
        if (!task.isProgress()) return false;

        ProgressManager progressManager = ProgressManager.getInstance();
        return !progressManager.hasModalProgressIndicator() &&
                !progressManager.hasUnsafeProgressIndicator();
    }

    public InterfaceQueue getQueue() {
        return WeakRef.ensure(queue);
    }

    private Project getProject() {
        return getQueue().getProject();
    }
}
