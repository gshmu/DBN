package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public class SimulateSlowDatabaseAction extends ToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        return DatabaseNavigator.getInstance().isSlowDatabaseModeEnabled();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        DatabaseNavigator.getInstance().setSlowDatabaseModeEnabled(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(DatabaseNavigator.DEVELOPER);
    }
}
