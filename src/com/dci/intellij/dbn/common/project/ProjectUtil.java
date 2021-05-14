package com.dci.intellij.dbn.common.project;

import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Safe;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import org.jetbrains.annotations.NotNull;

public interface ProjectUtil {
    static void closeProject(@NotNull Project project) {
        Dispatch.run(() -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }


    static void projectOpened(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectOpened(@NotNull Project project) {
                        runnable.run(project);
                    }
                });
    }

    static void projectClosing(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosing(@NotNull Project project) {
                        runnable.run(project);
                    }
                });

    }

    static void projectClosed(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosed(@NotNull Project project) {
                        Safe.run(() -> runnable.run(project));
                    }
                });

    }

}
