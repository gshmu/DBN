package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ProjectSupplier {
    @NotNull
    Project getProject();
}
