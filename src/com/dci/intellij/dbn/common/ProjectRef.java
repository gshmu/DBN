package com.dci.intellij.dbn.common;

import java.lang.ref.WeakReference;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;

public class ProjectRef {
    private WeakReference<Project> ref;
    public ProjectRef(Project project) {
        ref = new WeakReference<Project>(project);
    }

    @Nullable
    public Project get() {
        return ref.get();
    }
}
