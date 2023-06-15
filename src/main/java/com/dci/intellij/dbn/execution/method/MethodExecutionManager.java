package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.routine.Consumer;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionConfigListener;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.database.interfaces.DatabaseExecutionInterface;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserDialog;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionInputDialog;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import lombok.Getter;
import lombok.val;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.conditionallyLog;
import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.execution.ExecutionStatus.CANCELLED;
import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;

@State(
    name = MethodExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Getter
public class MethodExecutionManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.MethodExecutionManager";

    private final MethodBrowserSettings browserSettings = new MethodBrowserSettings();
    private final MethodExecutionHistory executionHistory = new MethodExecutionHistory(getProject());
    private final MethodExecutionArgumentValueHistory argumentValuesHistory = new MethodExecutionArgumentValueHistory();

    private MethodExecutionManager(Project project) {
        super(project, COMPONENT_NAME);
        ProjectEvents.subscribe(project, this, ConnectionConfigListener.TOPIC, connectionConfigListener());
    }

    @NotNull
    private ConnectionConfigListener connectionConfigListener() {
        return new ConnectionConfigListener() {
            @Override
            public void connectionRemoved(ConnectionId connectionId) {
                browserSettings.connectionRemoved(connectionId);
                executionHistory.connectionRemoved(connectionId);
                argumentValuesHistory.connectionRemoved(connectionId);
            }
        };
    }

    public static MethodExecutionManager getInstance(@NotNull Project project) {
        return projectService(project, MethodExecutionManager.class);
    }

    public MethodExecutionInput getExecutionInput(DBMethod method) {
        return executionHistory.getExecutionInput(method);
    }

    @NotNull
    public MethodExecutionInput getExecutionInput(DBObjectRef<DBMethod> methodRef) {
        return executionHistory.getExecutionInput(methodRef, true);
    }

    public void startMethodExecution(@NotNull MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        promptExecutionDialog(executionInput, debuggerType, () -> MethodExecutionManager.this.execute(executionInput));
    }

    public void startMethodExecution(@NotNull DBMethod method, @NotNull DBDebuggerType debuggerType) {
        promptExecutionDialog(method, debuggerType, () -> MethodExecutionManager.this.execute(method));
    }

    private void promptExecutionDialog(@NotNull DBMethod method, @NotNull DBDebuggerType debuggerType, Runnable callback) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        promptExecutionDialog(executionInput, debuggerType, callback);
    }

    public void promptExecutionDialog(MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType, Runnable callback) {
        Project project = executionInput.getProject();
        DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();

        ConnectionAction.invoke("the method execution", false, executionInput,
                action -> Progress.prompt(project, action, true,
                        "Loading method details",
                        "Loading details of " + methodRef.getQualifiedNameWithType(),
                        progress -> {
                            ConnectionHandler connection = action.getConnection();
                            String methodIdentifier = methodRef.getPath();
                            if (connection.isValid()) {
                                DBMethod method = executionInput.getMethod();
                                if (method == null) {
                                    String message = "Can not execute method " + methodIdentifier + ".\nMethod not found!";
                                    Messages.showErrorDialog(project, message);
                                } else {
                                    // load the arguments while in background
                                    executionInput.getMethod().getArguments();
                                    showInputDialog(executionInput, debuggerType, callback);
                                }
                            } else {
                                String message =
                                        "Can not execute method " + methodIdentifier + ".\n" +
                                                "No connectivity to '" + connection.getName() + "'. " +
                                                "Please check your connection settings and try again.";
                                Messages.showErrorDialog(project, message);
                            }
                        }));
    }

    private void showInputDialog(@NotNull MethodExecutionInput executionInput, @NotNull DBDebuggerType debuggerType, @NotNull Runnable executor) {
        MethodExecutionInputDialog.open(executionInput, debuggerType, executor);
    }


    public void showExecutionHistoryDialog(
            MethodExecutionInput selection,
            boolean editable,
            boolean debug,
            Consumer<MethodExecutionInput> callback) {

        Project project = getProject();
        Progress.modal(project, selection, true,
                "Loading data dictionary",
                "Loading method execution history",
                progress -> {
                    MethodExecutionInput selectedInput = Commons.nvln(selection, executionHistory.getLastSelection());
                    if (selectedInput != null) {
                        // initialize method arguments while in background
                        DBMethod method = selectedInput.getMethod();
                        if (isValid(method)) {
                            method.getArguments();
                        }
                    }

                    if (!progress.isCanceled()) {
                        Dispatch.run(() -> {
                            MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(project, selectedInput, editable, debug);
                            executionHistoryDialog.show();
                            MethodExecutionInput newlySelected = executionHistoryDialog.getSelectedExecutionInput();
                            if (newlySelected != null && callback != null) {
                                if (executionHistoryDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                                    callback.accept(newlySelected);
                                }
                            }
                        });
                    }
                });
    }

    public void execute(DBMethod method) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        execute(executionInput);
    }

    public void execute(MethodExecutionInput input) {
        cacheArgumentValues(input);
        executionHistory.setSelection(input.getMethodRef());
        DBMethod method = input.getMethod();
        MethodExecutionContext context = input.getExecutionContext();
        context.set(EXECUTING, true);

        if (method == null) {
            DBObjectRef<DBMethod> methodRef = input.getMethodRef();
            Messages.showErrorDialog(getProject(), "Could not resolve " + methodRef.getQualifiedNameWithType() + "\".");
        } else {
            Project project = method.getProject();
            ConnectionHandler connection = Failsafe.nn(method.getConnection());
            DatabaseExecutionInterface executionInterface = connection.getInterfaces().getExecutionInterface();
            MethodExecutionProcessor executionProcessor = executionInterface.createExecutionProcessor(method);

            Progress.prompt(project, method, true,
                    "Executing method",
                    "Executing " + method.getQualifiedNameWithType(),
                    progress -> {
                try {
                    executionProcessor.execute(input, DBDebuggerType.NONE);
                    if (context.isNot(CANCELLED)) {
                        ExecutionManager executionManager = ExecutionManager.getInstance(project);
                        executionManager.addExecutionResult(input.getExecutionResult());
                        context.set(EXECUTING, false);
                    }

                    context.set(CANCELLED, false);
                } catch (SQLException e) {
                    conditionallyLog(e);
                    context.set(EXECUTING, false);
                    if (context.isNot(CANCELLED)) {
                        Messages.showErrorDialog(project,
                                "Method execution error",
                                "Error executing " + method.getQualifiedNameWithType() + ".\n" + e.getMessage().trim(),
                                new String[]{"Try Again", "Cancel"}, 0,
                                option -> when(option == 0, () ->
                                        startMethodExecution(input, DBDebuggerType.NONE)));
                    }
                }
            });
        }
    }

    private void cacheArgumentValues(MethodExecutionInput input) {
        ConnectionHandler connection = input.getExecutionContext().getTargetConnection();
        if (connection != null) {
            for (val entry : input.getArgumentValueHistory().entrySet()) {
                MethodExecutionArgumentValue argumentValue = entry.getValue();

                argumentValuesHistory.cacheVariable(
                        connection.getConnectionId(),
                        argumentValue.getName(),
                        argumentValue.getValue());
            }
        }
    }

    public void debugExecute(
            @NotNull MethodExecutionInput input,
            @NotNull DBNConnection conn,
            DBDebuggerType debuggerType) throws SQLException {

        DBMethod method = input.getMethod();
        if (method != null) {
            ConnectionHandler connection = method.getConnection();
            DatabaseExecutionInterface executionInterface = connection.getInterfaces().getExecutionInterface();
            MethodExecutionProcessor executionProcessor = debuggerType == DBDebuggerType.JDWP ?
                    executionInterface.createExecutionProcessor(method) :
                    executionInterface.createDebugExecutionProcessor(method);

            executionProcessor.execute(input, conn, debuggerType);
            MethodExecutionContext context = input.getExecutionContext();
            if (context.isNot(CANCELLED)) {
                ExecutionManager executionManager = ExecutionManager.getInstance(method.getProject());
                executionManager.addExecutionResult(input.getExecutionResult());
            }
            context.set(CANCELLED, false);
        }
    }

    public void promptMethodBrowserDialog(
            @Nullable MethodExecutionInput executionInput, boolean debug,
            @Nullable Consumer<MethodExecutionInput> callback) {

        Project project = getProject();
        Progress.prompt(project, executionInput, true,
                "Loading data dictionary",
                "Loading executable elements",
                progress -> {
                    MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                    MethodBrowserSettings settings = executionManager.getBrowserSettings();
                    DBMethod currentMethod = executionInput == null ? null : executionInput.getMethod();
                    if (currentMethod != null) {
                        currentMethod.getArguments();
                        settings.setSelectedConnection(currentMethod.getConnection());
                        settings.setSelectedSchema(currentMethod.getSchema());
                        settings.setSelectedMethod(currentMethod);
                    }

                    DBSchema schema = settings.getSelectedSchema();
                    ObjectTreeModel objectTreeModel = !debug || DatabaseFeature.DEBUGGING.isSupported(schema) ?
                            new ObjectTreeModel(schema, settings.getVisibleObjectTypes(), settings.getSelectedMethod()) :
                            new ObjectTreeModel(null, settings.getVisibleObjectTypes(), null);

                    Dispatch.run(() -> {
                        Failsafe.nn(project);
                        MethodExecutionBrowserDialog browserDialog = new MethodExecutionBrowserDialog(project, objectTreeModel, true);
                        browserDialog.show();
                        if (browserDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                            DBMethod method = browserDialog.getSelectedMethod();
                            MethodExecutionInput methodExecutionInput = executionManager.getExecutionInput(method);
                            if (callback != null && methodExecutionInput != null) {
                                callback.accept(methodExecutionInput);
                            }
                        }
                    });
                });
    }


    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        executionHistory.setExecutionInputs(executionInputs);
    }

    public void cleanupExecutionHistory(List<ConnectionId> connectionIds) {
        executionHistory.cleanupHistory(connectionIds);
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        Element browserSettingsElement = new Element("method-browser");
        element.addContent(browserSettingsElement);
        browserSettings.writeConfiguration(browserSettingsElement);


        executionHistory.writeState(element);
        argumentValuesHistory.writeState(element);
        return element;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
        Element browserSettingsElement = element.getChild("method-browser");
        if (browserSettingsElement != null) {
            browserSettings.readConfiguration(browserSettingsElement);
        }

        executionHistory.readState(element);
        argumentValuesHistory.readState(element);
    }




    @Override
    public void disposeInner() {
        Disposer.dispose(executionHistory);
        super.disposeInner();
    }
}
