package com.dci.intellij.dbn.debugger.common.process;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBProgramRunConfig;
import com.dci.intellij.dbn.debugger.jdbc.config.ui.CompileDebugDependenciesDialog;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.history.LocalHistory;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;

public abstract class DBProgramRunner<T extends ExecutionInput> extends GenericProgramRunner {
    @Nullable
    protected RunContentDescriptor doExecute(@NotNull Project project, @NotNull RunProfileState state, RunContentDescriptor contentToReuse, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return doExecute(project, env.getExecutor(), state, contentToReuse, env);
    }

    protected RunContentDescriptor doExecute(
            final Project project,
            final Executor executor,
            RunProfileState state,
            RunContentDescriptor contentToReuse,
            final ExecutionEnvironment environment) throws ExecutionException {

        final DBProgramRunConfig runProfile = (DBProgramRunConfig) environment.getRunProfile();
        new ConnectionAction("the debug execution", runProfile.getConnectionHandler(), new TaskInstructions("Checking debug privileges", false, true)) {
            @Override
            protected boolean canExecute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseDebuggerManager databaseDebuggerManager = DatabaseDebuggerManager.getInstance(project);
                return databaseDebuggerManager.checkForbiddenOperation(connectionHandler,
                        "Another debug session is active on this connection. You can only run one debug session at the time.");
            }

            @Override
            protected void execute() {
                performPrivilegeCheck(
                        project,
                        (T) runProfile.getExecutionInput(),
                        environment,
                        null);
            }
        }.start();
        return null;
    }

    private void performPrivilegeCheck(
            final Project project,
            final T executionInput,
            final ExecutionEnvironment environment,
            final Callback callback) {
        final DBProgramRunConfig runProfile = (DBProgramRunConfig) environment.getRunProfile();
        final ConnectionHandler connectionHandler = runProfile.getConnectionHandler();
        if (connectionHandler == null) {

        } else {
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
            final List<String> missingPrivileges = debuggerManager.getMissingDebugPrivileges(connectionHandler);
            if (missingPrivileges.size() > 0) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("The current user (").append(connectionHandler.getUserName()).append(") does not have sufficient privileges to perform debug operations on this database.\n");
                buffer.append("Please contact your administrator to grant the required privileges. ");
                buffer.append("Missing privileges:\n");
                for (String missingPrivilege : missingPrivileges) {
                    buffer.append(" - ").append(missingPrivilege).append("\n");
                }

                MessageUtil.showWarningDialog(
                        project, "Insufficient privileges", buffer.toString(),
                        new String[]{"Continue anyway", "Cancel"}, 0,
                        new SimpleTask() {
                            @Override
                            protected boolean canExecute() {
                                return getHandle() == 0;
                            }

                            @Override
                            protected void execute() {
                                performInitialize(
                                        connectionHandler,
                                        executionInput,
                                        environment,
                                        callback);
                            }
                        });
            } else {
                performInitialize(
                        connectionHandler,
                        executionInput,
                        environment,
                        callback);
            }
        }
    }

    private void performInitialize(
            @NotNull final ConnectionHandler connectionHandler,
            @NotNull final T executionInput,
            @NotNull final ExecutionEnvironment environment,
            final Callback callback) {
        final DBProgramRunConfig runProfile = (DBProgramRunConfig) environment.getRunProfile();
        if (runProfile.isCompileDependencies()) {
            final Project project = connectionHandler.getProject();

            new BackgroundTask(project, "Initializing debug environment", false, true) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) {
                    DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                    initProgressIndicator(progressIndicator, true, "Loading method dependencies");
                    if (!project.isDisposed() && !progressIndicator.isCanceled()) {

                        List<DBSchemaObject> dependencies = debuggerManager.loadCompileDependencies(runProfile.getMethods(), progressIndicator);
                        if (!progressIndicator.isCanceled()) {
                            if (dependencies.size() > 0) {
                                performCompile(
                                        connectionHandler,
                                        executionInput,
                                        environment,
                                        callback,
                                        dependencies);
                            } else {
                                performExecution(
                                        executionInput,
                                        environment,
                                        callback);
                            }
                        }
                    }
                }
            }.start();
        }
    }

    private void performCompile(
            @NotNull final ConnectionHandler connectionHandler,
            @NotNull final T executionInput,
            @NotNull final ExecutionEnvironment environment,
            final Callback callback,
            final List<DBSchemaObject> dependencies) {

        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                final Project project = connectionHandler.getProject();
                DBProgramRunConfig runConfiguration = (DBProgramRunConfig) environment.getRunProfile();
                CompileDebugDependenciesDialog dependenciesDialog = new CompileDebugDependenciesDialog(runConfiguration, dependencies);
                dependenciesDialog.show();
                final List<DBSchemaObject> selectedDependencies =  dependenciesDialog.getSelection();

                if (dependenciesDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE){
                    if (selectedDependencies.size() > 0) {
                        new BackgroundTask(project, "Compiling dependencies", false, true){
                            @Override
                            protected void execute(@NotNull ProgressIndicator progressIndicator) {
                                DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                                for (DBSchemaObject schemaObject : selectedDependencies) {
                                    if (!progressIndicator.isCanceled()) {
                                        progressIndicator.setText("Compiling " + schemaObject.getQualifiedNameWithType());
                                        DBContentType contentType = schemaObject.getContentType();
                                        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, contentType);
                                        compilerManager.compileObject(schemaObject, CompileTypeOption.DEBUG, compilerAction);
                                    }
                                }
                                connectionHandler.getObjectBundle().refreshObjectsStatus(null);
                                if (!progressIndicator.isCanceled()) {
                                    performExecution(
                                            executionInput,
                                            environment,
                                            callback);
                                }
                            }
                        }.start();
                    } else {
                        performExecution(
                                executionInput,
                                environment,
                                callback);
                    }
                }
            }
        }.start();
    }

    private void performExecution(
            final T executionInput,
            final ExecutionEnvironment environment,
            final Callback callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                final Project project = environment.getProject();

                boolean continueExecution = promptExecutionDialog(executionInput);

                if (continueExecution) {
                    RunContentDescriptor reuseContent = environment.getContentToReuse();
                    DBDebugProcessStarter debugProcessStarter = createProcessStarter(connectionHandler);
                    XDebugSession session = null;
                    try {
                        session = XDebuggerManager.getInstance(project).startSession(
                                environment,
                                debugProcessStarter);

                        RunContentDescriptor descriptor = session.getRunContentDescriptor();

                        if (callback != null) callback.processStarted(descriptor);
                        Executor executor = environment.getExecutor();
                        if (true /*LocalHistoryConfiguration.getInstance().ADD_LABEL_ON_RUNNING*/) {
                            RunProfile runProfile = environment.getRunProfile();
                            LocalHistory.getInstance().putSystemLabel(project, executor.getId() + " " + runProfile.getName());
                        }

                        ExecutionManager.getInstance(project).getContentManager().showRunContent(executor, descriptor);

                        ProcessHandler processHandler = descriptor.getProcessHandler();
                        if (processHandler != null) {
                            processHandler.startNotify();
                            ExecutionConsole executionConsole = descriptor.getExecutionConsole();
                            if (executionConsole instanceof ConsoleView) {
                                ConsoleView consoleView = (ConsoleView) executionConsole;
                                consoleView.attachToProcess(processHandler);
                            }
                        }

                    } catch (ExecutionException e) {
                        NotificationUtil.sendErrorNotification(project, "Debugger", "Error initializing debug environment: " + e.getMessage());
                    }
                }

            }
        }.start();
    }

    protected abstract DBDebugProcessStarter createProcessStarter(ConnectionHandler connectionHandler);

    protected abstract boolean promptExecutionDialog(T executionInput);
}
