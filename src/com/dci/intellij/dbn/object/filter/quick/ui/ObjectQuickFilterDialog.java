package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.openapi.project.Project;

public class ObjectQuickFilterDialog extends DBNDialog<ObjectQuickFilterForm> {
    public ObjectQuickFilterDialog(Project project, DBObjectList objectList) {
        super(project, "Quick filter", true);
        component = new ObjectQuickFilterForm(this, objectList);
        setModal(true);
        setResizable(false);
        getOKAction().putValue(Action.NAME, "Apply");
        init();
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
                        component.getFilter().clear();
                        doOKAction();
                    }
                },
                getCancelAction()
        };
    }

    public void doOKAction() {
        ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
        quickFilterManager.applyFilter(component.getObjectList(), component.getFilter());
        super.doOKAction();
    }

    public void doCancelAction() {
        super.doCancelAction();
    }
}
