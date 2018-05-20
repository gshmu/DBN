package com.dci.intellij.dbn.execution.method.action;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SetExecutionSchemaComboBoxAction extends ComboBoxAction {
    private MethodExecutionInput executionInput;

    public SetExecutionSchemaComboBoxAction(MethodExecutionInput executionInput) {
        this.executionInput = executionInput;
        DBSchema schema = executionInput.getTargetSchema();
        if (schema != null) {
            Presentation presentation = getTemplatePresentation();
            presentation.setText(schema.getName(), false);
            presentation.setIcon(schema.getIcon());
        }
    }

    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (connectionHandler != null) {
            for (DBSchema schema : connectionHandler.getObjectBundle().getSchemas()){
                actionGroup.add(new SetExecutionSchemaAction(executionInput, schema));
            }
        }

        return actionGroup;
    }

    public void update(AnActionEvent e) {
        DBSchema schema = executionInput.getTargetSchema();
        Presentation presentation = e.getPresentation();
        presentation.setText(schema.getName(), false);
        presentation.setIcon(schema.getIcon());
    }
 }