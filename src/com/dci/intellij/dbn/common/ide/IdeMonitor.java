package com.dci.intellij.dbn.common.ide;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ex.ApplicationEx;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.impl.welcomeScreen.WelcomeFrame;
import org.jetbrains.annotations.NotNull;

public class IdeMonitor implements ApplicationComponent{

    public static IdeMonitor getInstance() {
        return ApplicationManager.getApplication().getComponent(IdeMonitor.class);
    }

    private static class ProjectCloseRunnable implements Runnable {
        private Project project;

        public ProjectCloseRunnable(Project project) {
            this.project = project;
        }

        @Override
        public void run() {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    ProjectManager.getInstance().closeProject(project);
                    WelcomeFrame.showIfNoProjectOpened();
                }
            }.start();

        }
    }

    private static class ApplicationCloseRunnable implements Runnable {
        @Override
        public void run() {
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    ApplicationEx application = (ApplicationEx) ApplicationManager.getApplication();
                    application.exit(false, true);
                }
            }.start();

        }
    }

    public Runnable getProjectCloseCallback(Project project) {
        return new ProjectCloseRunnable(project);
    }


    public Runnable getAppCloseCallback() {
        return new ApplicationCloseRunnable();
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DBNavigator.IdeMonitor";
    }
}
