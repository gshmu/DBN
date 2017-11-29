package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class StatementExecutionQueue {

    private Project project;
    private final Queue<StatementExecutionProcessor> processors = new ConcurrentLinkedQueue<StatementExecutionProcessor>();
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
            new BackgroundTask(project, "Executing statements", true, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        StatementExecutionProcessor processor = processors.poll();
                        while (processor != null) {
                            try {
                                StatementExecutionQueue.this.execute(processor);
                            } catch (ProcessCanceledException ignore) {}

                            if (progressIndicator.isCanceled()) {
                                cancelExecution();
                            }
                            processor = processors.poll();
                        }
                    } finally {
                        executing = false;
                        if (progressIndicator.isCanceled()) {
                            cancelExecution();
                        }
                    }
                }
            }.start();
        }
    }

    protected abstract void execute(StatementExecutionProcessor processor);

    public void cancelExecution() {
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

    public void cancelExecution(StatementExecutionProcessor processor) {
        processor.getExecutionStatus().setQueued(false);
        processors.remove(processor);
        if (processors.size() == 0) {
            executing = false;
        }
    }
}
