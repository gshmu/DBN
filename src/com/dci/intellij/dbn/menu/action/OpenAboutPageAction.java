package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.about.ui.AboutComponent;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class OpenAboutPageAction extends DumbAwareAction {

    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        AboutComponent aboutComponent = new AboutComponent(project);
        aboutComponent.showPopup(project);
    }
}
