package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.factory.DatabaseObjectFactory;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class ObjectFactoryInputDialog extends DBNDialog {
    private ObjectFactoryInputForm inputForm;

    public ObjectFactoryInputDialog(ObjectFactoryInputForm inputForm) {
        super(inputForm.getConnectionHandler().getProject(),
                "Create " + inputForm.getObjectType().getName(), true);
        this.inputForm = inputForm;
        setModal(true);
        setResizable(true);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.ObjectFactoryInput";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return inputForm.getComponent();
    }

    public void doOKAction() {
        Project project = inputForm.getConnectionHandler().getProject();
        DatabaseObjectFactory factory = DatabaseObjectFactory.getInstance(project);
        ObjectFactoryInput factoryInput = inputForm.createFactoryInput(null);
        if (factory.createObject(factoryInput)) {
            super.doOKAction();
        }
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

}
