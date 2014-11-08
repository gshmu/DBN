package com.dci.intellij.dbn.execution.method.result.ui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;
import java.io.StringReader;
import java.util.List;
import org.jetbrains.generate.tostring.util.StringUtil;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.execution.common.output.ui.ExecutionLogOutputConsole;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.method.ArgumentValue;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.action.CloseExecutionResultAction;
import com.dci.intellij.dbn.execution.method.result.action.EditMethodAction;
import com.dci.intellij.dbn.execution.method.result.action.OpenSettingsAction;
import com.dci.intellij.dbn.execution.method.result.action.PromptMethodExecutionAction;
import com.dci.intellij.dbn.execution.method.result.action.StartMethodExecutionAction;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.diagnostic.logging.LogConsoleBase;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.ui.tree.TreeUtil;

public class MethodExecutionResultForm extends DBNFormImpl implements ExecutionResultForm<MethodExecutionResult> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel statusPanel;
    private JLabel connectionLabel;
    private JLabel durationLabel;
    private JPanel outputCursorsPanel;
    private JTree argumentValuesTree;
    private JPanel argumentValuesPanel;
    private JPanel executionResultPanel;
    private TabbedPane cursorOutputTabs;


    private MethodExecutionResult executionResult;

    public MethodExecutionResultForm(MethodExecutionResult executionResult) {
        this.executionResult = executionResult;
        cursorOutputTabs = new TabbedPane(this);
        createActionsPanel();
        updateCursorArgumentsPanel();

        outputCursorsPanel.add(cursorOutputTabs, BorderLayout.CENTER);

        argumentValuesPanel.setBorder(IdeBorderFactory.createBorder());
        updateStatusBarLabels();
        executionResultPanel.setSize(800, -1);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        TreeUtil.expand(argumentValuesTree, 2);

        Disposer.register(this, cursorOutputTabs);
        Disposer.register(this, executionResult);
    }

    public void setExecutionResult(MethodExecutionResult executionResult) {
        if (this.executionResult != executionResult) {
            MethodExecutionResult oldExecutionResult = this.executionResult;
            this.executionResult = executionResult;
            rebuild();

            DisposerUtil.dispose(oldExecutionResult);
        }
    }

    public MethodExecutionResult getExecutionResult() {
        return executionResult;
    }

    public DBMethod getMethod() {
        return executionResult.getMethod();
    }

    public void rebuild() {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                updateArgumentValueTables();
                updateCursorArgumentsPanel();
                updateStatusBarLabels();
            }
        }.start();
    }

    private void updateArgumentValueTables() {
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();

        DBMethod method = executionResult.getMethod();
        ArgumentValuesTreeModel treeModel = new ArgumentValuesTreeModel(method, inputArgumentValues, outputArgumentValues);
        argumentValuesTree.setModel(treeModel);
        TreeUtil.expand(argumentValuesTree, 2);
    }

    private void updateCursorArgumentsPanel() {
        cursorOutputTabs.removeAllTabs();
        Project project = getMethod().getProject();
        String logOutput = executionResult.getLogOutput();
        StringReader stringReader = null;
        if (StringUtil.isNotEmpty(logOutput)) {
            stringReader = new StringReader(logOutput);
        }
        String logConsoleName = "Output";
        ConnectionHandler connectionHandler = getExecutionResult().getConnectionHandler();
        if (connectionHandler != null) {
            DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
            String databaseLogName = compatibilityInterface.getDatabaseLogName();
            if (databaseLogName != null) {
                logConsoleName = databaseLogName;
            }
        }


        LogConsoleBase outputConsole = new ExecutionLogOutputConsole(project, stringReader, logConsoleName);
        outputConsole.activate();

        TabInfo outputTabInfo = new TabInfo(outputConsole.getComponent());
        outputTabInfo.setText(outputConsole.getTitle());
        outputTabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
        cursorOutputTabs.addTab(outputTabInfo);

        boolean isFirst = true;
        for (ArgumentValue argumentValue : executionResult.getArgumentValues()) {
            if (argumentValue.isCursor()) {
                DBArgument argument = argumentValue.getArgument();

                MethodExecutionCursorResultForm cursorResultComponent =
                        new MethodExecutionCursorResultForm(executionResult, argument);

                TabInfo tabInfo = new TabInfo(cursorResultComponent.getComponent());
                tabInfo.setText(argument.getName());
                tabInfo.setIcon(argument.getIcon());
                tabInfo.setObject(argument);
                cursorOutputTabs.addTab(tabInfo);
                if (isFirst) {
                    cursorOutputTabs.select(tabInfo, false);
                    isFirst = false;
                }
            }
        }

        cursorOutputTabs.revalidate();
        cursorOutputTabs.repaint();
    }

    public void selectCursorOutput(DBArgument argument) {
        for (TabInfo tabInfo : cursorOutputTabs.getTabs()) {
            Object object = tabInfo.getObject();
            if (object != null && object.equals(argument)) {
                cursorOutputTabs.select(tabInfo, true);
                break;
            }
        }

    }

    private void updateStatusBarLabels() {
        ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
        if (connectionHandler != null) {
            connectionLabel.setIcon(connectionHandler.getIcon());
            connectionLabel.setText(connectionHandler.getName());
        }

        durationLabel.setText(": " + executionResult.getExecutionDuration() + " ms");
    }



    private void createActionsPanel() {
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.MethodExecutionResult.Controls", false,
                new CloseExecutionResultAction(this),
                new EditMethodAction(this),
                new StartMethodExecutionAction(this),
                new PromptMethodExecutionAction(this),
                ActionUtil.SEPARATOR,
                new OpenSettingsAction());
        actionsPanel.add(actionToolbar.getComponent());
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        executionResult = null;
    }

    private void createUIComponents() {
        List<ArgumentValue> inputArgumentValues = executionResult.getExecutionInput().getArgumentValues();
        List<ArgumentValue> outputArgumentValues = executionResult.getArgumentValues();
        argumentValuesTree = new ArgumentValuesTree(this, inputArgumentValues, outputArgumentValues);
    }
}
