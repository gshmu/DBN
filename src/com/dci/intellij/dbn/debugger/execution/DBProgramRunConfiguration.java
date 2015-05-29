package com.dci.intellij.dbn.debugger.execution;

import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.PresentableConnectionProvider;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.project.Project;

public abstract class DBProgramRunConfiguration<T extends PresentableConnectionProvider, I extends ExecutionInput> extends RunConfigurationBase implements LocatableConfiguration {
    private boolean compileDependencies = true;
    private I executionInput;

    protected DBProgramRunConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    public boolean isCompileDependencies() {
        return compileDependencies;
    }

    public void setCompileDependencies(boolean compileDependencies) {
        this.compileDependencies = compileDependencies;
    }

    public I getExecutionInput() {
        return executionInput;
    }

    public void setExecutionInput(I executionInput) {
        this.executionInput = executionInput;
    }

    public abstract List<DBMethod> getMethods();

    @Nullable
    public abstract T getSource();

    @Nullable
    public final ConnectionHandler getConnectionHandler() {
        T source = getSource();
        return source == null ? null : source.getConnectionHandler();
    }
}
