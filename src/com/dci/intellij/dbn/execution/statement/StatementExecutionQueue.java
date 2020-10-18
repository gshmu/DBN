package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dci.intellij.dbn.execution.ExecutionStatus.CANCELLED;
import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;
import static com.dci.intellij.dbn.execution.ExecutionStatus.QUEUED;

public final class StatementExecutionQueue extends StatefulDisposable.Base {

    private final ProjectRef project;
    private final Queue<StatementExecutionProcessor> processors = new ConcurrentLinkedQueue<>();
    private boolean executing = false;

    public StatementExecutionQueue(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        project = ProjectRef.of(connectionHandler.getProject());
    }

    void queue(StatementExecutionProcessor processor) {
        ExecutionContext executionContext = processor.getExecutionContext();
        executionContext.set(CANCELLED, false);
        if (!this.processors.contains(processor)) {
            executionContext.set(QUEUED, true);
            this.processors.add(processor);
            execute();
        }
    }

    @NotNull
    public Project getProject() {
        return project.ensure();
    }


    private void execute() {
        if (!executing) {
            synchronized (this) {
                if (!executing) {
                    executing = true;
                    Project project = getProject();
                    Progress.background(project, "Executing statements", true, (progress) -> {
                        try {
                            StatementExecutionProcessor processor = processors.poll();
                            while (processor != null) {
                                execute(processor);

                                if (progress.isCanceled()) {
                                    cancelExecution();
                                }
                                processor = processors.poll();
                            }
                        } finally {
                            executing = false;
                            if (progress.isCanceled()) {
                                cancelExecution();
                            }
                        }
                    });
                }
            }
        }
    }

    private void execute(StatementExecutionProcessor processor) {
        try {
            Project project = getProject();
            ExecutionContext context = processor.getExecutionContext();
            context.set(QUEUED, false);
            context.set(EXECUTING, true);
            StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
            statementExecutionManager.process(processor);
        } catch (ProcessCanceledException ignore) {}
    }

    private void cancelExecution() {
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
    protected void disposeInner() {
        SafeDisposer.nullify(this);
    }
}
