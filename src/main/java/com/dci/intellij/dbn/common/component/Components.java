package com.dci.intellij.dbn.common.component;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public class Components {
    private Components() {}


    @NotNull
    public static <T extends ProjectComponent> T projectService(@NotNull Project project, @NotNull Class<T> interfaceClass) {
        DatabaseNavigator.getInstance();
        return nd(project).getService(interfaceClass);
    }

    @NotNull
    public static <T> T projectComponent(@NotNull Project project, @NotNull Class<T> interfaceClass) {
        DatabaseNavigator.getInstance();
        return nd(project).getComponent(interfaceClass);
    }

    @NotNull
    public static <T extends ApplicationComponent> T applicationService(@NotNull Class<T> interfaceClass) {
        return ApplicationManager.getApplication().getService(interfaceClass);
    }
}
