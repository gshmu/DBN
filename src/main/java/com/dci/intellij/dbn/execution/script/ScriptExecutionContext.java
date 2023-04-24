package com.dci.intellij.dbn.execution.script;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptExecutionContext extends ExecutionContext<ScriptExecutionInput> {
    public ScriptExecutionContext(ScriptExecutionInput input) {
        super(input);
    }

    @NotNull
    @Override
    public String getTargetName() {
        return getInput().getSourceFile().getPath();
    }

    @Nullable
    @Override
    public ConnectionHandler getTargetConnection() {
        return getInput().getConnection();
    }

    @Nullable
    @Override
    public SchemaId getTargetSchema() {
        return getInput().getSchemaId();
    }
}
