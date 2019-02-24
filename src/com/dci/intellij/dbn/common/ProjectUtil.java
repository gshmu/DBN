package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import org.jetbrains.annotations.NotNull;

public class ProjectUtil {
    public static void closeProject(@NotNull Project project) {
        Dispatch.invokeNonModal(() -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }
}
