package com.dci.intellij.dbn.common.component;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.project.Projects;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class ProjectComponentBase extends StatefulDisposable.Base implements
        ProjectComponent,
        ProjectManagerListener,
        StatefulDisposable,
        NotificationSupport,
        Service {

    private final ProjectRef project;
    private final String componentName;

    protected ProjectComponentBase(@NotNull Project project, String componentName) {
        this.project = ProjectRef.of(project);
        this.componentName = componentName;
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
    }

    @NotNull
    public final String getComponentName() {
        return componentName;
    }

    @Override
    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public boolean canCloseProject() {
        return true;
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
    }

    @Override
    protected void disposeInner() {
        nullify();
    }

    protected void closeProject(boolean exitApp) {
        if (exitApp) {
            ApplicationManager.getApplication().exit();
        } else {
            Projects.closeProject(getProject());
        }
    }

    @Override
    public void checkDisposed() {
        super.checkDisposed();
        getProject();
    }
}
