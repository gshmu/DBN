package com.dci.intellij.dbn.common.routine;

import com.intellij.openapi.progress.ProgressIndicator;

@FunctionalInterface
public interface ProgressRunnable extends ParametricRunnable.Basic<ProgressIndicator>{
}
