package com.dci.intellij.dbn.execution.script;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.RemoteExecutionInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptExecutionInput extends RemoteExecutionInput {
    private CmdLineInterface cmdLineInterface;
    private VirtualFile sourceFile;
    private boolean clearOutput;

    ScriptExecutionInput(Project project, VirtualFile sourceFile, ConnectionHandler connection, SchemaId targetSchema, boolean clearOutput) {
        super(project, ExecutionTarget.SCRIPT);
        this.sourceFile = sourceFile;
        setTargetConnection(connection);
        setSchema(targetSchema);
        this.clearOutput = clearOutput;
    }

    @Override
    protected ExecutionContext createExecutionContext() {
        return new ExecutionContext() {
            @NotNull
            @Override
            public String getTargetName() {
                return sourceFile.getPath();
            }

            @Nullable
            @Override
            public ConnectionHandler getTargetConnection() {
                return ScriptExecutionInput.this.getConnection();
            }

            @Nullable
            @Override
            public SchemaId getTargetSchema() {
                return getSchema();
            }
        };
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

    @Override
    public ConnectionHandler getConnection() {
        return getTargetConnection();
    }

    public SchemaId getSchema() {
        return getTargetSchemaId();
    }

    public void setSchema(SchemaId schema) {
        setTargetSchemaId(schema);
    }

    public boolean isClearOutput() {
        return clearOutput;
    }

    public void setClearOutput(boolean clearOutput) {
        this.clearOutput = clearOutput;
    }
}
