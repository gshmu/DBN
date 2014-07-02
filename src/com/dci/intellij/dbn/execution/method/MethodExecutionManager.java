package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.common.execution.MethodExecutionProcessor;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.history.ui.MethodExecutionHistoryDialog;
import com.dci.intellij.dbn.execution.method.ui.MethodExecutionDialog;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodExecutionManager extends AbstractProjectComponent implements JDOMExternalizable {
    private List<MethodExecutionInput> executionInputs = new ArrayList<MethodExecutionInput>();
    MethodBrowserSettings browserSettings = new MethodBrowserSettings();
    private boolean groupHistoryEntries;

    private MethodExecutionManager(Project project) {
        super(project);
    }

    public static MethodExecutionManager getInstance(Project project) {
        return project.getComponent(MethodExecutionManager.class);
    }

    public MethodBrowserSettings getBrowserSettings() {
        return browserSettings;
    }

    public boolean isGroupHistoryEntries() {
        return groupHistoryEntries;
    }

    public void setGroupHistoryEntries(boolean groupHistoryEntries) {
        this.groupHistoryEntries = groupHistoryEntries;
    }

    public List<MethodExecutionInput> getExecutionInputs() {
        return executionInputs;
    }

    public MethodExecutionInput getExecutionInput(DBMethod method) {
        for (MethodExecutionInput executionInput : executionInputs) {
            if (executionInput.getMethodRef().is(method)) {
                return executionInput;
            }
        }
        MethodExecutionInput executionInput = new MethodExecutionInput(method);
        executionInputs.add(executionInput);
        Collections.sort(executionInputs);
        return executionInput;
    }

    public MethodExecutionInput getExecutionInput(DBMethodRef methodRef) {
        for (MethodExecutionInput executionInput : executionInputs) {
            if (executionInput.getMethodRef().equals(methodRef)) {
                return executionInput;
            }
        }

        DBMethod method = methodRef.get();
        if (method != null) {
            MethodExecutionInput executionInput = new MethodExecutionInput(method);
            executionInputs.add(executionInput);
            return executionInput;
        }

        return null;
    }

    public boolean promptExecutionDialog(DBMethod method, boolean debug) {
        MethodExecutionInput executionInput = getExecutionInput(method);
        return promptExecutionDialog(executionInput, debug);
    }

    public boolean promptExecutionDialog(MethodExecutionInput executionInput, boolean debug) {
        if (executionInput.getConnectionHandler().isValid(true)) {
            DBMethod method = executionInput.getMethod();
            if (method == null) {
                String message =
                        "Can not execute method " +
                         executionInput.getMethodRef().getPath() + ".\nMethod not found!";
                MessageUtil.showErrorDialog(message);
            } else {
                MethodExecutionDialog executionDialog = new MethodExecutionDialog(executionInput, debug);
                executionDialog.show();

                return executionDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE;
            }
        } else {
            String message =
                    "Can not execute method " + executionInput.getMethodRef().getPath() + ".\n" +
                    "No connectivity to '" + executionInput.getConnectionHandler().getQualifiedName() + "'. " +
                    "Please check your connection settings and try again.";
            MessageUtil.showErrorDialog(message);
        }
        return false;
    }


    public MethodExecutionHistoryDialog showExecutionHistoryDialog(boolean editable) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionInputs, null, editable);
        executionHistoryDialog.show();
        return executionHistoryDialog;
    }

    public MethodExecutionHistoryDialog showExecutionHistoryDialog(MethodExecutionInput selectedExecutionInput, boolean editable) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionInputs, selectedExecutionInput, editable);
        executionHistoryDialog.show();
        return executionHistoryDialog;
    }

    public MethodExecutionInput selectHistoryMethodExecutionInput(MethodExecutionInput selectedExecutionInput) {
        MethodExecutionHistoryDialog executionHistoryDialog = new MethodExecutionHistoryDialog(getProject(), executionInputs, selectedExecutionInput, false);
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
        executionInput.setExecuting(true);
        final DBMethod method = executionInput.getMethod();
        if (method == null) {
            DBMethodRef methodRef = executionInput.getMethodRef();
            MessageUtil.showErrorDialog("Could not resolve " + methodRef.getMethodObjectType().getName() + " \"" + methodRef.getSchemaName() + "." + methodRef.getQualifiedMethodName() + "\".");
        } else {
            final Project project = method.getProject();
            DatabaseExecutionInterface executionInterface = method.getConnectionHandler().getInterfaceProvider().getDatabaseExecutionInterface();
            final MethodExecutionProcessor executionProcessor = executionInterface.createExecutionProcessor(method);

            new BackgroundTask(project, "Executing method", false) {
                public void execute(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        initProgressIndicator(progressIndicator, true, "Executing " + method.getQualifiedNameWithType());
                        executionInput.initExecutionResult(false);
                        executionProcessor.execute(executionInput);
                        if (!executionInput.isExecutionCancelled()) {
                            new SimpleLaterInvocator() {
                                public void execute() {
                                    ExecutionManager executionManager = ExecutionManager.getInstance(project);
                                    executionManager.showExecutionConsole(executionInput.getExecutionResult());
                                    executionInput.setExecuting(false);
                                }
                            }.start();
                        }

                        executionInput.setExecutionCancelled(false);
                    } catch (final SQLException e) {
                        executionInput.setExecuting(false);
                        if (!executionInput.isExecutionCancelled()) {
                            new SimpleLaterInvocator() {
                                public void execute() {
                                    MessageUtil.showErrorDialog("Could not execute " + method.getTypeName() + ".", e);
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

    public boolean debugExecute(final MethodExecutionInput executionInput, final Connection connection) {
        final DBMethod method = executionInput.getMethod();
        DatabaseExecutionInterface executionInterface = method.getConnectionHandler().getInterfaceProvider().getDatabaseExecutionInterface();
        final MethodExecutionProcessor executionProcessor = executionInterface.createDebugExecutionProcessor(method);
        try {
            executionInput.initExecutionResult(true);
            executionProcessor.execute(executionInput, connection);
            if (!executionInput.isExecutionCancelled()) {
                new SimpleLaterInvocator() {
                    public void execute() {
                        ExecutionManager executionManager = ExecutionManager.getInstance(method.getProject());
                        executionManager.showExecutionConsole(executionInput.getExecutionResult());
                    }
                }.start();
            }
            executionInput.setExecutionCancelled(false);
            return true;
        } catch (final SQLException e) {
            if (!executionInput.isExecutionCancelled()) {
                new SimpleLaterInvocator() {
                    public void execute() {
                        MessageUtil.showErrorDialog("Could not execute " + method.getTypeName() + ".", e);
                    }
                }.start();
            }
            return false;
        }
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
        for (MethodExecutionInput executionInput : executionInputs) {
            executionInput.dispose();
        }
        super.disposeComponent();
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readExternal(Element element) throws InvalidDataException {
        groupHistoryEntries = SettingsUtil.getBoolean(element, "group-history-entries", true);

        Element browserSettingsElement = element.getChild("method-browser");
        if (browserSettingsElement != null)
            browserSettings.readConfiguration(browserSettingsElement);

        Element congfigsElement = element.getChild("execution-inputs");
        for (Object object : congfigsElement.getChildren()) {
            Element configElement = (Element) object;
            MethodExecutionInput executionInput = new MethodExecutionInput();
            executionInput.readConfiguration(configElement);
            // backward compatibility
            if (executionInput.getMethodRef().getSchemaName() != null) {
                executionInputs.add(executionInput);
            }
        }
        Collections.sort(executionInputs);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        SettingsUtil.setBoolean(element, "group-history-entries", groupHistoryEntries);

        Element browserSettingsElement = new Element("method-browser");
        browserSettings.writeConfiguration(browserSettingsElement);
        element.addContent(browserSettingsElement);

        Element configsElement = new Element("execution-inputs");
        element.addContent(configsElement);
        for (MethodExecutionInput executionInput : this.executionInputs) {
            Element configElement = new Element("execution-input");
            executionInput.writeConfiguration(configElement);
            configsElement.addContent(configElement);
        }
    }

    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        this.executionInputs = executionInputs;
    }


}
