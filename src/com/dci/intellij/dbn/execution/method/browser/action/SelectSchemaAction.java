package com.dci.intellij.dbn.execution.method.browser.action;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.action.AnObjectAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class SelectSchemaAction extends AnObjectAction<DBSchema> {
    private WeakRef<MethodExecutionBrowserForm> browserComponent;

    SelectSchemaAction(MethodExecutionBrowserForm browserComponent, DBSchema schema) {
        super(schema);
        this.browserComponent = WeakRef.from(browserComponent);
    }


    @NotNull
    public DBSchema getSchema() {
        return Failsafe.nn(getObject());
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DBSchema object) {
        MethodExecutionBrowserForm browserForm = browserComponent.get();
        if (browserForm != null) {
            browserForm.setSchema(object);
        }
    }


}
