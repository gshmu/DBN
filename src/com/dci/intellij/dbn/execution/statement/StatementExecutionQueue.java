package com.dci.intellij.dbn.execution.statement;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import static com.dci.intellij.dbn.execution.ExecutionStatus.QUEUED;

public abstract class StatementExecutionQueue extends DisposableBase{

    private ProjectRef projectRef;
    private final Queue<StatementExecutionProcessor> processors = new ConcurrentLinkedQueue<StatementExecutionProcessor>();
    private boolean executing = false;

    StatementExecutionQueue(StatementExecutionManager executionManager) {
        super(executionManager);
        projectRef = ProjectRef.from(executionManager.getProject());
    }

    public void queue(StatementExecutionProcessor processor) {
        if (!this.processors.contains(processor)) {
            processor.getExecutionContext().set(QUEUED, true);
            this.processors.add(processor);
            execute();
        }
    }

    @NotNull
    public Project getProject() {
        return projectRef.getnn();
    }


    private void execute() {
        if (!executing) {
            synchronized (this) {
                if (!executing) {
                    executing = true;
                    new BackgroundTask(getProject(), "Executing statements", true, true) {
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
        }
    }

    protected abstract void execute(StatementExecutionProcessor processor);

    public void cancelExecution() {
        // cleanup queue for untouched processors
        StatementExecutionProcessor processor = processors.poll();
        while(processor != null) {
            processor.getExecutionContext().reset();
            processor = processors.poll();
        }
    }

    public boolean contains(StatementExecutionProcessor processor) {
        return processors.contains(processor);
    }

    public void cancelExecution(StatementExecutionProcessor processor) {
        processor.getExecutionContext().set(QUEUED, false);
        processors.remove(processor);
        if (processors.size() == 0) {
            executing = false;
        }
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            processors.clear();
        }
    }
}
