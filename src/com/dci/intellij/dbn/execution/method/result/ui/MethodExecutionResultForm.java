package com.dci.intellij.dbn.execution.method.result.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultFormBase;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.logging.ui.DatabaseLoggingResultConsole;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.util.List;

public class MethodExecutionResultForm extends ExecutionResultFormBase<MethodExecutionResult> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JLabel connectionLabel;
    private JLabel durationLabel;
    private JPanel outputCursorsPanel;
    private JTree argumentValuesTree;
    private JPanel argumentValuesPanel;
    private JPanel executionResultPanel;
    private JBScrollPane argumentValuesScrollPane;

    private final TabbedPane outputTabs;


    public MethodExecutionResultForm(@NotNull MethodExecutionResult executionResult) {
        super(executionResult);
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();
        argumentValuesTree = new ArgumentValuesTree(this, inputArgumentValues, outputArgumentValues);
        argumentValuesScrollPane.getViewport().add(argumentValuesTree);


        outputTabs = new TabbedPane(this);
        createActionsPanel();
        updateOutputTabs();

        outputCursorsPanel.add(outputTabs, BorderLayout.CENTER);

        argumentValuesPanel.setBorder(IdeBorderFactory.createBorder());
        updateStatusBarLabels();
        executionResultPanel.setSize(800, -1);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        TreeUtil.expand(argumentValuesTree, 2);
    }

    public DBMethod getMethod() {
        MethodExecutionResult executionResult = getExecutionResult();
        return executionResult.getMethod();
    }

    public void rebuildForm() {
        Dispatch.run(() -> {
            updateArgumentValueTree();
            updateOutputTabs();
            updateStatusBarLabels();
        });
    }

    private void updateArgumentValueTree() {
        MethodExecutionResult executionResult = getExecutionResult();
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();

        DBMethod method = executionResult.getMethod();
        ArgumentValuesTreeModel treeModel = new ArgumentValuesTreeModel(method, inputArgumentValues, outputArgumentValues);
        argumentValuesTree.setModel(treeModel);
        TreeUtil.expand(argumentValuesTree, 2);
    }

    private void updateOutputTabs() {
        outputTabs.removeAllTabs();
        MethodExecutionResult executionResult = getExecutionResult();
        String logOutput = executionResult.getLogOutput();
        String logConsoleName = "Output";
        ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        String databaseLogName = compatibilityInterface.getDatabaseLogName();
        if (databaseLogName != null) {
            logConsoleName = databaseLogName;
        }

        DatabaseLoggingResultConsole outputConsole = new DatabaseLoggingResultConsole(connectionHandler, logConsoleName, true);
        LogOutputContext context = new LogOutputContext(connectionHandler);
        outputConsole.writeToConsole(context,
                LogOutput.createSysOutput(context,
                        executionResult.getExecutionInput().getExecutionContext().getExecutionTimestamp(),
                        " - Method execution started", true));

        if (Strings.isNotEmptyOrSpaces(logOutput)) {
            outputConsole.writeToConsole(context, LogOutput.createStdOutput(logOutput));
        }
        outputConsole.writeToConsole(context, LogOutput.createSysOutput(context, " - Method execution finished\n\n", false));
        Disposer.register(this, outputConsole);

        TabInfo outputTabInfo = new TabInfo(outputConsole.getComponent());
        outputTabInfo.setText(outputConsole.getTitle());
        outputTabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
        outputTabInfo.setObject(outputConsole);
        outputTabs.addTab(outputTabInfo);

        boolean isFirst = true;
        List<ArgumentValue> argumentValues = executionResult.getArgumentValues();
        for (ArgumentValue argumentValue : argumentValues) {
            DBArgument argument = argumentValue.getArgument();
            if (argument != null) {
                if (argumentValue.isCursor()) {
                    MethodExecutionCursorResultForm cursorResultForm =
                            new MethodExecutionCursorResultForm(this, executionResult, argument);

                    TabInfo tabInfo = new TabInfo(cursorResultForm.getComponent());
                    tabInfo.setText(argument.getName());
                    tabInfo.setIcon(argument.getIcon());
                    tabInfo.setObject(cursorResultForm);
                    outputTabs.addTab(tabInfo);
                    if (isFirst) {
                        outputTabs.select(tabInfo, false);
                        isFirst = false;
                    }
                } else if (argumentValue.isLargeObject()) {
                    MethodExecutionLargeValueResultForm largeValueResultForm =
                            new MethodExecutionLargeValueResultForm(this, executionResult, argument);

                    TabInfo tabInfo = new TabInfo(largeValueResultForm.getComponent());
                    tabInfo.setText(argument.getName());
                    tabInfo.setIcon(argument.getIcon());
                    tabInfo.setObject(largeValueResultForm);
                    outputTabs.addTab(tabInfo);
                    if (isFirst) {
                        outputTabs.select(tabInfo, false);
                        isFirst = false;
                    }
                } else {
                    System.out.println();
                }
            } else {
                System.out.println();
            }

        }

        GUIUtil.repaint(outputTabs);
    }

    void selectArgumentOutputTab(DBArgument argument) {
        for (TabInfo tabInfo : outputTabs.getTabs()) {
            Object object = tabInfo.getObject();
            if (object instanceof MethodExecutionCursorResultForm) {
                MethodExecutionCursorResultForm cursorResultForm = (MethodExecutionCursorResultForm) object;
                if (cursorResultForm.getArgument().equals(argument)) {
                    outputTabs.select(tabInfo, true);
                    break;
                }
            } else if (object instanceof MethodExecutionLargeValueResultForm) {
                MethodExecutionLargeValueResultForm largeValueResultForm = (MethodExecutionLargeValueResultForm) object;
                if (largeValueResultForm.getArgument().equals(argument)) {
                    outputTabs.select(tabInfo, true);
                    break;
                }
            }


        }
    }

    private void updateStatusBarLabels() {
        MethodExecutionResult executionResult = getExecutionResult();
        SessionId sessionId = executionResult.getExecutionInput().getTargetSessionId();
        String connectionType =
                sessionId == SessionId.MAIN ? " (main)" :
                sessionId == SessionId.POOL ? " (pool)" : " (session)";
        ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
        connectionLabel.setIcon(connectionHandler.getIcon());
        connectionLabel.setText(connectionHandler.getName() + connectionType);

        durationLabel.setText(": " + executionResult.getExecutionDuration() + " ms");
    }



    private void createActionsPanel() {
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"DBNavigator.MethodExecutionResult.Controls", false,"DBNavigator.ActionGroup.MethodExecutionResult");
        actionsPanel.add(actionToolbar.getComponent());
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

}
