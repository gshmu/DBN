package com.dci.intellij.dbn.debugger.jdbc.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBMethodRunConfigFactory extends DBProgramRunConfigurationFactory {
    protected DBMethodRunConfigFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        DBMethodRunConfig runConfiguration = (DBMethodRunConfig) configuration;
        MethodExecutionInput executionInput = runConfiguration.getExecutionInput();
        if (runConfiguration.isGeneric() || executionInput == null) {
            return super.getIcon();
        } else {
            DBObjectRef<DBMethod> methodRef = executionInput.getMethodRef();
            DBMethod method = methodRef.get();
            return method == null ? methodRef.getObjectType().getIcon() : method.getIcon();
        }
    }

    @Override
    public RunConfiguration createTemplateConfiguration(Project project) {
        return new DBMethodRunConfig(project, this, "", false);
    }

    @Override
    public DBProgramRunConfiguration createConfiguration(Project project, String name, boolean generic) {
        return new DBMethodRunConfig(project, this, name, generic);
    }

    public DBMethodRunConfig createConfiguration(DBMethod method) {
        String name = DatabaseDebuggerManager.createMethodConfigurationName(method);
        DBMethodRunConfig runConfiguration = new DBMethodRunConfig(method.getProject(), this, name, false);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
        runConfiguration.setExecutionInput(executionInput);
        return runConfiguration;
    }

}
