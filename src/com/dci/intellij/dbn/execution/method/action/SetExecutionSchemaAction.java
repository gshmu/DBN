package com.dci.intellij.dbn.execution.method.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;

public class SetExecutionSchemaAction extends DumbAwareAction {
    private MethodExecutionInput executionInput;
    private DBObjectRef<DBSchema> schemaRef;

    public SetExecutionSchemaAction(MethodExecutionInput executionInput, DBSchema schema) {
        super(schema.getName(), null, schema.getIcon());
        this.executionInput = executionInput;
        this.schemaRef = DBObjectRef.from(schema);
    }

    @NotNull
    public DBSchema getSchema() {
        return DBObjectRef.getnn(schemaRef);
    }

    public void actionPerformed(AnActionEvent e) {
        executionInput.setExecutionSchema(getSchema());
    }

    public void update(AnActionEvent e) {
        DBSchema schema = getSchema();
        Presentation presentation = e.getPresentation();
        presentation.setText(NamingUtil.enhanceUnderscoresForDisplay(schema.getName()));
        presentation.setIcon(schema.getIcon());
        presentation.setDescription(schema.getDescription());
    }
}
