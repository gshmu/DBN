package com.dci.intellij.dbn.common.component;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class Components {
    private Components() {}


    public static <T extends ProjectComponent> T projectService(@NotNull Project project, @NotNull Class<T> interfaceClass) {
        T service = Failsafe.nd(project).getService(interfaceClass);
        return Failsafe.nn(service);
    }

    public static <T extends ApplicationComponent> T applicationService(@NotNull Class<T> interfaceClass) {
        return ApplicationManager.getApplication().getService(interfaceClass);
    }
}
