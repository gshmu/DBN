package com.dci.intellij.dbn.common.project;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

public interface ProjectUtil {
    static void closeProject(@NotNull Project project) {
        Dispatch.run(() -> {
            ProjectManager.getInstance().closeProject(project);
            WelcomeFrame.showIfNoProjectOpened();
        });
    }


    static void projectOpened(ParametricRunnable<Project, RuntimeException> runnable) {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(
                ProjectManager.TOPIC, new ProjectManagerListener() {
                    @Override
                    public void projectOpened(@NotNull Project project) {
                        runnable.run(project);
                    }
                });

    }

    static void projectClosing(ParametricRunnable<Project, RuntimeException> runnable) {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(
                ProjectManager.TOPIC, new ProjectManagerListener() {
                    @Override
                    public void projectClosing(@NotNull Project project) {
                        runnable.run(project);
                    }
                });

    }

    static void projectClosed(ParametricRunnable<Project, RuntimeException> runnable) {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(
                ProjectManager.TOPIC, new ProjectManagerListener() {
                    @Override
                    public void projectClosed(@NotNull Project project) {
                        runnable.run(project);
                    }
                });

    }

}
