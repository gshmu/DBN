package com.dci.intellij.dbn.debugger.execution.statement;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.execution.DBProgramRunner;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;

public class DBStatementRunner extends DBProgramRunner<StatementExecutionInput> {
    public static final String RUNNER_ID = "DBNStatementRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return false;
    }

    @Override
    protected void performExecution(
            final StatementExecutionInput executionInput,
            final Executor executor,
            final ExecutionEnvironment environment,
            final Callback callback) {
/*        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                final Project project = connectionHandler.getProject();

                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                boolean continueExecution = executionManager.promptExecutionDialog(executionInput, true);

                if (continueExecution) {
                    RunContentDescriptor reuseContent = environment.getContentToReuse();
                    DBProgramDebugProcessStarter debugProcessStarter = new DBProgramDebugProcessStarter(connectionHandler);
                    XDebugSession session = null;
                    try {
                        session = XDebuggerManager.getInstance(project).startSession(
                                DBStatementRunner.this,
                                environment,
                                reuseContent,
                                debugProcessStarter);

                        RunContentDescriptor descriptor = session.getRunContentDescriptor();

                        if (callback != null) callback.processStarted(descriptor);

                        if (true *//*LocalHistoryConfiguration.getInstance().ADD_LABEL_ON_RUNNING*//*) {
                            RunProfile runProfile = environment.getRunProfile();
                            LocalHistory.getInstance().putSystemLabel(project, executor.getId() + " " + runProfile.getName());
                        }

                        ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, descriptor);
                        ProcessHandler processHandler = descriptor.getProcessHandler();
                        if (processHandler != null) processHandler.startNotify();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();*/
    }

}

