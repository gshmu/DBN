package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.Action;

public class ObjectDependencyTreeDialog extends DBNDialog<ObjectDependencyTreeForm> {
    private DBObjectRef<DBSchemaObject> objectRef;
    public ObjectDependencyTreeDialog(Project project, DBSchemaObject object) {
        super(project, "Object dependency tree", true);
        this.objectRef = DBObjectRef.of(object);
        setModal(false);
        setResizable(true);
        renameAction(getCancelAction(), "Close");
        init();
    }

    @NotNull
    @Override
    protected ObjectDependencyTreeForm createForm() {
        DBSchemaObject object = DBObjectRef.get(objectRef);
        return new ObjectDependencyTreeForm(this, object);
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        return new Action[]{getCancelAction()};
    }
}
