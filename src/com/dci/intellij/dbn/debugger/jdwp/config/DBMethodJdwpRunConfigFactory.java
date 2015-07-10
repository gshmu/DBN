package com.dci.intellij.dbn.debugger.jdwp.config;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfigurationFactory;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;

public class DBMethodJdwpRunConfigFactory extends DBProgramRunConfigurationFactory {
    protected DBMethodJdwpRunConfigFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public Icon getIcon(@NotNull RunConfiguration configuration) {
        DBMethodJdwpRunConfig runConfiguration = (DBMethodJdwpRunConfig) configuration;
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
        return new DBMethodJdwpRunConfig(project, this, "", false);
    }

    @Override
    public DBProgramRunConfiguration createConfiguration(Project project, String name, boolean generic) {
        return new DBMethodJdwpRunConfig(project, this, name, generic);
    }

    public DBMethodJdwpRunConfig createConfiguration(DBMethod method) {
        String name = DatabaseDebuggerManager.createMethodConfigurationName(method);
        DBMethodJdwpRunConfig runConfiguration = new DBMethodJdwpRunConfig(method.getProject(), this, name, false);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(method.getProject());
        MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
        runConfiguration.setExecutionInput(executionInput);
        return runConfiguration;
    }

}
