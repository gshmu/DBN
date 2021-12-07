package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.listener.MouseClickedListener;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
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
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.TabLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;

public class ExecutionConsoleForm extends DBNFormImpl{
    private JPanel mainPanel;
    private TabbedPane resultTabs;
    private final Latent<ExecutionMessagesPanel> executionMessagesPanel = Latent.basic(() -> new ExecutionMessagesPanel(this));

    private boolean canScrollToSource;

    public ExecutionConsoleForm(Disposable parent, Project project) {
        super(parent, project);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        ProjectEvents.subscribe(project, this, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
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

    private final EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged(Project project) {
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(getProject()).getVisibilitySettings();
            TabbedPane resultTabs = getResultTabs();
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
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

    private final PsiDocumentTransactionListener psiDocumentTransactionListener = new PsiDocumentTransactionListener() {

        @Override
        public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {

        }

        @Override
        public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
            try {
                TabbedPane resultTabs = getResultTabs();
                for (TabInfo tabInfo : resultTabs.getTabs()) {
                    ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        StatementExecutionProcessor executionProcessor = statementExecutionResult.getExecutionProcessor();
                        if (Failsafe.check(executionProcessor) && file.equals(executionProcessor.getPsiFile())) {
                            Icon icon = executionProcessor.isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
                            tabInfo.setIcon(icon);
                        }
                    }

                    if (executionMessagesPanel.loaded()) {
                        JComponent messagePanelComponent = getMessagesPanel().getComponent();
                        GUIUtil.repaint(messagePanelComponent);
                    }
                }
            } catch (ProcessCanceledException ignore) {}
        }
    };


    private final MouseListener mouseListener = MouseClickedListener.create(e -> {
        if (e.isShiftDown() && (16 & e.getModifiers()) > 0 || ((8 & e.getModifiers()) > 0)) {
            if (e.getSource() instanceof TabLabel) {
                TabLabel tabLabel = (TabLabel) e.getSource();
                removeTab(tabLabel.getInfo());
            }
        }
    });

    private final TabsListener tabsListener = new TabsListener() {
        @Override
        public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
            if (canScrollToSource) {
                if (newSelection != null) {
                    ExecutionResult<?> executionResult = getExecutionResult(newSelection);
                    if (executionResult instanceof StatementExecutionResult) {
                        StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                        statementExecutionResult.navigateToEditor(NavigationInstructions.create(FOCUS, SCROLL));
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
        ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
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
    public JComponent getMainComponent() {
        return getResultTabs();
    }

    public void selectResult(StatementExecutionResult executionResult, NavigationInstructions instructions) {
        StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
        if (executionMessage != null) {
            prepareMessagesTab(instructions);
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            messagesPane.selectMessage(executionMessage, instructions);
        }
    }

    public void addResult(ExplainPlanResult explainPlanResult, NavigationInstructions instructions) {
        if (explainPlanResult.isError()) {
            prepareMessagesTab(instructions.with(RESET));
            ExecutionMessagesPanel messagesPane = getMessagesPanel();
            ExplainPlanMessage explainPlanMessage = new ExplainPlanMessage(explainPlanResult, MessageType.ERROR);
            messagesPane.addExplainPlanMessage(explainPlanMessage, instructions);
        } else {
            showResultTab(explainPlanResult);
        }
    }

    public void addResult(StatementExecutionResult executionResult, NavigationInstructions instructions) {
        ExecutionMessagesPanel messagesPane = getMessagesPanel();
        TreePath messageTreePath = null;
        CompilerResult compilerResult = executionResult.getCompilerResult();
        //boolean hasCompilerResult = compilerResult != null;
        //boolean selectMessage = !executionResult.getExecutionProcessor().getExecutionInput().isBulkExecution() && !hasCompilerResult;
        //boolean focusMessage = selectMessage && focusOnExecution();
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
            if (executionMessage == null) {
                showResultTab(executionResult);
            } else {
                prepareMessagesTab(instructions.with(RESET));
                messageTreePath = messagesPane.addExecutionMessage(executionMessage, instructions);
            }
        } else {
            prepareMessagesTab(instructions.with(RESET));
            messageTreePath = messagesPane.addExecutionMessage(executionResult.getExecutionMessage(), instructions);
        }

        if (compilerResult != null) {
            addCompilerResult(compilerResult);
        }
        if (messageTreePath != null) {
            messagesPane.expand(messageTreePath);
        }
    }

    private boolean focusOnExecution() {
        Project project = ensureProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }

    public void addCompilerResult(@NotNull CompilerResult compilerResult) {
        boolean bulk = compilerResult.getCompilerAction().isBulkCompile();
        boolean error = compilerResult.isError();
        boolean single = compilerResult.isSingleMessage();

        prepareMessagesTab(NavigationInstructions.create(RESET));

        CompilerMessage firstMessage = null;
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();

        for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
            if (firstMessage == null) {
                firstMessage = compilerMessage;
            }
            messagesPanel.addCompilerMessage(compilerMessage,
                    NavigationInstructions.create().
                    with(FOCUS, !bulk && error && single).
                    with(SCROLL, single));
        }

        if (firstMessage != null && firstMessage.isError() && !bulk) {
            messagesPanel.selectMessage(firstMessage, NavigationInstructions.create(SCROLL, SELECT, OPEN));
        }
    }

    public void addResult(MethodExecutionResult executionResult) {
        showResultTab(executionResult);
    }

    public ExecutionResult<?> getSelectedExecutionResult() {
        TabInfo selectedInfo = getResultTabs().getSelectedInfo();
        return selectedInfo == null ? null : getExecutionResult(selectedInfo);
    }

    @Nullable
    private static ExecutionResult<?> getExecutionResult(TabInfo tabInfo) {
        ExecutionResultForm<?> executionResultForm = (ExecutionResultForm<?>) tabInfo.getObject();
        return executionResultForm == null ? null : executionResultForm.getExecutionResult();
    }

    /*********************************************************
     *                       Messages                        *
     *********************************************************/
    private ExecutionMessagesPanel getMessagesPanel() {
        return executionMessagesPanel.get();
    }

    private void prepareMessagesTab(NavigationInstructions instructions) {
        TabbedPane resultTabs = getResultTabs();
        ExecutionMessagesPanel messagesPanel = getMessagesPanel();
        if (instructions.isReset()) {
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
        resultTabs.select(tabInfo, instructions.isFocus());
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
        Project project = ensureProject();
        return ExecutionManager.getInstance(project);
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
        boolean emptyOutput = Strings.isEmptyOrSpaces(output.getText());
        VirtualFile sourceFile = context.getSourceFile();
        ConnectionHandler connectionHandler = context.getConnectionHandler();
        boolean selectTab = sourceFile != null;
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
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
        ExecutionResultForm<?> resultForm = ensureResultForm(logOutput);
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
     *           Statement / method executions               *
     *********************************************************/
    private void showResultTab(ExecutionResult<?> executionResult) {
        if (executionResult instanceof ExplainPlanResult) {
            addResultTab(executionResult);
        } else {
            ExecutionResult<?> previousExecutionResult = executionResult.getPrevious();

            ExecutionResultForm executionResultForm;
            if (previousExecutionResult == null) {
                executionResultForm = getExecutionResultForm(executionResult);
                if (executionResultForm != null) {
                    selectResultTab(executionResult);
                }
            } else {
                executionResultForm = getExecutionResultForm(previousExecutionResult);
                if (executionResultForm != null) {
                    executionResultForm.setExecutionResult(executionResult);
                }
            }

            if (executionResultForm == null) {
                addResultTab(executionResult);
            }
        }
    }

    private void addResultTab(ExecutionResult<?> executionResult) {
        ExecutionResultForm<?> resultForm = ensureResultForm(executionResult);
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

    public void removeResultTab(ExecutionResult<?> executionResult) {
        try {
            TabbedPane resultTabs = getResultTabs();
            canScrollToSource = false;
            ExecutionResultForm<?> resultForm = getExecutionResultForm(executionResult);
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
                        Documents.refreshEditorAnnotations(file);
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

    public <T extends ExecutionResult<?>> void selectResultTab(T executionResult) {
        TabbedPane resultTabs = getResultTabs();
        ExecutionResultForm<?> resultForm = getExecutionResultForm(executionResult);

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

    public void closeExecutionResults(List<ConnectionId> connectionIds) {
        List<TabInfo> tabs = getExecutionResultTabs();
        for (TabInfo tabInfo : tabs) {
            ExecutionResult<?> executionResult = getExecutionResult(tabInfo);
            if (executionResult != null && connectionIds.contains(executionResult.getConnectionId())) {
                removeTab(tabInfo);
            }
        }
    }

    @NotNull
    private List<TabInfo> getExecutionResultTabs() {
        return Failsafe.check(resultTabs) ? new ArrayList<>(resultTabs.getTabs()) : Collections.emptyList();
    }

    @Nullable
    private ExecutionResultForm<?> ensureResultForm(ExecutionResult<?> executionResult) {
        ExecutionResultForm<?> executionResultForm = getExecutionResultForm(executionResult);
        if (executionResultForm == null) {
            synchronized (this) {
                executionResultForm = getExecutionResultForm(executionResult);
                if (executionResultForm == null) {
                    executionResultForm = executionResult.createForm();
                }
            }
        }
        return executionResultForm;
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

    @Nullable
    public ExecutionResultForm<?> getExecutionResultForm(ExecutionResult<?> executionResult) {
        List<TabInfo> tabs = getExecutionResultTabs();
        for (TabInfo tabInfo : tabs) {
            ExecutionResultForm<?> executionResultForm = (ExecutionResultForm<?>) tabInfo.getObject();
            if (executionResultForm != null && executionResultForm.getExecutionResult() == executionResult) {
                return executionResultForm;
            }
        }
        return null;
    }
}
