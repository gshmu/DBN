package com.dci.intellij.dbn.execution;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
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
    protected DBObjectRef<DBSchema> targetSchemaRef;

    private Latent<ExecutionContext> executionContext = Latent.create(this::createExecutionContext);

    @NotNull
    public final ExecutionContext getExecutionContext() {
        return executionContext.get();
    }

    protected void resetExecutionContext() {
        executionContext.reset();
    }

    protected abstract ExecutionContext createExecutionContext();

    private final TimeoutSettingsListener timeoutSettingsListener = new TimeoutSettingsListener() {
        @Override
        public void settingsChanged(ExecutionTarget executionTarget) {
            if (executionTarget == getExecutionTarget()) {
                ExecutionTimeoutSettings timeoutSettings = getExecutionTimeoutSettings();
                executionTimeout.updateSettingsValue(timeoutSettings.getExecutionTimeout());
                debugExecutionTimeout.updateSettingsValue(timeoutSettings.getDebugExecutionTimeout());
            }
        }
    };

    public ExecutionInput(Project project, ExecutionTarget executionTarget) {
        projectRef = ProjectRef.from(project);
        this.executionTarget = executionTarget;
        ExecutionTimeoutSettings timeoutSettings = getExecutionTimeoutSettings();
        executionTimeout = new ExecutionTimeout(timeoutSettings.getExecutionTimeout());
        debugExecutionTimeout = new ExecutionTimeout(timeoutSettings.getDebugExecutionTimeout());
        EventUtil.subscribe(getProject(), this, TimeoutSettingsListener.TOPIC, timeoutSettingsListener);
    }

    @NotNull
    public ExecutionTimeoutSettings getExecutionTimeoutSettings() {
        Project project = getProject();
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        return executionEngineSettings.getExecutionTimeoutSettings(getExecutionTarget());
    }

    @NotNull
    public final Project getProject() {
        return projectRef.getnn();
    }

    @Nullable
    public final DBSchema getTargetSchema() {
        return DBObjectRef.get(targetSchemaRef);
    }

    public final void setTargetSchema(@Nullable DBSchema schema){
        targetSchemaRef = DBObjectRef.from(schema);
    }

    @Nullable
    public final ConnectionHandler getTargetConnection() {
        return ConnectionHandlerRef.get(targetConnectionRef);
    }

    public final void setTargetConnection(@Nullable ConnectionHandler connectionHandler) {
        this.targetConnectionRef = ConnectionHandlerRef.from(connectionHandler);
    }

    @NotNull
    public ExecutionContext getExecutionContext(boolean reset) {
        ExecutionContext executionContext = getExecutionContext();
        if (reset) executionContext.reset();
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
        executionTimeout.set(SettingsUtil.getIntegerAttribute(element, "execution-timeout", executionTimeout.get()));
        debugExecutionTimeout.set(SettingsUtil.getIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get()));
    }

    @Override
    public void writeConfiguration(Element element) {
        SettingsUtil.setIntegerAttribute(element, "execution-timeout", executionTimeout.get());
        SettingsUtil.setIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get());
    }

    @NotNull
    public final ExecutionTarget getExecutionTarget() {
        return executionTarget;
    }
}
