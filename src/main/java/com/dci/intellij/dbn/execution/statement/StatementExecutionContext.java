package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StatementExecutionContext extends ExecutionContext<StatementExecutionInput> {
    public StatementExecutionContext(StatementExecutionInput input) {
        super(input);
    }

    @NotNull
    @Override
    public String getTargetName() {
        ExecutablePsiElement executablePsiElement = getInput().getExecutablePsiElement();
        return Commons.nvl(executablePsiElement == null ? null : executablePsiElement.getPresentableText(), "Statement");
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
}
