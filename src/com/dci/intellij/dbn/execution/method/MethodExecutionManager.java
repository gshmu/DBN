package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionHistory;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@State(
    name = "DBNavigator.Project.MethodExecutionManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class MethodExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private MethodBrowserSettings browserSettings = new MethodBrowserSettings();
    private MethodExecutionHistory executionHistory = new MethodExecutionHistory();

    private MethodExecutionManager(Project project) {
        super(project);
    }

    public static MethodExecutionManager getInstance(Project project) {
        return project.getComponent(MethodExecutionManager.class);
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

    public MethodExecutionInput getExecutionInput(DBMethodRef methodRef) {
        return executionHistory.getExecutionInput(methodRef);
    }

    public boolean promptExecutionDialog(DBMethod method, boolean debug) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        return promptExecutionDialog(executionInput, debug);
    }

    public boolean promptExecutionDialog(final MethodExecutionInput executionInput, final boolean debug) {
        final AtomicBoolean result = new AtomicBoolean(false);
        new ConnectionAction(executionInput) {
            @Override
            public void execute() {
                Project project = getProject();
                ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                if (connectionHandler.isValid(true)) {
                    DBMethod method = executionInput.getMethod();
                    if (method == null) {
                        String message =
                                "Can not execute method " +
                                        executionInput.getMethodRef().getPath() + ".\nMethod not found!";
                        MessageUtil.showErrorDialog(project, message);
                    } else {
                        MethodExecutionDialog executionDialog = new MethodExecutionDialog(executionInput, debug);
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

    public MethodExecutionInput selectHistoryMethodExecutionInput(MethodExecutionInput selectedExecutionInput) {
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
        executionHistory.setSelection(executionInput.getMethodRef());
        executionInput.setExecuting(true);
        final DBMethod method = executionInput.getMethod();
        if (method == null) {
            DBMethodRef methodRef = executionInput.getMethodRef();
            MessageUtil.showErrorDialog(getProject(), "Could not resolve " + methodRef.getMethodObjectType().getName() + " \"" + methodRef.getSchemaName() + "." + methodRef.getQualifiedMethodName() + "\".");
        } else {
            final Project project = method.getProject();
            DatabaseExecutionInterface executionInterface = method.getConnectionHandler().getInterfaceProvider().getDatabaseExecutionInterface();
            final MethodExecutionProcessor executionProcessor = executionInterface.createExecutionProcessor(method);

            new BackgroundTask(project, "Executing method", false) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        initProgressIndicator(progressIndicator, true, "Executing " + method.getQualifiedNameWithType());
                        executionInput.initExecutionResult(false);
                        executionProcessor.execute(executionInput, false);
                        if (!executionInput.isExecutionCancelled()) {
                            ExecutionManager executionManager = ExecutionManager.getInstance(project);
                            executionManager.addExecutionResult(executionInput.getExecutionResult());
                            executionInput.setExecuting(false);
                        }

                        executionInput.setExecutionCancelled(false);
                    } catch (final SQLException e) {
                        executionInput.setExecuting(false);
                        if (!executionInput.isExecutionCancelled()) {
                            new SimpleLaterInvocator() {
                                public void execute() {
                                    MessageUtil.showErrorDialog(project, "Could not execute " + method.getTypeName() + ".", e);
                                    if (promptExecutionDialog(executionInput, false)) {
                                        MethodExecutionManager.this.execute(executionInput);
                                    }
                                }
                            }.start();
                        }
                    }
                }
            }.start();
        }
    }

    public void debugExecute(final MethodExecutionInput executionInput, final Connection connection) throws SQLException {
        final DBMethod method = executionInput.getMethod();
        if (method != null) {
            DatabaseExecutionInterface executionInterface = method.getConnectionHandler().getInterfaceProvider().getDatabaseExecutionInterface();
            final MethodExecutionProcessor executionProcessor = executionInterface.createDebugExecutionProcessor(method);

            executionInput.initExecutionResult(true);
            executionProcessor.execute(executionInput, connection, true);
            if (!executionInput.isExecutionCancelled()) {
                ExecutionManager executionManager = ExecutionManager.getInstance(method.getProject());
                executionManager.addExecutionResult(executionInput.getExecutionResult());
            }
            executionInput.setExecutionCancelled(false);
        }
    }


    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        executionHistory.setExecutionInputs(executionInputs);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.MethodExecutionManager";
    }

    @Override
    public void disposeComponent() {
        executionHistory.dispose();
        super.disposeComponent();
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


        Element executionHistoryElement = new Element("execution-history");
        element.addContent(executionHistoryElement);
        executionHistory.writeState(executionHistoryElement);
        return element;
    }

    @Override
    public void loadState(Element element) {
        Element browserSettingsElement = element.getChild("method-browser");
        if (browserSettingsElement != null) {
            browserSettings.readConfiguration(browserSettingsElement);
        }

        Element executionHistoryElement = element.getChild("execution-history");
        if (executionHistoryElement == null) {

            // TODO backward compatibility (to remove)
            executionHistory.setGroupEntries(SettingsUtil.getBoolean(element, "group-history-entries", true));

            List<MethodExecutionInput> executionInputs = executionHistory.getExecutionInputs();
            Element executionInputsElement = element.getChild("execution-inputs");
            for (Object object : executionInputsElement.getChildren()) {
                Element configElement = (Element) object;
                MethodExecutionInput executionInput = new MethodExecutionInput();
                executionInput.readConfiguration(configElement);
                if (executionInput.getMethodRef().getSchemaName() != null) {
                    executionInputs.add(executionInput);
                }
            }
            Collections.sort(executionInputs);

            Element selectionElement = element.getChild("history-selection");
            if (selectionElement != null) {
                DBMethodRef selection = new DBMethodRef();
                selection.readState(selectionElement);
                executionHistory.setSelection(selection);
            }
        } else {
            executionHistory.readState(executionHistoryElement);
        }
    }


}
