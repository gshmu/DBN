package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AutoscrollToEditorAction extends ToggleAction implements DumbAware{

    public AutoscrollToEditorAction() {
        super("Autoscroll to editor"/*, "", Icons.BROWSER_AUTOSCROLL_TO_EDITOR*/);
    }


    public boolean isSelected(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        return browserManager.getAutoscrollToEditor().value();
    }

    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        Project project = ActionUtil.ensureProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        browserManager.getAutoscrollToEditor().setValue(state);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setText("Autoscroll to Editor");
    }

}
