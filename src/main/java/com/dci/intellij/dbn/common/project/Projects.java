package com.dci.intellij.dbn.common.project;

import com.dci.intellij.dbn.common.event.ApplicationEvents;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.util.Guarded;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import org.jetbrains.annotations.NotNull;

public final class Projects {
    private Projects() {}

    public static void closeProject(@NotNull Project project) {
        Dispatch.run(() -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }

    public static void projectOpened(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectOpened(@NotNull Project project) {
                        Guarded.run(() -> runnable.run(project));
                    }
                });
    }

    public static void projectClosing(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosing(@NotNull Project project) {
                        Guarded.run(() -> runnable.run(project));
                    }
                });

    }

    public static void projectClosed(ParametricRunnable<Project, RuntimeException> runnable) {
        ApplicationEvents.subscribe(null, ProjectManager.TOPIC,
                new ProjectManagerListener() {
                    @Override
                    public void projectClosed(@NotNull Project project) {
                        Guarded.run(() -> runnable.run(project));
                    }
                });

    }

    public static @NotNull Project[] getOpenProjects() {
        return ProjectManager.getInstance().getOpenProjects();
    }
}
