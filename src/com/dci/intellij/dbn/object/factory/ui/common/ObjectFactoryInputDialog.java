package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

public class ObjectFactoryInputDialog extends DBNDialog<ObjectFactoryInputForm> {
    private ObjectFactoryInputForm inputForm;
    public ObjectFactoryInputDialog(Project project, @NotNull ObjectFactoryInputForm inputForm) {
        super(project, "Create " + inputForm.getObjectType().getName(), true);
        this.inputForm = inputForm;
        setModal(true);
        setResizable(true);
        Disposer.register(this, inputForm);
        init();
    }

    @NotNull
    @Override
    protected ObjectFactoryInputForm createComponent() {
        return inputForm;
    }

    @Override
    public void doOKAction() {
        Project project = getComponent().getConnectionHandler().getProject();
        DatabaseObjectFactory factory = DatabaseObjectFactory.getInstance(project);
        ObjectFactoryInput factoryInput = getComponent().createFactoryInput(null);
        if (factory.createObject(factoryInput)) {
            super.doOKAction();
        }
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }
}
