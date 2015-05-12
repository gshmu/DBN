package com.dci.intellij.dbn.execution.method.browser.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.execution.method.browser.ui.MethodExecutionBrowserForm;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;

public class SelectSchemaAction extends DumbAwareAction {
    private DBObjectRef<DBSchema> schemaRef;
    private MethodExecutionBrowserForm browserComponent;

    public SelectSchemaAction(MethodExecutionBrowserForm browserComponent, DBSchema schema) {
        super(NamingUtil.enhanceUnderscoresForDisplay(schema.getQualifiedName()), null, schema.getIcon());
        this.browserComponent = browserComponent;
        this.schemaRef = DBObjectRef.from(schema);
    }


    @NotNull
    public DBSchema getSchema() {
        return DBObjectRef.getnn(schemaRef);
    }

    public void actionPerformed(AnActionEvent e) {
        browserComponent.setSchema(getSchema());
    }


}
