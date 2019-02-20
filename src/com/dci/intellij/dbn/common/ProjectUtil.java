package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import org.jetbrains.annotations.NotNull;

public class ProjectUtil {
    public static void closeProject(@NotNull Project project) {
        SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }
}
