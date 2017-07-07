package com.dci.intellij.dbn.execution;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public abstract class ExecutionInput extends DisposableBase implements Disposable, ConnectionProvider, PersistentConfiguration {
    private ExecutionTimeout executionTimeout;
    private ExecutionTimeout debugExecutionTimeout;
    private ExecutionTarget executionTarget;

    private ProjectRef projectRef;
    protected ConnectionHandlerRef targetConnectionRef;
    protected DBObjectRef<DBSchema> targetSchemaRef;

    private final TimeoutSettingsListener timeoutSettingsListener = new TimeoutSettingsListener() {
        @Override
        public void settingsChanged(ExecutionTarget executionTarget) {
            if (executionTarget == ExecutionInput.this.executionTarget) {
                ExecutionTimeoutSettings timeoutSettings = getExecutionTimeoutSettings();
                executionTimeout.updateSettingsValue(timeoutSettings.getExecutionTimeout());
                debugExecutionTimeout.updateSettingsValue(timeoutSettings.getDebugExecutionTimeout());
            }
        }
    };

    public ExecutionInput(Project project, ExecutionTarget executionTarget) {
        projectRef = new ProjectRef(project);
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

    public Project getProject() {
        return projectRef.get();
    }

    public DBSchema getTargetSchema() {
        return DBObjectRef.get(targetSchemaRef);
    }

    public void setTargetSchema(DBSchema schema){
        targetSchemaRef = schema.getRef();
    }

    public ConnectionHandler getTargetConnection() {
        return ConnectionHandlerRef.get(targetConnectionRef);
    }

    public void setTargetConnection(ConnectionHandler connectionHandler) {
        this.targetConnectionRef = ConnectionHandlerRef.from(connectionHandler);
    }

    @NotNull
    public abstract ExecutionContext getExecutionContext();

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

    public void readConfiguration(Element element) {
        executionTimeout.set(SettingsUtil.getIntegerAttribute(element, "execution-timeout", executionTimeout.get()));
        debugExecutionTimeout.set(SettingsUtil.getIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get()));
    }

    public void writeConfiguration(Element element) {
        SettingsUtil.setIntegerAttribute(element, "execution-timeout", executionTimeout.get());
        SettingsUtil.setIntegerAttribute(element, "debug-execution-timeout", debugExecutionTimeout.get());
    }

    @NotNull
    public final ExecutionTarget getExecutionTarget() {
        return executionTarget;
    }
}
