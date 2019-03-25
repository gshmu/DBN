package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

@Nullifiable
public final class StatementExecutionQueue extends DisposableBase{

    private ProjectRef projectRef;
    private final Queue<StatementExecutionProcessor> processors = new ConcurrentLinkedQueue<StatementExecutionProcessor>();
    private boolean executing = false;

    public StatementExecutionQueue(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        projectRef = ProjectRef.from(connectionHandler.getProject());
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
        return projectRef.ensure();
    }


    private void execute() {
        Synchronized.run(this,
                () -> !executing,
                () -> {
                    executing = true;
                    Project project = getProject();
                    Progress.background(project, "Executing statements", true, (progress) -> {
                        try {
                            StatementExecutionProcessor processor = processors.poll();
                            while (processor != null) {
                                ExecutionContext context = processor.getExecutionContext();
                                try {
                                    context.set(QUEUED, false);
                                    context.set(EXECUTING, true);
                                    StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
                                    statementExecutionManager.process(processor);
                                } catch (ProcessCanceledException ignore) {}

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
                });
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
}
