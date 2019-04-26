package com.dci.intellij.dbn.menu.action;

import com.dci.intellij.dbn.common.about.ui.AboutComponent;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class AboutPageOpenAction extends DumbAwareProjectAction {

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        AboutComponent aboutComponent = new AboutComponent(project);
        aboutComponent.showPopup(project);
    }
}
