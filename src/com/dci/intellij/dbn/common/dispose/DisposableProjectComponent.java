package com.dci.intellij.dbn.common.dispose;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface DisposableProjectComponent extends RegisteredDisposable {
    @NotNull
    Project getProject();
}
