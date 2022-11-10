package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ExecutionSchemaSelectAction extends AnObjectAction<DBSchema> {
    private MethodExecutionInput executionInput;

    ExecutionSchemaSelectAction(MethodExecutionInput executionInput, DBSchema schema) {
        super(schema);
        this.executionInput = executionInput;
    }

    @Override
    protected void actionPerformed(
            @NotNull AnActionEvent e,
            @NotNull Project project,
            @NotNull DBSchema object) {

        executionInput.setTargetSchemaId(SchemaId.from(object));
    }
}
