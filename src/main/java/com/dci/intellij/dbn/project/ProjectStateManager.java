package com.dci.intellij.dbn.project;

import com.dci.intellij.dbn.common.component.Components;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

public class ProjectStateManager extends ProjectComponentBase {
    public static final String COMPONENT_NAME = "DBNavigator.Project.StateManager";

    protected ProjectStateManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
    }

    public static ProjectStateManager getInstance(@NotNull Project project) {
        return Components.projectService(project, ProjectStateManager.class);
    }

    public static void registerDisposable(Project project, Disposable child) {
        guarded(() -> {
            if (isNotValid(project)) return;
            ProjectStateManager stateManager = ProjectStateManager.getInstance(project);
            Disposer.register(stateManager, child);
        });
    }
}
