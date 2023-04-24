package com.dci.intellij.dbn.execution.script;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.RemoteExecutionInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScriptExecutionInput extends RemoteExecutionInput {
    private CmdLineInterface cmdLineInterface;
    private VirtualFile sourceFile;
    private boolean clearOutput;

    ScriptExecutionInput(Project project, VirtualFile sourceFile, ConnectionHandler connection, SchemaId targetSchema, boolean clearOutput) {
        super(project, ExecutionTarget.SCRIPT);
        this.sourceFile = sourceFile;
        setTargetConnection(connection);
        setTargetSchemaId(targetSchema);
        this.clearOutput = clearOutput;
    }

    @Override
    protected ScriptExecutionContext createExecutionContext() {
        return new ScriptExecutionContext(this);
    }

    @Override
    public ConnectionHandler getConnection() {
        return getTargetConnection();
    }

    public SchemaId getSchemaId() {
        return getTargetSchemaId();
    }
}
