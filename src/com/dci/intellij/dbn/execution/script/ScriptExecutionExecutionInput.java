package com.dci.intellij.dbn.execution.script;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionExecutionInput {
    private CmdLineInterface cmdLineInterface;
    private VirtualFile sourceFile;
    private ConnectionHandlerRef connectionHandlerRef;
    private DBObjectRef<DBSchema> schemaRef;
    private int executionTimeout;
    private boolean clearOutput;

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

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
}
