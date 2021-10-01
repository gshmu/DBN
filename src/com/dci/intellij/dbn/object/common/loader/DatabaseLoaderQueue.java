package com.dci.intellij.dbn.object.common.loader;

import com.dci.intellij.dbn.common.Constants;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class DatabaseLoaderQueue extends Task.Modal implements Disposable {
    private boolean active = true;
    private final Queue<Runnable> queue = new LinkedBlockingQueue<>();

    public DatabaseLoaderQueue(@Nullable Project project) {
        super(project, Constants.DBN_TITLE_DIALOG_SUFFIX + "Loading data dictionary", false);
    }

    public void queue(Runnable task) {
        queue.offer(task);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        Runnable task = queue.poll();
        while (task != null) {
            task.run();
            task = queue.poll();
        }
        active = false;
    }

    @Override
    public void dispose() {
        queue.clear();
    }
}
