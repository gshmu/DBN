package com.dci.intellij.dbn.object.common.operation;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DatabaseOperationManager extends AbstractProjectComponent {
    private DatabaseOperationManager(Project project) {
        super(project);
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.OperationManager";
    }
}
