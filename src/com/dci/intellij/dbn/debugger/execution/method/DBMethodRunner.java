package com.dci.intellij.dbn.debugger.execution.method;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DBProgramDebugProcessStarter;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunner;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.history.LocalHistory;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

public class DBMethodRunner extends DBProgramRunner<MethodExecutionInput> {
    public static final String RUNNER_ID = "DBNMethodRunner";

    @NotNull
    public String getRunnerId() {
        return RUNNER_ID;
    }

    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (profile instanceof DBMethodRunConfiguration) {
            DBMethodRunConfiguration runConfiguration = (DBMethodRunConfiguration) profile;
            return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && runConfiguration.getMethod() != null;
        }
        return false;
    }

    @Override
    protected void performExecution(
            final MethodExecutionInput executionInput,
            final Executor executor,
            final ExecutionEnvironment environment,
            final Callback callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                final Project project = environment.getProject();

                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                boolean continueExecution = executionManager.promptExecutionDialog(executionInput, true);

                if (continueExecution) {
                    RunContentDescriptor reuseContent = environment.getContentToReuse();
                    DBProgramDebugProcessStarter debugProcessStarter = new DBProgramDebugProcessStarter(connectionHandler);
                    XDebugSession session = null;
                    try {
                        session = XDebuggerManager.getInstance(project).startSession(
                                DBMethodRunner.this,
                                environment,
                                reuseContent,
                                debugProcessStarter);

                        RunContentDescriptor descriptor = session.getRunContentDescriptor();

                        if (callback != null) callback.processStarted(descriptor);

                        if (true /*LocalHistoryConfiguration.getInstance().ADD_LABEL_ON_RUNNING*/) {
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
        }.start();
    }

}

