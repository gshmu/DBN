package com.dci.intellij.dbn.execution.script;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionInput implements ExecutionInput{
    private Project project;
    private CmdLineInterface cmdLineInterface;
    private VirtualFile sourceFile;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> schemaRef;
    private int executionTimeout;
    private boolean clearOutput;

    public ScriptExecutionInput(Project project, VirtualFile sourceFile, ConnectionHandler connectionHandler, DBSchema schema, boolean clearOutput) {
        this.project = project;
        this.sourceFile = sourceFile;
        setConnectionHandler(connectionHandler);
        setSchema(schema);
        this.clearOutput = clearOutput;
    }

    public CmdLineInterface getCmdLineInterface() {
        return cmdLineInterface;
    }

    public void setCmdLineInterface(CmdLineInterface cmdLineInterface) {
        this.cmdLineInterface = cmdLineInterface;
    }

    public VirtualFile getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(VirtualFile sourceFile) {
        this.sourceFile = sourceFile;
    }

    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = ConnectionHandlerRef.from(connectionHandler);
    }

    public DBSchema getSchema() {
        return DBObjectRef.get(schemaRef);
    }

    public void setSchema(DBSchema schema) {
        this.schemaRef = DBObjectRef.from(schema);
    }

    public boolean isClearOutput() {
        return clearOutput;
    }

    public void setClearOutput(boolean clearOutput) {
        this.clearOutput = clearOutput;
    }

    @Override
    public Project getProject() {
        return project;
    }

    @Override
    public int getDebugExecutionTimeout() {
        return 0;
    }

    @NotNull
    @Override
    public ExecutionContext getExecutionContext() {
        return null;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public void setDebugExecutionTimeout(int timeout) {

    }

    @Override
    public ExecutionTimeoutSettings getExecutionTimeoutSettings() {
        return ExecutionEngineSettings.getInstance(getProject()).getScriptExecutionSettings();
    }

    @Override
    public boolean isDisposed() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
