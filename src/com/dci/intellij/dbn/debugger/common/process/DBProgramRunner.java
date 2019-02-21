package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.ui.CompileDebugDependenciesDialog;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.compiler.CompileManagerListener;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.object.DBMethod;
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
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;
import static com.dci.intellij.dbn.common.util.MessageUtil.options;
import static com.dci.intellij.dbn.common.util.MessageUtil.showWarningDialog;

public abstract class DBProgramRunner<T extends ExecutionInput> extends GenericProgramRunner {
    public static final String INVALID_RUNNER_ID = "DBNInvalidRunner";

    @Override
    @Nullable
    protected RunContentDescriptor doExecute(@NotNull Project project, @NotNull RunProfileState state, RunContentDescriptor contentToReuse, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return doExecute(project, env.getExecutor(), state, contentToReuse, env);
    }

    private RunContentDescriptor doExecute(
            Project project,
            Executor executor,
            RunProfileState state,
            RunContentDescriptor contentToReuse,
            ExecutionEnvironment environment) throws ExecutionException {

        DBRunConfig runProfile = (DBRunConfig) environment.getRunProfile();
        ConnectionAction.invoke("the debug execution",
                runProfile.getConnectionHandler(),
                instructions("Checking debug privileges", TaskInstruction.CANCELLABLE),
                action -> {
                    performPrivilegeCheck(
                            project,
                            (T) runProfile.getExecutionInput(),
                            environment,
                            null);
                },
                null,
                (action) -> {
                    ConnectionHandler connectionHandler = action.getConnectionHandler();
                    DatabaseDebuggerManager databaseDebuggerManager = DatabaseDebuggerManager.getInstance(project);
                    return databaseDebuggerManager.checkForbiddenOperation(connectionHandler,
                            "Another debug session is active on this connection. You can only run one debug session at the time.");
                });
        return null;
    }

    private void performPrivilegeCheck(
            Project project,
            T executionInput,
            ExecutionEnvironment environment,
            Callback callback) {
        DBRunConfig runProfile = (DBRunConfig) environment.getRunProfile();
        ConnectionHandler connectionHandler = runProfile.getConnectionHandler();
        if (connectionHandler == null) {

        } else {
            DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
            List<String> missingPrivileges = debuggerManager.getMissingDebugPrivileges(connectionHandler);
            if (missingPrivileges.size() > 0) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("The current user (").append(connectionHandler.getUserName()).append(") does not have sufficient privileges to perform debug operations on this database.\n");
                buffer.append("Please contact your administrator to grant the required privileges. ");
                buffer.append("Missing privileges:\n");
                for (String missingPrivilege : missingPrivileges) {
                    buffer.append(" - ").append(missingPrivilege).append("\n");
                }

                showWarningDialog(
                        project, "Insufficient privileges", buffer.toString(),
                        options("Continue anyway", "Cancel"), 0,
                        MessageCallback.create(0, option -> {
                            performInitialize(
                                    connectionHandler,
                                    executionInput,
                                    environment,
                                    callback);
                        }));
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
            @NotNull ConnectionHandler connectionHandler,
            @NotNull T executionInput,
            @NotNull ExecutionEnvironment environment,
            @Nullable Callback callback) {
        DBRunConfig runProfile = (DBRunConfig) environment.getRunProfile();
        if (runProfile.isCompileDependencies()) {
            Project project = connectionHandler.getProject();

            BackgroundTask.invoke(project,
                    instructions("Initializing debug environment", TaskInstruction.CANCELLABLE),
                    (data, progress) -> {
                        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                        BackgroundTask.initProgressIndicator(progress, true, "Loading method dependencies");
                        if (!project.isDisposed() && !progress.isCanceled()) {
                            List<DBMethod> methods = runProfile.getMethods();
                            List<DBSchemaObject> dependencies = debuggerManager.loadCompileDependencies(methods, progress);
                            if (!progress.isCanceled()) {
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
                    });
        }
    }

    private void performCompile(
            @NotNull ConnectionHandler connectionHandler,
            @NotNull T executionInput,
            @NotNull ExecutionEnvironment environment,
            @Nullable Callback callback,
            List<DBSchemaObject> dependencies) {

        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            Project project = connectionHandler.getProject();
            DBRunConfig runConfiguration = (DBRunConfig) environment.getRunProfile();
            CompileDebugDependenciesDialog dependenciesDialog = new CompileDebugDependenciesDialog(runConfiguration, dependencies);
            dependenciesDialog.show();
            List<DBSchemaObject> selectedDependencies =  dependenciesDialog.getSelection();

            if (dependenciesDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE){
                if (selectedDependencies.size() > 0) {

                    BackgroundTask.invoke(project,
                            instructions("Compiling dependencies", TaskInstruction.CANCELLABLE),
                            (data, progress) -> {
                                DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                                for (DBSchemaObject schemaObject : selectedDependencies) {
                                    if (!progress.isCanceled()) {
                                        progress.setText("Compiling " + schemaObject.getQualifiedNameWithType());
                                        DBContentType contentType = schemaObject.getContentType();
                                        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, contentType);
                                        compilerManager.compileObject(schemaObject, CompileType.DEBUG, compilerAction);
                                    }
                                }
                                EventUtil.notify(project, CompileManagerListener.TOPIC).compileFinished(connectionHandler, null);
                                if (!progress.isCanceled()) {
                                    performExecution(
                                            executionInput,
                                            environment,
                                            callback);
                                }

                            });
                } else {
                    performExecution(
                            executionInput,
                            environment,
                            callback);
                }
            }
        });
    }

    private void performExecution(
            T executionInput,
            ExecutionEnvironment environment,
            Callback callback) {
        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
            Project project = environment.getProject();

            promptExecutionDialog(executionInput, SimpleTask.create(data -> {
                DBDebugProcessStarter debugProcessStarter = createProcessStarter(connectionHandler);
                try {
                    XDebugSession session = XDebuggerManager.getInstance(project).startSession(
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
            }));
        });
    }

    protected abstract DBDebugProcessStarter createProcessStarter(ConnectionHandler connectionHandler);

    protected abstract void promptExecutionDialog(T executionInput, RunnableTask callback);
}
