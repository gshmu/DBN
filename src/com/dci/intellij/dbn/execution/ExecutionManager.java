package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanResult;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentFactoryImpl;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@State(
    name = ExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ExecutionManager";

    public static final String TOOL_WINDOW_ID = "DB Execution Console";
    private Latent<ExecutionConsoleForm> executionConsoleForm = Latent.disposable(this, () -> new ExecutionConsoleForm(getProject()));

    private ExecutionManager(Project project) {
        super(project);
    }

    public static ExecutionManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, ExecutionManager.class);
    }

    private void showExecutionConsole() {
        ToolWindow toolWindow = initExecutionConsole();
        toolWindow.show(null);
    }

    public void hideExecutionConsole() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow != null) {
            toolWindow.getContentManager().removeAllContents(false);
            toolWindow.setAvailable(false, null);
        }
    }

    private ToolWindow initExecutionConsole() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            synchronized (this) {
                toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
                if (toolWindow == null) {
                    toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM, getProject(), true);
                    toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
                    toolWindow.setToHideOnEmptyContent(true);
                }
            }
        }

        if (toolWindow.getContentManager().getContents().length == 0) {
            synchronized (this) {
                if (toolWindow.getContentManager().getContents().length == 0) {
                    ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                    ContentFactory contentFactory = new ContentFactoryImpl();
                    Content content = contentFactory.createContent(executionConsoleForm.getComponent(), null, true);
                    toolWindow.getContentManager().addContent(content);
                    toolWindow.setAvailable(true, null);
                }
            }
        }
        return toolWindow;
    }

    public void addExecutionResult(final CompilerResult compilerResult) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResult(compilerResult);
        });
    }

    public void addExecutionResults(final List<CompilerResult> compilerResults) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResults(compilerResults);
        });
    }

    public void addExplainPlanResult(final ExplainPlanResult explainPlanResult) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResult(explainPlanResult);
        });
    }

    public void writeLogOutput(@NotNull final LogOutputContext context, final LogOutput output) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            if (!context.isClosed()) {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                executionConsoleForm.displayLogOutput(context, output);
            }
        });
    }

    public void addExecutionResult(@NotNull final StatementExecutionResult executionResult) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            if (executionResult.isLoggingActive()) {
                LogOutputContext context = new LogOutputContext(executionResult.getConnectionHandler());
                context.setHideEmptyLines(false);
                String loggingOutput = executionResult.getLoggingOutput();

                executionConsoleForm.displayLogOutput(
                        context, LogOutput.createSysOutput(context,
                                executionResult.getExecutionContext().getExecutionTimestamp(),
                                " - Statement execution started", false));

                if (StringUtil.isNotEmptyOrSpaces(loggingOutput)) {
                    executionConsoleForm.displayLogOutput(context,
                            LogOutput.createStdOutput(loggingOutput));
                }

                executionConsoleForm.displayLogOutput(context,
                        LogOutput.createSysOutput(context, " - Statement execution finished\n", false));
            }

            executionConsoleForm.addResult(executionResult);
            if (!executionResult.isBulkExecution() && !executionResult.hasCompilerResult() && !focusOnExecution()) {
                executionResult.navigateToEditor(NavigationInstruction.FOCUS);
            }
        });
    }

    private boolean focusOnExecution() {
        Project project = getProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }


    public void addExecutionResult(final MethodExecutionResult executionResult) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResult(executionResult);
        });
    }

    public void selectExecutionResult(final StatementExecutionResult executionResult) {
        SimpleLaterInvocator.invokeNonModal(() -> {
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.selectResult(executionResult);
            showExecutionConsole();
        });

    }

    public void removeMessagesTab() {
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        executionConsoleForm.removeMessagesTab();
    }

    public void removeResultTab(ExecutionResult executionResult) {
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        executionConsoleForm.removeResultTab(executionResult);
    }

    public void selectResultTab(ExecutionResult executionResult) {
        showExecutionConsole();
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        executionConsoleForm.selectResultTab(executionResult);
    }

    @NotNull
    private ExecutionConsoleForm getExecutionConsoleForm() {
        return executionConsoleForm.get();
    }

    public void closeExecutionResults(List<ConnectionId> connectionIds){
        getExecutionConsoleForm().closeExecutionResults(connectionIds);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @Override
    public void projectOpened() {
        ToolWindow toolWindow = initExecutionConsole();
        toolWindow.getContentManager().removeAllContents(false);
        toolWindow.setAvailable(false, null);
    }

    @Override
    public void projectClosed() {
    }


    @Override
    public void projectClosing(@NotNull Project project) {
        if (executionConsoleForm.loaded()) {
            getExecutionConsoleForm().removeAllTabs();
        }
        super.projectClosing(project);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Nullable
    public ExecutionResult getSelectedExecutionResult() {
        return executionConsoleForm.loaded() ? getExecutionConsoleForm().getSelectedExecutionResult() : null;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {
    }
}
