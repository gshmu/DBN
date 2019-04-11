package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.execution.common.message.ui.ExecutionMessagesPanel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingResult;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class ExecutionConsoleForm extends DBNFormImpl{
    private JPanel mainPanel;
    //private Map<Component, ExecutionResult> executionResultsMap = new HashMap<Component, ExecutionResult>();
    private TabbedPane resultTabs;
    private Latent<ExecutionMessagesPanel> executionMessagesPanel = Latent.disposable(this, () -> new ExecutionMessagesPanel(ExecutionConsoleForm.this));

    private boolean canScrollToSource;

    public ExecutionConsoleForm(Project project) {
        super(project);
        EventUtil.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        EventUtil.subscribe(project, this, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    private TabbedPane getResultTabs() {
        if (!isDisposed() && !Failsafe.check(resultTabs)) {
            resultTabs = new TabbedPane(this);
            mainPanel.removeAll();
            mainPanel.add(resultTabs, BorderLayout.CENTER);
            resultTabs.setFocusable(false);
            //resultTabs.setAdjustBorders(false);
            resultTabs.addTabMouseListener(mouseListener);
            resultTabs.addListener(tabsListener);
            resultTabs.setPopupGroup(new ExecutionConsolePopupActionGroup(this), "place", false);
            resultTabs.setTabsPosition(JBTabsPosition.bottom);
            resultTabs.setBorder(null);
            Disposer.register(this, resultTabs);
            return resultTabs;
        }
        return Failsafe.nn(resultTabs);
    }

    private int getTabCount() {
        return getResultTabs().getTabCount();
    }

    private EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged() {
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            TabbedPane resultTabs = getResultTabs();
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult executionResult = getExecutionResult(tabInfo);
                if (executionResult != null) {
                    ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
                    EnvironmentType environmentType = connectionHandler.getEnvironmentType();
                    if (visibilitySettings.getExecutionResultTabs().value()){
                        tabInfo.setTabColor(environmentType.getColor());
                    } else {
                        tabInfo.setTabColor(null);
                    }
                }
            }
        }
    };

    private PsiDocumentTransactionListener psiDocumentTransactionListener = new PsiDocumentTransactionListener() {

        @Override
        public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {

        }

        @Override
        public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
            TabbedPane resultTabs = getResultTabs();
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult executionResult = getExecutionResult(tabInfo);
                if (executionResult instanceof StatementExecutionResult) {
                    StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                    StatementExecutionProcessor executionProcessor = statementExecutionResult.getExecutionProcessor();
                    if (Failsafe.check(executionProcessor) && executionProcessor.getPsiFile().equals(file)) {
                        Icon icon = executionProcessor.isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
                        tabInfo.setIcon(icon);
                    }
                }

                if (executionMessagesPanel.loaded()) {
                    JComponent messagePanelComponent = getMessagesPanel().getComponent();
                    GUIUtil.repaint(messagePanelComponent);
                }
            }
        }
    };


    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.isShiftDown() && (16 & e.getModifiers()) > 0 || ((8 & e.getModifiers()) > 0)) {
                if (e.getSource() instanceof TabLabel) {
                    TabLabel tabLabel = (TabLabel) e.getSource();
                    removeTab(tabLabel.getInfo());
                }
            }
        }
    };

    private TabsListener tabsListener = new TabsListener.Adapter() {
        @Override
        public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
            if (canScrollToSource) {
                if (newSelection != null) {
                    ExecutionResult executionResult = getExecutionResult(newSelection);
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        statementExecutionResult.navigateToEditor(NavigationInstruction.SCROLL);
                    }
                }
            }
            if (oldSelection != null && newSelection != null && getExecutionResult(newSelection) instanceof DatabaseLoggingResult) {
                newSelection.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE);
            }

        }
    };

    public void removeAllExceptTab(TabInfo exceptionTabInfo) {
        for (TabInfo tabInfo : getResultTabs().getTabs()) {
            if (tabInfo != exceptionTabInfo) {
                removeTab(tabInfo);
            }
        }
    }

    public synchronized void removeTab(TabInfo tabInfo) {
        ExecutionResult executionResult = getExecutionResult(tabInfo);
        if (executionResult == null) {
            removeMessagesTab();
        } else {
            removeResultTab(executionResult);
        }
    }

    public void removeAllTabs() {
        for (TabInfo tabInfo : getResultTabs().getTabs()) {
            removeTab(tabInfo);
        }
    }

    @NotNull
    @Override
    public JComponent ensureComponent() {
        return getResultTabs();
    }

    public void selectResult(StatementExecutionResult executionResult) {
        StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
        if (executionMessage != null) {
            prepareMessagesTab(false);
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            messagesPane.selectMessage(executionMessage, true);
        }
    }

    public void addResult(ExplainPlanResult explainPlanResult) {
        if (explainPlanResult.isError()) {
            prepareMessagesTab(true);
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            ExplainPlanMessage explainPlanMessage = new ExplainPlanMessage(explainPlanResult, MessageType.ERROR);
            messagesPane.addExplainPlanMessage(explainPlanMessage, true);
        } else {
            showResultTab(explainPlanResult);
        }
    }

    public void addResult(StatementExecutionResult executionResult) {
        ExecutionMessagesPanel messagesPane = getMessagesPanel();
        TreePath messageTreePath = null;
        CompilerResult compilerResult = executionResult.getCompilerResult();
        boolean hasCompilerResult = compilerResult != null;
        boolean selectMessage = !executionResult.getExecutionProcessor().getExecutionInput().isBulkExecution() && !hasCompilerResult;
        boolean focusMessage = selectMessage && focusOnExecution();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
            if (executionMessage == null) {
                showResultTab(executionResult);
            } else {
                prepareMessagesTab(true);
                messageTreePath = messagesPane.addExecutionMessage(executionMessage, selectMessage, focusMessage);
            }
        } else {
            prepareMessagesTab(true);
            messageTreePath = messagesPane.addExecutionMessage(executionResult.getExecutionMessage(), selectMessage, focusMessage);
        }

        if (compilerResult != null) {
            addResult(compilerResult);
        }
        if (messageTreePath != null) {
            messagesPane.expand(messageTreePath);
        }
    }

    private boolean focusOnExecution() {
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(getProject());
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }

    public void addResult(CompilerResult compilerResult) {
        prepareMessagesTab(true);
        CompilerMessage firstMessage = null;
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        if (compilerResult.getCompilerMessages().size() > 0) {
            for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                if (firstMessage == null) {
                    firstMessage = compilerMessage;
                }
                messagesPanel.addCompilerMessage(compilerMessage, false);
            }
        }

        if (firstMessage != null && firstMessage.isError()) {
            messagesPanel.selectMessage(firstMessage);
        }
    }

    public void addResult(MethodExecutionResult executionResult) {
        showResultTab(executionResult);
    }

    public void addResults(List<CompilerResult> compilerResults) {
        prepareMessagesTab(true);
        CompilerMessage firstMessage = null;
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        for (CompilerResult compilerResult : compilerResults) {
            if (compilerResult.getCompilerMessages().size() > 0) {
                for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                    if (firstMessage == null) {
                        firstMessage = compilerMessage;
                    }
                    messagesPanel.addCompilerMessage(compilerMessage, false);
                }
            }
        }
        if (firstMessage != null && firstMessage.isError()) {
            messagesPanel.selectMessage(firstMessage);
        }
    }
    
    public ExecutionResult getSelectedExecutionResult() {
        TabInfo selectedInfo = getResultTabs().getSelectedInfo();
        return selectedInfo == null ? null : getExecutionResult(selectedInfo);
    }

    @Nullable
    private static ExecutionResult getExecutionResult(TabInfo tabInfo) {
        ExecutionResultForm executionResultForm = (ExecutionResultForm) tabInfo.getObject();
        return executionResultForm == null ? null : executionResultForm.getExecutionResult();
    }

    /*********************************************************
     *                       Messages                        *
     *********************************************************/
    private ExecutionMessagesPanel getMessagesPanel() {
        return executionMessagesPanel.get();
    }

    private void prepareMessagesTab(boolean resetMessages) {
        TabbedPane resultTabs = getResultTabs();
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        if (resetMessages) {
            messagesPanel.resetMessagesStatus();
        }
        JComponent component = messagesPanel.getComponent();
        if (resultTabs.getTabCount() == 0 || resultTabs.getTabAt(0).getComponent() != component) {
            TabInfo tabInfo = new TabInfo(component);

            tabInfo.setText("Messages");
            tabInfo.setIcon(Icons.EXEC_RESULT_MESSAGES);
            resultTabs.addTab(tabInfo, 0);
        }

        TabInfo tabInfo = resultTabs.getTabAt(0);
        resultTabs.select(tabInfo, true);
    }


    public void removeMessagesTab() {
        TabbedPane resultTabs = getResultTabs();
        ExecutionMessagesPanel executionMessagesPanel = getMessagesPanel();
        JComponent component = executionMessagesPanel.getComponent();
        if (resultTabs.getTabCount() > 0 || resultTabs.getTabAt(0).getComponent() == component) {
            TabInfo tabInfo = resultTabs.getTabAt(0);
            resultTabs.removeTab(tabInfo);
        }

        executionMessagesPanel.reset();
        if (getTabCount() == 0) {
            getExecutionManager().hideExecutionConsole();
        }
    }

    @NotNull
    public ExecutionManager getExecutionManager() {
        return ExecutionManager.getInstance(getProject());
    }

    private boolean isMessagesTabVisible() {
        TabbedPane resultTabs = getResultTabs();
        if (resultTabs.getTabCount() > 0) {
            JComponent messagesPanelComponent = getMessagesPanel().getComponent();
            TabInfo tabInfo = resultTabs.getTabAt(0);
            return tabInfo.getComponent() == messagesPanelComponent;
        }
        return false;
    }

    /*********************************************************
     *                       Logging                         *
     *********************************************************/
    public void displayLogOutput(LogOutputContext context, LogOutput output) {
        TabbedPane resultTabs = getResultTabs();
        boolean emptyOutput = StringUtil.isEmptyOrSpaces(output.getText());
        VirtualFile sourceFile = context.getSourceFile();
        ConnectionHandler connectionHandler = context.getConnectionHandler();
        boolean selectTab = sourceFile != null;
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            ExecutionResult executionResult = getExecutionResult(tabInfo);
            if (executionResult instanceof DatabaseLoggingResult) {
                DatabaseLoggingResult logOutput = (DatabaseLoggingResult) executionResult;
                if (logOutput.matches(context)) {
                    logOutput.write(context, output);
                    if (!emptyOutput && !selectTab) {
                        tabInfo.setIcon(Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);
                    }
                    if (selectTab) {
                        resultTabs.select(tabInfo, true);
                    }
                    return;
                }
            }
        }
        boolean messagesTabVisible = isMessagesTabVisible();

        DatabaseLoggingResult logOutput = new DatabaseLoggingResult(context);
        ExecutionManager executionManager = getExecutionManager();
        ExecutionResultForm resultForm = executionManager.ensureResultForm(logOutput);
        if (Failsafe.check(resultForm)) {
            JComponent component = resultForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setObject(resultForm);
            tabInfo.setText(logOutput.getName());
            tabInfo.setIcon(emptyOutput || selectTab ?
                    Icons.EXEC_LOG_OUTPUT_CONSOLE :
                    Icons.EXEC_LOG_OUTPUT_CONSOLE_UNREAD);
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            if (visibilitySettings.getExecutionResultTabs().value()){
                tabInfo.setTabColor(connectionHandler.getEnvironmentType().getColor());
            } else {
                tabInfo.setTabColor(null);
            }

            resultTabs.addTab(tabInfo, messagesTabVisible ? 1 : 0);
            if (selectTab) {
                resultTabs.select(tabInfo, true);
            }
            logOutput.write(context, output);
        }
    }

    /*********************************************************
     *                  Statement executions                 *
     *********************************************************/
    public void showResultTab(ExecutionResult executionResult) {
        if (executionResult instanceof ExplainPlanResult) {
            addResultTab(executionResult);
        } else {
            if (containsResultTab(executionResult)) {
                selectResultTab(executionResult);
            } else {
                addResultTab(executionResult);
            }
        }
    }

    private void addResultTab(ExecutionResult executionResult) {
        ExecutionManager executionManager = getExecutionManager();
        ExecutionResultForm resultForm = executionManager.ensureResultForm(executionResult);
        if (Failsafe.check(resultForm)) {
            JComponent component = resultForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setObject(resultForm);
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            if (visibilitySettings.getExecutionResultTabs().value()){
                tabInfo.setTabColor(executionResult.getConnectionHandler().getEnvironmentType().getColor());
            } else {
                tabInfo.setTabColor(null);
            }
            tabInfo.setText(executionResult.getName());
            tabInfo.setIcon(executionResult.getIcon());
            getResultTabs().addTab(tabInfo);
            selectResultTab(tabInfo);
        }
    }

    private boolean containsResultTab(ExecutionResult executionResult) {
        ExecutionResultForm resultForm = getExecutionResultForm(executionResult);
        if (resultForm != null) {
            Component component = resultForm.getComponent();
            return containsResultTab(component);
        }
        return false;
    }

    public void removeResultTab(ExecutionResult executionResult) {
        try {
            TabbedPane resultTabs = getResultTabs();
            canScrollToSource = false;
            ExecutionResultForm resultForm = getExecutionResultForm(executionResult);
            if (resultForm != null) {
                try {
                    TabInfo tabInfo = resultTabs.findInfo(resultForm.getComponent());
                    if (resultTabs.getTabs().contains(tabInfo)) {
                        DBLanguagePsiFile file = null;
                        if (executionResult instanceof StatementExecutionResult) {
                            StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                            StatementExecutionInput executionInput = statementExecutionResult.getExecutionInput();
                            file = executionInput.getExecutionProcessor().getPsiFile();
                        }

                        resultTabs.removeTab(tabInfo);
                        DocumentUtil.refreshEditorAnnotations(file);
                    }
                } finally {
                    if (getTabCount() == 0) {
                        getExecutionManager().hideExecutionConsole();
                    }
                }
            }
        } finally {
            canScrollToSource = true;
        }
    }

    public <T extends ExecutionResult> void selectResultTab(T executionResult) {
        TabbedPane resultTabs = getResultTabs();
        ExecutionResultForm resultForm = getExecutionResultForm(executionResult);

        if (resultForm != null) {
            JComponent component = resultForm.getComponent();
            TabInfo tabInfo = resultTabs.findInfo(component);
            if (tabInfo != null) {
                tabInfo.setText(executionResult.getName());
                tabInfo.setIcon(executionResult.getIcon());
                selectResultTab(tabInfo);
            }
        }
    }

    private ExecutionResultForm getExecutionResultForm(ExecutionResult executionResult) {
        ExecutionManager executionManager = getExecutionManager();
        return executionManager.getResultForm(executionResult);
    }

    public void closeExecutionResults(List<ConnectionId> connectionIds) {
        TabbedPane resultTabs = getResultTabs();
        List<TabInfo> tabs = new ArrayList<>(resultTabs.getTabs());
        for (TabInfo tabInfo : tabs) {
            ExecutionResult executionResult = getExecutionResult(tabInfo);
            if (executionResult != null && connectionIds.contains(executionResult.getConnectionId())) {
                removeTab(tabInfo);
            }
        }
    }


    /*********************************************************
     *                      Miscellaneous                    *
     *********************************************************/

    private boolean containsResultTab(Component component) {
        TabbedPane resultTabs = getResultTabs();
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo.getComponent() == component) {
                return true;
            }
        }
        return false;
    }

    private void selectResultTab(TabInfo tabInfo) {
        TabbedPane resultTabs = getResultTabs();
        try {
            canScrollToSource = false;
            resultTabs.select(tabInfo, true);
        } finally {
            canScrollToSource = true;
        }
    }
}
