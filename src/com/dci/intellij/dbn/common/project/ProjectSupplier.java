package com.dci.intellij.dbn.common.project;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ProjectSupplier {
    @Nullable
    Project getProject();


    @NotNull
    default Project ensureProject() {
        return Failsafe.nd(getProject());
    }
}
