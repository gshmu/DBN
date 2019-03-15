package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.Icons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class ExecutionConsoleToolWindowFactory implements ToolWindowFactory, DumbAware{
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
/*
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        ExecutionConsoleForm executionConsoleForm = executionManager.getExecutionConsoleForm();

        ContentFactory contentFactory = new ContentFactoryImpl();
        Content content = contentFactory.createContent(executionConsoleForm.getComponent(), null, true);
        toolWindow.getContentManager().addContent(content);
*/
        toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
        toolWindow.setToHideOnEmptyContent(true);
        toolWindow.setAvailable(false, null);
    }
}
