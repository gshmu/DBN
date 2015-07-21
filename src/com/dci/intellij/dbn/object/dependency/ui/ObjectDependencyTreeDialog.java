package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class ObjectDependencyTreeDialog extends DBNDialog<ObjectDependencyTreeForm> {
    public ObjectDependencyTreeDialog(Project project, DBSchemaObject schemaObject) {
        super(project, "Object Dependency Tree", true);
        this.component = new ObjectDependencyTreeForm(this, schemaObject);
        setModal(false);
        setResizable(true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{getCancelAction()};
    }
}
