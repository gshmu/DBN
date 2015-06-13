package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.JComponent;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.intellij.openapi.project.Project;

public class ObjectQuickFilterDialog extends DBNDialog<ObjectQuickFilterForm> {
    public ObjectQuickFilterDialog(Project project) {
        super(project, "Quick Filter", true);
        component = new ObjectQuickFilterForm(this);
        setModal(true);
        setResizable(false);
        init();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return component == null ? null : component.getPreferredFocusedComponent();
    }

    public void doOKAction() {
        super.doOKAction();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }
}
