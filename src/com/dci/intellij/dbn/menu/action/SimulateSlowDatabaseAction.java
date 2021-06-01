package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.environment.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public class SimulateSlowDatabaseAction extends ToggleAction implements DumbAware {
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return Environment.isSlowDatabaseModeEnabled();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Environment.setSlowDatabaseModeEnabled(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(Environment.DEVELOPER_MODE);
    }
}
