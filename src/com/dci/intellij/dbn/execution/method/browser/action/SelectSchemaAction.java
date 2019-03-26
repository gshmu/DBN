package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class SelectSchemaAction extends AnObjectAction<DBSchema> {
    private MethodExecutionBrowserForm browserComponent;

    public SelectSchemaAction(MethodExecutionBrowserForm browserComponent, DBSchema schema) {
        super(schema );
        this.browserComponent = browserComponent;
    }


    @NotNull
    public DBSchema getSchema() {
        return Failsafe.nn(getObject());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        browserComponent.setSchema(getSchema());
    }


}
