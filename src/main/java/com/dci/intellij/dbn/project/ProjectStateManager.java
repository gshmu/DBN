package com.dci.intellij.dbn.project;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectStateManager extends ProjectComponentBase {
    public static final String COMPONENT_NAME = "DBNavigator.Project.Initializer";

    protected ProjectStateManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
    }

    public static ProjectStateManager getInstance(@NotNull Project project) {
        return Components.projectService(project, ProjectStateManager.class);
    }

    public static void registerDisposable(Project project, Disposable child) {
        Disposer.register(ProjectStateManager.getInstance(project), child);
    }
}
