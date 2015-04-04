package com.dci.intellij.dbn.execution.script;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ScriptExecutionManager extends AbstractProjectComponent {

    private ScriptExecutionManager(Project project) {
        super(project);
    }

    public static ScriptExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ScriptExecutionManager.class);
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ScriptExecutionManager";
    }
}