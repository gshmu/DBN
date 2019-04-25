package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SetExecutionSchemaAction extends AnObjectAction<DBSchema> {
    private MethodExecutionInput executionInput;

    SetExecutionSchemaAction(MethodExecutionInput executionInput, DBSchema schema) {
        super(schema);
        this.executionInput = executionInput;
    }

    @NotNull
    public DBSchema getSchema() {
        return Failsafe.nn(getObject());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchema object) {
        executionInput.setTargetSchemaId(SchemaId.from(object));
    }
}
