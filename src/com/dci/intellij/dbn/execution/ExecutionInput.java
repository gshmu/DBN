package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsSupport;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ExecutionInput extends DisposableBase implements Disposable, ConnectionProvider, PersistentConfiguration {
    private ExecutionTimeout executionTimeout;
    private ExecutionTimeout debugExecutionTimeout;
    private ExecutionTarget executionTarget;

    private ProjectRef projectRef;
    protected ConnectionHandlerRef targetConnectionRef;
    protected SchemaId targetSchemaId;

    private Latent<ExecutionContext> executionContext = Latent.basic(() -> createExecutionContext());

    @NotNull
    public final ExecutionContext getExecutionContext() {
        return executionContext.get();
    }

    protected void resetExecutionContext() {
        executionContext.reset();
    }

    protected abstract ExecutionContext createExecutionContext();

    public ExecutionInput(Project project, ExecutionTarget executionTarget) {
        projectRef = ProjectRef.from(project);
        this.executionTarget = executionTarget;
        executionTimeout = new ExecutionTimeout(project, executionTarget, false);
        debugExecutionTimeout = new ExecutionTimeout(project, executionTarget, true);
    }

    @NotNull
    public ExecutionTimeoutSettings getExecutionTimeoutSettings() {
        Project project = getProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        return executionEngineSettings.getExecutionTimeoutSettings(getExecutionTarget());
    }

    @NotNull
    public final Project getProject() {
        return projectRef.ensure();
    }

    @Nullable
    public final SchemaId getTargetSchemaId() {
        return targetSchemaId;
    }

    public final void setTargetSchemaId(@Nullable SchemaId schema){
        targetSchemaId = schema;
    }

    @Nullable
    public final ConnectionHandler getTargetConnection() {
        return ConnectionHandlerRef.get(targetConnectionRef);
    }

    public final void setTargetConnection(@Nullable ConnectionHandler connectionHandler) {
        this.targetConnectionRef = ConnectionHandlerRef.from(connectionHandler);
    }

    @NotNull
    public ExecutionContext initExecutionContext() {
        ExecutionContext executionContext = getExecutionContext();
        executionContext.reset();
        return executionContext;
    }

    public int getExecutionTimeout() {
        return executionTimeout.get();
    }

    public void setExecutionTimeout(int timeout) {
        executionTimeout.set(timeout);
    }

    public int getDebugExecutionTimeout() {
        return debugExecutionTimeout.get();
    }

    public void setDebugExecutionTimeout(int timeout) {
        debugExecutionTimeout.set(timeout);
    }

    public abstract ConnectionId getConnectionHandlerId();

    @Override
    public void readConfiguration(Element element) {
        executionTimeout.set(SettingsSupport.getIntegerAttribute(element, "execution-timeout", executionTimeout.get()));
        debugExecutionTimeout.set(SettingsSupport.getIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get()));
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsSupport.setIntegerAttribute(element, "execution-timeout", executionTimeout.get());
        SettingsSupport.setIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get());
    }

    @NotNull
    public final ExecutionTarget getExecutionTarget() {
        return executionTarget;
    }
}
