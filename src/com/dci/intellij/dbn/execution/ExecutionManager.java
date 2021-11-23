package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.result.ui.ExecutionResultForm;
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
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.getBoolean;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.setBoolean;

@State(
    name = ExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private @Getter @Setter boolean retainStickyNames = false;

    public static final String COMPONENT_NAME = "DBNavigator.Project.ExecutionManager";
    public static final String TOOL_WINDOW_ID = "DBNavigator.ToolWindow.DatabaseExecutionConsole";

    private final Latent<ExecutionConsoleForm> executionConsoleForm =
            Latent.basic(() -> {
                ExecutionConsoleForm form = new ExecutionConsoleForm(this, getProject());
                Disposer.register(this, form);
                return form;
            });

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
        ToolWindow toolWindow = getExecutionConsoleWindow();
        toolWindow.getContentManager().removeAllContents(false);
        toolWindow.setAvailable(false, null);
    }

    public ToolWindow getExecutionConsoleWindow() {
        Project project = getProject();
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    }

    private synchronized ToolWindow initExecutionConsole() {
        ToolWindow toolWindow = getExecutionConsoleWindow();
        ContentManager contentManager = toolWindow.getContentManager();

        if (contentManager.getContents().length == 0) {
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            ContentFactory contentFactory = contentManager.getFactory();
            Content content = contentFactory.createContent(executionConsoleForm.getComponent(), null, true);
            contentManager.addContent(content);
            toolWindow.setAvailable(true, null);
        }
        return toolWindow;
    }

    @Nullable
    ExecutionResultForm getExecutionResultForm(ExecutionResult executionResult) {
        return getExecutionConsoleForm().getExecutionResultForm(executionResult);
    }

    public void addCompilerResult(@NotNull CompilerResult compilerResult) {
        Dispatch.run(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addCompilerResult(compilerResult);
        });
    }

    public void addExplainPlanResult(@NotNull ExplainPlanResult explainPlanResult) {
        Dispatch.run(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResult(explainPlanResult, NavigationInstructions.create(SELECT, FOCUS));
        });
    }

    public void writeLogOutput(@NotNull LogOutputContext context, LogOutput output) {
        Dispatch.run(() -> {
            if (!context.isClosed()) {
                showExecutionConsole();
                ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
                executionConsoleForm.displayLogOutput(context, output);
            }
        });
    }

    public void addExecutionResult(@NotNull StatementExecutionResult executionResult, NavigationInstructions instructions) {
        Dispatch.run(() -> {
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

            executionConsoleForm.addResult(executionResult, instructions);
            if (!executionResult.isBulkExecution() && !executionResult.hasCompilerResult() && !focusOnExecution()) {
                executionResult.navigateToEditor(NavigationInstructions.create(FOCUS, SCROLL, SELECT));
            }
        });
    }

    private boolean focusOnExecution() {
        Project project = getProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        return statementExecutionSettings.isFocusResult();
    }


    public void addExecutionResult(MethodExecutionResult executionResult) {
        Dispatch.run(() -> {
            showExecutionConsole();
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.addResult(executionResult);
        });
    }

    public void selectExecutionResult(StatementExecutionResult executionResult) {
        Dispatch.run(() -> {
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            executionConsoleForm.selectResult(executionResult, NavigationInstructions.create(FOCUS, SCROLL, SELECT));
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
    public ExecutionConsoleForm getExecutionConsoleForm() {
        return executionConsoleForm.get();
    }

    public void closeExecutionResults(List<ConnectionId> connectionIds){
        ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
        executionConsoleForm.closeExecutionResults(connectionIds);
    }

    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Nullable
    public ExecutionResult getSelectedExecutionResult() {
        return executionConsoleForm.loaded() ? getExecutionConsoleForm().getSelectedExecutionResult() : null;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    public Element getState() {
        Element element = new Element("state");
        setBoolean(element, "retain-sticky-names", retainStickyNames);
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        retainStickyNames = getBoolean(element, "retain-sticky-names", retainStickyNames);
    }

}
