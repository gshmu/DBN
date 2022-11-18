package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.progress.PerformInBackgroundOption.ALWAYS_BACKGROUND;

public class InterfaceQueueConsumer implements Consumer<InterfaceTask<?>>{
    private final WeakRef<InterfaceQueue> queue;

    public InterfaceQueueConsumer(InterfaceQueue queue) {
        this.queue = WeakRef.of(queue);
    }

    @Override
    public void accept(InterfaceTask<?> task) {
        InterfaceQueue queue = this.queue.ensure();
        Project project = queue.getConnection().getProject();
        ProgressManager progressManager = ProgressManager.getInstance();
        Task.Backgroundable backgroundable = new Task.Backgroundable(project, task.getTitle(), true, ALWAYS_BACKGROUND) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(task.getDescription());
                queue.executeTask(task);
            }
        };
        progressManager.run(backgroundable);

    }
}
