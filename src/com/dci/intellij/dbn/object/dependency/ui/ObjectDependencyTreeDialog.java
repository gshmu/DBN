package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class ObjectDependencyTreeDialog extends DBNDialog {
    private ObjectDependencyTreeForm inputForm;

    public ObjectDependencyTreeDialog(ObjectDependencyTreeForm inputForm) {
        super(inputForm.getProject(), "Object Dependency Tree", true);
        this.inputForm = inputForm;
        setModal(true);
        setResizable(true);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.ObjectDependencyTreeDialog";
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return inputForm.getComponent();
    }

    public void doOKAction() {
    }

    public void doCancelAction() {
        super.doCancelAction();
    }

}
