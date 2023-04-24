package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MethodExecutionContext extends ExecutionContext<MethodExecutionInput> {
    public MethodExecutionContext(MethodExecutionInput input) {
        super(input);
    }

    @NotNull
    @Override
    public String getTargetName() {
        DBObjectRef method = getInput().getMethodRef();
        return method.getObjectType().getName() + " " + method.getObjectName();
    }

    @Nullable
    @Override
    public ConnectionHandler getTargetConnection() {
        return getInput().getConnection();
    }

    @Nullable
    @Override
    public SchemaId getTargetSchema() {
        return getInput().getTargetSchemaId();
    }

    public ExecutionOptions getOptions() {
        return getInput().getOptions();
    }
}
