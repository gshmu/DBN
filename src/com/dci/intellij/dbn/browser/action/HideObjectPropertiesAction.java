package com.dci.intellij.dbn.browser.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class HideObjectPropertiesAction extends AnAction{
    public HideObjectPropertiesAction() {
        super("Hide Properties", null, Icons.ACTION_CLOSE);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        browserManager.showObjectProperties(false);
    }
}
