package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.execution.common.ui.ExecutionConsoleForm;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
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
    name = "DBNavigator.Project.ExecutionManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element>, Disposable {
    public static final String TOOL_WINDOW_ID = "DB Execution Console";
    private ExecutionConsoleForm executionConsoleForm;

    private ExecutionManager(Project project) {
        super(project);
    }

    public static ExecutionManager getInstance(Project project) {
        return project.getComponent(ExecutionManager.class);
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

    @Override
    public void projectOpened() {
        ToolWindow toolWindow = initExecutionConsole();
        toolWindow.getContentManager().removeAllContents(false);
        toolWindow.setAvailable(false, null);

    }

    private ToolWindow initExecutionConsole() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
        if (toolWindow == null) {
            toolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID, true, ToolWindowAnchor.BOTTOM, this, true);
            toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
            toolWindow.setToHideOnEmptyContent(true);
        }

        if (toolWindow.getContentManager().getContents().length == 0) {
            ExecutionConsoleForm executionConsoleForm = getExecutionConsoleForm();
            ContentFactory contentFactory = new ContentFactoryImpl();
            Content content = contentFactory.createContent(executionConsoleForm.getComponent(), null, true);
            toolWindow.getContentManager().addContent(content);
            toolWindow.setAvailable(true, null);
        }
        return toolWindow;
    }

    public void showExecutionConsole(final CompilerResult compilerResult) {
        new SimpleLaterInvocator() {
            public void execute() {
                getExecutionConsoleForm().show(compilerResult);
                showExecutionConsole();
            }
        }.start();
    }

    public void showExecutionConsole(final List<CompilerResult> compilerResults) {
        new SimpleLaterInvocator() {
            public void execute() {
                getExecutionConsoleForm().show(compilerResults);
                showExecutionConsole();
            }
        }.start();
    }

    public void focusExecutionConsole(final StatementExecutionResult executionResult) {
        new ConditionalLaterInvocator() {
            public void execute() {
                getExecutionConsoleForm().select(executionResult);
                showExecutionConsole();
            }
        }.start();

    }
    public void showExecutionConsole(final StatementExecutionResult executionResult) {
        new SimpleLaterInvocator() {
            public void execute() {
                getExecutionConsoleForm().show(executionResult);
                showExecutionConsole();
            }
        }.start();
    }

    public void showExecutionConsole(final MethodExecutionResult executionResult) {
        new SimpleLaterInvocator() {
            public void execute() {
                getExecutionConsoleForm().show(executionResult);
                showExecutionConsole();
            }
        }.start();
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
        getExecutionConsoleForm().selectResultTab(executionResult);
    }

    public ExecutionConsoleForm getExecutionConsoleForm() {
        if (executionConsoleForm == null) {
            executionConsoleForm = new ExecutionConsoleForm(getProject());
        }
        return executionConsoleForm;
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.ExecutionManager";
    }

    public void dispose() {
        if (executionConsoleForm != null) {
            executionConsoleForm.dispose();
            executionConsoleForm = null;
        }
    }

    public ExecutionResult getSelectedExecutionResult() {
        return executionConsoleForm == null ? null : executionConsoleForm.getSelectedExecutionResult();
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
    public void loadState(Element element) {
    }
}
