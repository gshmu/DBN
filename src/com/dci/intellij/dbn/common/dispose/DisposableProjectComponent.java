package com.dci.intellij.dbn.common.dispose;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.project.Project;

public interface DisposableProjectComponent extends Disposable {
    @NotNull
    Project getProject();
}
