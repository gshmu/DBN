package com.dci.intellij.dbn.object.filter.quick.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ObjectQuickFilterDialog extends DBNDialog<ObjectQuickFilterForm> {
    private DBObjectList objectList;
    public ObjectQuickFilterDialog(Project project, DBObjectList objectList) {
        super(project, "Quick filter", true);
        this.objectList = objectList;
        setModal(true);
        setResizable(false);
        getOKAction().putValue(Action.NAME, "Apply");
        init();
    }

    @NotNull
    @Override
    protected ObjectQuickFilterForm createComponent() {
        return new ObjectQuickFilterForm(this, objectList);
    }

    @Override
    protected String getDimensionServiceKey() {
        return null;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        return new Action[]{
                getOKAction(),
                new AbstractAction("Clear Filters") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        getComponent().getFilter().clear();
                        doOKAction();
                    }
                },
                getCancelAction()
        };
    }

    @Override
    public void doOKAction() {
        try {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.applyFilter(getComponent().getObjectList(), getComponent().getFilter());
        } finally {
            super.doOKAction();
        }
    }

    @Override
    public void doCancelAction() {
        super.doCancelAction();
    }
}
