package com.dci.intellij.dbn.execution.method.browser.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;

public class MethodExecutionBrowserDialog extends DBNDialog<MethodExecutionBrowserForm> implements Disposable {
    private SelectAction selectAction;
    private DBObjectRef<DBMethod> methodRef;
    private ObjectTreeModel objectTreeModel;
    private boolean debug;

    public MethodExecutionBrowserDialog(Project project, ObjectTreeModel objectTreeModel, boolean debug) {
        super(project, "Method browser", true);
        setModal(true);
        setResizable(true);
        this.objectTreeModel = objectTreeModel;
        this.debug = debug;
        getComponent().addTreeSelectionListener(selectionListener);
        init();
    }

    @NotNull
    @Override
    protected MethodExecutionBrowserForm createComponent() {
        return new MethodExecutionBrowserForm(this, objectTreeModel, debug);
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    @NotNull
    protected final Action[] createActions() {
        selectAction = new SelectAction();
        selectAction.setEnabled(false);
        return new Action[]{selectAction, getCancelAction()};
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
    }

    private TreeSelectionListener selectionListener = new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            selectAction.setEnabled(getComponent().getSelectedMethod() != null);
        }
    };


    public DBMethod getSelectedMethod() {
        return DBObjectRef.get(methodRef);
    }

    /**********************************************************
     *                         Actions                        *
     **********************************************************/
    private class SelectAction extends AbstractAction {

        public SelectAction() {
            super("Select");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            methodRef = DBObjectRef.from(getComponent().getSelectedMethod());
            close(OK_EXIT_CODE);
        }

    }
}
