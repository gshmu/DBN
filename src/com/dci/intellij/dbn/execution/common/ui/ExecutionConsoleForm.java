package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionResult;
import com.dci.intellij.dbn.execution.common.message.ui.ExecutionMessagesPanel;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionCursorResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import com.intellij.ui.tabs.JBTabsPosition;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;
import com.intellij.ui.tabs.impl.TabLabel;

public class ExecutionConsoleForm extends DBNFormImpl implements DBNForm {
    private JPanel mainPanel;
    //private Map<Component, ExecutionResult> executionResultsMap = new HashMap<Component, ExecutionResult>();
    private TabbedPane resultTabs;
    private ExecutionMessagesPanel executionMessagesPanel;

    private boolean canScrollToSource;
    private Project project;

    public ExecutionConsoleForm(Project project) {
        this.project = project;
        resultTabs = new TabbedPane(this);
        mainPanel.add(resultTabs, BorderLayout.CENTER);
        resultTabs.setFocusable(false);
        //resultTabs.setAdjustBorders(false);
        resultTabs.addTabMouseListener(mouseListener);
        resultTabs.addListener(tabsListener);
        resultTabs.setPopupGroup(new ExecutionConsolePopupActionGroup(this), "place", false);
        resultTabs.setTabsPosition(JBTabsPosition.bottom);
        resultTabs.setBorder(null);
        EventManager.subscribe(project, EnvironmentChangeListener.TOPIC, environmentChangeListener);
        EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public int getTabCount() {
        return resultTabs.getTabCount();
    }

    private EnvironmentChangeListener environmentChangeListener = new EnvironmentChangeListener() {
        @Override
        public void configurationChanged() {
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(project).getVisibilitySettings();
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                ExecutionResult executionResult = (ExecutionResult) tabInfo.getObject();
                ConnectionHandler connectionHandler = executionResult.getConnectionHandler();
                if (connectionHandler != null) {
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
            for (TabInfo tabInfo : resultTabs.getTabs()) {
                Object object = tabInfo.getObject();
                if (object instanceof StatementExecutionResult) {
                    StatementExecutionResult executionResult = (StatementExecutionResult) object;
                    if (executionResult.getExecutionProcessor().getPsiFile().equals(file)) {
                        Icon icon = executionResult.getExecutionProcessor().isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
                        tabInfo.setIcon(icon);
                    }
                }
            }
        }
    };


    private MouseListener mouseListener = new MouseAdapter() {
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
                    ExecutionResult executionResult = (ExecutionResult) newSelection.getObject();
                    if (executionResult != null) {
                        if (executionResult instanceof StatementExecutionResult) {
                            StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                            Icon icon = statementExecutionResult.getExecutionProcessor().isDirty() ? Icons.STMT_EXEC_RESULTSET_ORPHAN : Icons.STMT_EXEC_RESULTSET;
                            newSelection.setIcon(icon);
                            statementExecutionResult.navigateToEditor(false);
                        }
                    }

                }
            }

        }
    };

    public void removeAllExceptTab(TabInfo exceptionTabInfo) {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo != exceptionTabInfo) {
                removeTab(tabInfo);
            }
        }
    }

    public synchronized void removeTab(TabInfo tabInfo) {
        ExecutionResult executionResult = (ExecutionResult) tabInfo.getObject();
        if (executionResult == null) {
            removeMessagesTab();
        } else {
            removeResultTab(executionResult);
        }
    }

    public void removeAllTabs() {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            removeTab(tabInfo);
        }
    }

    public JComponent getComponent() {
        return resultTabs;
    }

    public void show(StatementExecutionResult executionResult) {
        ExecutionMessagesPanel messagesPane = getMessagesPanel();
        TreePath messageTreePath = null;
        if (executionResult instanceof StatementExecutionCursorResult) {
            StatementExecutionMessage executionMessage = executionResult.getExecutionMessage();
            if (executionMessage == null) {
                showResultTab(executionResult);
            } else {
                prepareMessagesTab();
                messageTreePath = messagesPane.addExecutionMessage(executionMessage, true);
            }
        } else {
            prepareMessagesTab();
            messageTreePath = messagesPane.addExecutionMessage(executionResult.getExecutionMessage(), true);
        }

        CompilerResult compilerResult = executionResult.getCompilerResult();
        if (compilerResult != null) {
            show(compilerResult);
        } else {
            StatementExecutionSettings statementExecutionSettings = ExecutionEngineSettings.getInstance(project).getStatementExecutionSettings();
            if (!statementExecutionSettings.isFocusResult()) {
                executionResult.navigateToEditor(true);
            }
        }
        if (messageTreePath != null) {
            messagesPane.expand(messageTreePath);
        }
    }

    public void show(CompilerResult compilerResult) {
        prepareMessagesTab();
        if (compilerResult.getCompilerMessages().size() > 0) {
            boolean isFirst = true;
            for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                getMessagesPanel().addCompilerMessage(compilerMessage, isFirst);
                isFirst = false;
            }
        }
    }

    public void show(MethodExecutionResult executionResult) {
        showResultTab(executionResult);
    }

    public void show(List<CompilerResult> compilerResults) {
        prepareMessagesTab();
        CompilerResult firstCompilerResult = null;
        for (CompilerResult compilerResult : compilerResults) {
            if (compilerResult.getCompilerMessages().size() > 0) {
                if (firstCompilerResult == null) {
                    firstCompilerResult = compilerResult;
                }

                for (CompilerMessage compilerMessage : compilerResult.getCompilerMessages()) {
                    getMessagesPanel().addCompilerMessage(compilerMessage, false);
                }
            }
        }
        if (firstCompilerResult != null) {
            getMessagesPanel().select(firstCompilerResult.getCompilerMessages().get(0));
        }
    }
    
    public ExecutionResult getSelectedExecutionResult() {
        TabInfo selectedInfo = resultTabs.getSelectedInfo();
        return selectedInfo == null ? null : (ExecutionResult) selectedInfo.getObject();
    }

    /*********************************************************
     *                       Messages                        *
     *********************************************************/
    private ExecutionMessagesPanel getMessagesPanel() {
        if (executionMessagesPanel == null) {
            executionMessagesPanel = new ExecutionMessagesPanel(project);
            Disposer.register(this, executionMessagesPanel);
        }
        return executionMessagesPanel;
    }

    private void prepareMessagesTab() {
        JComponent component = getMessagesPanel().getComponent();
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
        ExecutionMessagesPanel executionMessagesPanel = getMessagesPanel();
        JComponent component = executionMessagesPanel.getComponent();
        if (resultTabs.getTabCount() > 0 || resultTabs.getTabAt(0).getComponent() == component) {
            TabInfo tabInfo = resultTabs.getTabAt(0);
            resultTabs.removeTab(tabInfo);
        }

        executionMessagesPanel.reset();
        if (getTabCount() == 0) {
            ExecutionManager.getInstance(project).hideExecutionConsole();
        }
    }

    /*********************************************************
     *                  Statement executions                 *
     *********************************************************/
    public void showResultTab(ExecutionResult executionResult) {
        if (containsResultTab(executionResult)) {
            selectResultTab(executionResult);
        } else {
            addResultTab(executionResult);
        }
    }

    public void addResultTab(ExecutionResult executionResult) {
        JComponent component = executionResult.getResultPanel().getComponent();
        TabInfo tabInfo = new TabInfo(component);
        tabInfo.setObject(executionResult);
        EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(project).getVisibilitySettings();
        if (visibilitySettings.getExecutionResultTabs().value()){
            tabInfo.setTabColor(executionResult.getConnectionHandler().getEnvironmentType().getColor());
        } else {
            tabInfo.setTabColor(null);
        }
        tabInfo.setText(executionResult.getResultName());
        tabInfo.setIcon(executionResult.getResultIcon());
        resultTabs.addTab(tabInfo);
        selectResultTab(tabInfo);
    }

    public boolean containsResultTab(ExecutionResult executionProcessor) {
        Component component = executionProcessor.getResultPanel().getComponent();
        return containsResultTab(component);
    }

    public void removeResultTab(ExecutionResult executionResult) {
        try {
            canScrollToSource = false;
            ExecutionResultForm resultComponent = executionResult.getResultPanel();
            TabInfo tabInfo = resultTabs.findInfo(resultComponent.getComponent());
            if (resultTabs.getTabs().contains(tabInfo)) {
                resultTabs.removeTab(tabInfo);
                if (executionResult instanceof StatementExecutionResult) {
                    StatementExecutionResult statementExecutionResult = (StatementExecutionResult) executionResult;
                    StatementExecutionInput executionInput = statementExecutionResult.getExecutionInput();
                    if (executionInput != null && !executionInput.isDisposed()) {
                        DBLanguagePsiFile file = executionInput.getExecutablePsiElement().getFile();
                        DocumentUtil.refreshEditorAnnotations(file);
                    }
                }
                Disposer.dispose(resultComponent);
            }
            if (getTabCount() == 0) {
                ExecutionManager.getInstance(project).hideExecutionConsole();
            }

        } finally {
            canScrollToSource = true;
        }
    }

    public void selectResultTab(ExecutionResult executionResult) {
        executionResult.getResultPanel().setExecutionResult(executionResult);
        JComponent component = executionResult.getResultPanel().getComponent();
        TabInfo tabInfo = resultTabs.findInfo(component);
        if (tabInfo != null) {
            tabInfo.setText(executionResult.getResultName());
            tabInfo.setIcon(executionResult.getResultIcon());
            selectResultTab(tabInfo);
        }
    }

    /*********************************************************
     *                      Miscellaneous                    *
     *********************************************************/

    private boolean containsResultTab(Component component) {
        for (TabInfo tabInfo : resultTabs.getTabs()) {
            if (tabInfo.getComponent() == component) {
                return true;
            }
        }
        return false;
    }

    private void selectResultTab(TabInfo tabInfo) {
        try {
            canScrollToSource = false;
            resultTabs.select(tabInfo, true);
        } finally {
            canScrollToSource = true;
        }
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ExecutionConsolePanel";
    }

    public void dispose() {
        EventManager.unsubscribe(environmentChangeListener);
        super.dispose();
        project = null;
    }
}
