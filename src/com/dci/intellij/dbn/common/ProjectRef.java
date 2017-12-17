package com.dci.intellij.dbn.common;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class ProjectRef {
    private WeakReference<Project> ref;
    public ProjectRef(Project project) {
        ref = new WeakReference<Project>(project);
    }

    @Nullable
    public Project get() {
        return ref.get();
    }

    @NotNull
    public Project getnn() {
        return FailsafeUtil.get(ref.get());
    }
}
