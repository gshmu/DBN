package com.dci.intellij.dbn.execution.method;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionInputDialog;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;

@State(
    name = "DBNavigator.Project.MethodExecutionManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class MethodExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private MethodBrowserSettings browserSettings = new MethodBrowserSettings();
    private MethodExecutionHistory executionHistory = new MethodExecutionHistory(getProject());
    private MethodExecutionArgumentValuesCache argumentValuesCache = new MethodExecutionArgumentValuesCache();

    private MethodExecutionManager(Project project) {
        super(project);
    }

    public static MethodExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, MethodExecutionManager.class);
    }

    public MethodBrowserSettings getBrowserSettings() {
        return browserSettings;
    }

    public MethodExecutionHistory getExecutionHistory() {
        return executionHistory;
    }

    public MethodExecutionInput getExecutionInput(DBMethod method) {
        return executionHistory.getExecutionInput(method);
    }

    public MethodExecutionInput getExecutionInput(DBObjectRef<DBMethod> methodRef) {
        return executionHistory.getExecutionInput(methodRef);
    }

    public MethodExecutionArgumentValuesCache getArgumentValuesCache() {
        return argumentValuesCache;
    }

    public boolean promptExecutionDialog(DBMethod method, @NotNull DBDebuggerType debuggerType) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        return promptExecutionDialog(executionInput, debuggerType);
    }

    public boolean promptExecutionDialog(final MethodExecutionInput executionInput, final @NotNull DBDebuggerType debuggerType) {
        final AtomicBoolean result = new AtomicBoolean(false);
        new ConnectionAction("the method execution", executionInput) {
            @Override
            protected void execute() {
                Project project = getProject();
                ConnectionHandler connectionHandler = getConnectionHandler();
                if (connectionHandler.isValid(true)) {
                    DBMethod method = executionInput.getMethod();
                    if (method == null) {
                        String message =
                                "Can not execute method " +
                                        executionInput.getMethodRef().getPath() + ".\nMethod not found!";
                        MessageUtil.showErrorDialog(project, message);
                    } else {
                        MethodExecutionInputDialog executionDialog = new MethodExecutionInputDialog(executionInput, debuggerType);
                        executionDialog.show();

                        result.set(executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE);
                    }
                } else {
                    String message =
                            "Can not execute method " + executionInput.getMethodRef().getPath() + ".\n" +
                                    "No connectivity to '" + connectionHandler.getQualifiedName() + "'. " +
                                    "Please check your connection settings and try again.";
                    MessageUtil.showErrorDialog(project, message);
                }
            }
        }.start();
        return result.get();
    }


    public MethodExecutionHistoryDialog showExecutionHistoryDialog(boolean editable) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionHistory, null, editable);
        executionHistoryDialog.show();
        return executionHistoryDialog;
    }

    public MethodExecutionHistoryDialog showExecutionHistoryDialog(MethodExecutionInput selectedExecutionInput, boolean editable) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionHistory, selectedExecutionInput, editable);
        executionHistoryDialog.show();
        return executionHistoryDialog;
    }

    public MethodExecutionInput selectHistoryMethodExecutionInput(@Nullable MethodExecutionInput selectedExecutionInput) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionHistory, selectedExecutionInput, false);
        executionHistoryDialog.show();
        if (executionHistoryDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            return executionHistoryDialog.getSelectedExecutionInput();   
        }
        return null;
    }

    public void execute(DBMethod method) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        execute(executionInput);
    }

    public void execute(final MethodExecutionInput executionInput) {
        cacheArgumentValues(executionInput);
        executionHistory.setSelection(executionInput.getMethodRef());
        final DBMethod method = executionInput.getMethod();
        final ExecutionContext executionContext = executionInput.getExecutionContext();
        executionContext.setExecuting(true);

        if (method == null) {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            MessageUtil.showErrorDialog(getProject(), "Could not resolve " + methodRef.getQualifiedNameWithType() + "\".");
        } else {
            final Project project = method.getProject();
            ConnectionHandler connectionHandler = FailsafeUtil.get(method.getConnectionHandler());
            DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
            final MethodExecutionProcessor executionProcessor = executionInterface.createExecutionProcessor(method);

            new BackgroundTask(project, "Executing method", false, true) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        initProgressIndicator(progressIndicator, true, "Executing " + method.getQualifiedNameWithType());
                        executionProcessor.execute(executionInput, DBDebuggerType.NONE);
                        if (!executionContext.isExecutionCancelled()) {
                            ExecutionManager executionManager = ExecutionManager.getInstance(project);
                            executionManager.addExecutionResult(executionInput.getExecutionResult());
                            executionContext.setExecuting(false);
                        }

                        executionContext.setExecutionCancelled(false);
                    } catch (final SQLException e) {
                        executionContext.setExecuting(false);
                        if (!executionContext.isExecutionCancelled()) {
                            MessageUtil.showErrorDialog(project,
                                    "Method execution error",
                                    "Error executing " + method.getQualifiedNameWithType() + ".\n" + e.getMessage().trim(),
                                    new String[]{"Try Again", "Cancel"}, 0, new SimpleTask() {
                                        @Override
                                        protected void execute() {
                                            if (getOption() == 0) {
                                                if (promptExecutionDialog(executionInput, DBDebuggerType.NONE)) {
                                                    MethodExecutionManager.this.execute(executionInput);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                }
            }.start();
        }
    }

    private void cacheArgumentValues(MethodExecutionInput executionInput) {
        ConnectionHandler connectionHandler = executionInput.getExecutionContext().getTargetConnection();
        if (connectionHandler != null) {
            Set<MethodExecutionArgumentValue> argumentValues = executionInput.getArgumentValues();
            for (MethodExecutionArgumentValue argumentValue : argumentValues) {
                argumentValuesCache.cacheVariable(connectionHandler.getId(), argumentValue.getName(), argumentValue.getValue());
            }
        }
    }

    public void debugExecute(final MethodExecutionInput executionInput, final Connection connection, DBDebuggerType debuggerType) throws SQLException {
        final DBMethod method = executionInput.getMethod();
        if (method != null) {
            ConnectionHandler connectionHandler = method.getConnectionHandler();
            DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
            final MethodExecutionProcessor executionProcessor = debuggerType == DBDebuggerType.JDWP ?
                    executionInterface.createExecutionProcessor(method) :
                    executionInterface.createDebugExecutionProcessor(method);

            executionProcessor.execute(executionInput, connection, debuggerType);
            ExecutionContext executionContext = executionInput.getExecutionContext();
            if (!executionContext.isExecutionCancelled()) {
                ExecutionManager executionManager = ExecutionManager.getInstance(method.getProject());
                executionManager.addExecutionResult(executionInput.getExecutionResult());
            }
            executionContext.setExecutionCancelled(false);
        }
    }


    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        executionHistory.setExecutionInputs(executionInputs);
    }

    public void cleanupExecutionHistory(List<String> connectionIds) {
        executionHistory.cleanupHistory(connectionIds);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.MethodExecutionManager";
    }

    public void projectOpened() {
    }

    @Override
    public void dispose() {
        super.dispose();
        Disposer.dispose(executionHistory);
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        Element browserSettingsElement = new Element("method-browser");
        element.addContent(browserSettingsElement);
        browserSettings.writeConfiguration(browserSettingsElement);


        executionHistory.writeState(element);
        argumentValuesCache.writeState(element);
        return element;
    }

    @Override
    public void loadState(Element element) {
        Element browserSettingsElement = element.getChild("method-browser");
        if (browserSettingsElement != null) {
            browserSettings.readConfiguration(browserSettingsElement);
        }

        executionHistory.readState(element);
        argumentValuesCache.readState(element);
    }
}
