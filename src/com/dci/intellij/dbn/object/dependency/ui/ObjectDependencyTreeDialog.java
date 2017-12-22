package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.Action;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public class ObjectDependencyTreeDialog extends DBNDialog<ObjectDependencyTreeForm> {
    private DBObjectRef<DBSchemaObject> objectRef;
    public ObjectDependencyTreeDialog(Project project, DBSchemaObject object) {
        super(project, "Object dependency tree", true);
        this.objectRef = DBObjectRef.from(object);
        setModal(false);
        setResizable(true);
        getCancelAction().putValue(Action.NAME, "Close");
        init();
    }

    @NotNull
    @Override
    protected ObjectDependencyTreeForm createComponent() {
        DBSchemaObject object = DBObjectRef.get(objectRef);
        return new ObjectDependencyTreeForm(this, object);
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

    @NotNull
    protected final Action[] createActions() {
        return new Action[]{getCancelAction()};
    }
}
