package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;

public abstract class AbstractProjectComponent implements ProjectComponent, ProjectManagerListener, Disposable{
    private Project project;

    protected AbstractProjectComponent(Project project) {
        this.project = project;
        ProjectManager projectManager = ProjectManager.getInstance();
        projectManager.addProjectManagerListener(project, this);
    }

    public Project getProject() {
        return project;
    }

    public void projectOpened() {
    }

    public void projectClosed() {
    }

    public void initComponent() {
    }

    /***********************************************
     *            ProjectManagerListener           *
     ***********************************************/
    @Override
    public void projectOpened(Project project) {

    }

    @Override
    public boolean canCloseProject(Project project) {
        return true;
    }

    @Override
    public void projectClosed(Project project) {

    }

    @Override
    public void projectClosing(Project project) {

    }


    /********************************************* *
     *                Disposable                   *
     ***********************************************/
    private boolean disposed = false;

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        project = null;
    }

    public final void disposeComponent() {
    }
}
