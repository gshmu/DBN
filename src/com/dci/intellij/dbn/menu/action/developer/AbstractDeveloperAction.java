package com.dci.intellij.dbn.menu.action.developer;

import com.dci.intellij.dbn.environment.Environment;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDeveloperAction extends ToggleAction implements DumbAware {
    @Override
    public final boolean isSelected(@NotNull AnActionEvent e) {
        return getState();
    }

    @Override
    public final void setSelected(@NotNull AnActionEvent e, boolean state) {
        setState(state);
    }

    @Override
    public final void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setVisible(Environment.DEVELOPER_MODE);
    }

    protected abstract boolean getState();
    protected abstract void setState(boolean value);

}
