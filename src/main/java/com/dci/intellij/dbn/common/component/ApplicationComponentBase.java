package com.dci.intellij.dbn.common.component;


import org.jetbrains.annotations.NotNull;

public abstract class ApplicationComponentBase implements ApplicationComponent {
    private final String componentName;

    public ApplicationComponentBase(String componentName) {
        this.componentName = componentName;
    }

    @NotNull
    public final String getComponentName() {
        return componentName;
    }
}
