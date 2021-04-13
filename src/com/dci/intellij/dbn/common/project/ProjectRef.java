package com.dci.intellij.dbn.common.project;

import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectRef extends WeakRef<Project> {
    private ProjectRef(Project project) {
        super(project);
    }

    public static ProjectRef of(Project project) {
        if (project == null) {
            return new ProjectRef(null);
        } else {
            ProjectRef projectRef = project.getUserData(UserDataKeys.PROJECT_REF);
            if (projectRef == null) {
                projectRef = new ProjectRef(project);
                project.putUserData(UserDataKeys.PROJECT_REF, projectRef);
            }
            return projectRef;
        }
    }

    @NotNull
    @Override
    public Project ensure() {
        Project project = super.ensure();
        if (project.isDisposed()) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return project;
    }
}
