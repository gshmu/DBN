package com.dci.intellij.dbn.execution.method.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class SetExecutionSchemaAction extends AnObjectAction<DBSchema> {
    private MethodExecutionInput executionInput;

    public SetExecutionSchemaAction(MethodExecutionInput executionInput, DBSchema schema) {
        super(schema);
        this.executionInput = executionInput;
    }

    @NotNull
    public DBSchema getSchema() {
        return FailsafeUtil.get(getObject());
    }

    public void actionPerformed(AnActionEvent e) {
        executionInput.setExecutionSchema(getSchema());
    }
}
