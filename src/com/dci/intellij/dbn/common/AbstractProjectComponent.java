package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.component.LegacyComponent;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.notification.NotificationSupport;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.project.ProjectUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractProjectComponent extends StatefulDisposable.Base implements
        ProjectComponent,
        ProjectManagerListener,
        StatefulDisposable,
        NotificationSupport,
        LegacyComponent {

    private final ProjectRef project;

    protected AbstractProjectComponent(@NotNull Project project) {
        this.project = ProjectRef.of(project);
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
    }

    @Override
    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    public boolean canCloseProject() {
        return true;
    }

    @Deprecated
    public final void projectOpened() {}

    @Deprecated
    public final void projectClosed() {}

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
            ProjectUtil.closeProject(getProject());
        }
    }

    @Override
    public void checkDisposed() {
        super.checkDisposed();
        getProject();
    }
}
