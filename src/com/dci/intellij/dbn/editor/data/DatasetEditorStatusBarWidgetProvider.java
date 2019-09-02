package com.dci.intellij.dbn.editor.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatasetEditorStatusBarWidgetProvider implements StatusBarWidgetProvider {
  @Nullable
  @Override
  public StatusBarWidget getWidget(@NotNull Project project) {
    return DatasetEditorStatusBarWidget.getInstance(project);
  }

  @NotNull
  @Override
  public String getAnchor() {
    return StatusBar.Anchors.after(StatusBar.StandardWidgets.ENCODING_PANEL);
  }

}
