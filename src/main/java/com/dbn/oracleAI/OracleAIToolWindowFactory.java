package com.dbn.oracleAI;

import com.dbn.common.icon.Icons;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class OracleAIToolWindowFactory implements ToolWindowFactory, DumbAware {
  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setTitle("Oracle Companion");
    toolWindow.setStripeTitle("Oracle Companion");
    toolWindow.setIcon(Icons.WINDOW_EXECUTION_CONSOLE);
    toolWindow.setToHideOnEmptyContent(false);
    toolWindow.setAutoHide(false);
  }


}
