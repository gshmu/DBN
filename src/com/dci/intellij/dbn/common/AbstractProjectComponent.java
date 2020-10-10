package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.intellij.openapi.application.ApplicationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

@Nullifiable
public abstract class AbstractProjectComponent extends DisposableBase
        implements
        SettingsSupport,
        ApplicationListener,
        ProjectComponent,
        ProjectManagerListener,
        RegisteredDisposable,
        NotificationSupport {

    private final ProjectRef projectRef;

    protected AbstractProjectComponent(Project project) {
        this.projectRef = ProjectRef.from(project);
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
        ApplicationManager.getApplication().addApplicationListener(this);
    }

    @Override
    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

    public boolean canCloseProject() {
        return true;
    }

    public void projectClosing() {

    }

    @Override
    public void initComponent() {
    }

    /***********************************************
     *            ApplicationListener              *
     ***********************************************/

    @Override
    public boolean canExitApplication() {
        return true;
    }

    @Override
    public void applicationExiting() {
    }

    @Override
    public void beforeWriteActionStart(@NotNull Object action) {
    }

    @Override
    public void writeActionStarted(@NotNull Object action) {
    }

    @Override
    public void writeActionFinished(@NotNull Object action) {
    }

    @Override
    public void afterWriteActionFinished(@NotNull Object action) {
    }

    /***********************************************
     *            ProjectManagerListener           *
     ***********************************************/
    @Override
    public final void projectOpened(@NotNull Project project) {}

    @Override
    public final void projectClosed(@NotNull Project project) {}

    @Override
    public final boolean canCloseProject(@NotNull Project project) {
        if (project.equals(getProject())) {
            return canCloseProject();
        }
        return true;
    }

    @Override
    public final void projectClosing(@NotNull Project project) {
        if (project.equals(getProject())) {
            projectClosing();
        }
    }


    @Override
    public void disposeComponent() {
        dispose();
    }

    protected void closeProject() {
        ProjectUtil.closeProject(getProject());
    }

    @Override
    public void checkDisposed() {
        super.checkDisposed();
        getProject();
    }
}
