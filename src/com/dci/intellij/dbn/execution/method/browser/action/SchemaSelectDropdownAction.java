package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JComponent;

public class SchemaSelectDropdownAction extends DBNComboBoxAction {
    MethodExecutionBrowserForm browserComponent;

    public SchemaSelectDropdownAction(MethodExecutionBrowserForm browserComponent) {
        this.browserComponent = browserComponent;
    }

    @Override
    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent jComponent) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        ConnectionHandler connection = browserComponent.getSettings().getConnection();
        if (connection != null) {
            for (DBSchema schema : connection.getObjectBundle().getSchemas()) {
                SchemaSelectAction schemaSelectAction = new SchemaSelectAction(browserComponent, schema);
                actionGroup.add(schemaSelectAction);
            }
        }
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        String text = "Schema";
        Icon icon = null;

        DBSchema schema = browserComponent.getSettings().getSchema();
        if (schema != null) {
            text = schema.getName();
            icon = schema.getIcon();
        }

        presentation.setText(text, false);
        presentation.setIcon(icon);
    }
 }