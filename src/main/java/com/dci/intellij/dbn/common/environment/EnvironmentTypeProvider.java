package com.dci.intellij.dbn.common.environment;

import org.jetbrains.annotations.NotNull;

public interface EnvironmentTypeProvider {

    @NotNull
    EnvironmentType getEnvironmentType();
}
