package com.dci.intellij.dbn.execution.statement;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public abstract class StatementExecutionQueue {

    private Project project;
    private final Queue<StatementExecutionProcessor> processors = new ConcurrentLinkedQueue<>();
    private boolean executing = false;

    StatementExecutionQueue(Project project) {
        this.project = project;
    }

    public void queue(StatementExecutionProcessor processor) {
        if (!this.processors.contains(processor)) {
            processor.getExecutionStatus().setQueued(true);
            this.processors.add(processor);
            execute();
        }
    }

    private void execute() {
        if (!executing) {
            executing = true;
            new BackgroundTask(project, "Executing statements", false, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        StatementExecutionProcessor processor = processors.poll();
                        while (processor != null) {
                            StatementExecutionQueue.this.execute(processor);

                            if (progressIndicator.isCanceled()) {
                                cancel();
                            }
                            processor = processors.poll();
                        }
                    } finally {
                        executing = false;
                        if (progressIndicator.isCanceled()) {
                            cancel();
                        }
                    }
                }
            }.start();
        }
    }

    protected abstract void execute(StatementExecutionProcessor processor);

    public void cancel() {
        // cleanup queue for untouched processors
        StatementExecutionProcessor processor = processors.poll();
        while(processor != null) {
            processor.getExecutionStatus().reset();
            processor = processors.poll();
        }
    }

    public boolean contains(StatementExecutionProcessor processor) {
        return processors.contains(processor);
    }

    public void cancel(StatementExecutionProcessor processor) {
        processor.getExecutionStatus().setQueued(false);
        processors.remove(processor);
        if (processors.size() == 0) {
            executing = false;
        }
    }
}
