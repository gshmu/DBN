package com.dci.intellij.dbn.common;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.action.DBNDataKeys;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.project.Project;

public class ProjectRef extends WeakReference<Project>{
    private ProjectRef(Project project) {
        super(project);
    }

    @Nullable
    public Project get() {
        return super.get();
    }

    @NotNull
    public Project getnn() {
        return FailsafeUtil.get(get());
    }


    public static ProjectRef from(Project project) {
        ProjectRef projectRef = project.getUserData(DBNDataKeys.PROJECT_REF);
        if (projectRef == null) {
            projectRef = new ProjectRef(project);
            project.putUserData(DBNDataKeys.PROJECT_REF, projectRef);
        }
        return projectRef;
    }
}
