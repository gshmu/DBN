package com.dci.intellij.dbn.execution.method.browser.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.ActionEvent;

public class MethodExecutionBrowserDialog extends DBNDialog implements Disposable, TreeSelectionListener {
    private MethodExecutionBrowserForm mainComponent;
    private SelectAction selectAction;
    private DBMethod method;

    public MethodExecutionBrowserDialog(Project project, MethodBrowserSettings settings, ObjectTreeModel objectTreeModel) {
        super(project, "Method Browser", true);
        setModal(true);
        setResizable(true);
        mainComponent = new MethodExecutionBrowserForm(project, settings, objectTreeModel);
        mainComponent.addTreeSelectionListener(this);
        init();
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.MethodBrowser";
    }

    @Override
    public void show() {
        super.show();
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainComponent.getComponent();
    }

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

    public void dispose() {
        super.dispose();
        mainComponent.dispose();
        mainComponent = null;
    }

    public void valueChanged(TreeSelectionEvent e) {
        selectAction.setEnabled(mainComponent.getSelectedMethod() != null);
    }

    public DBMethod getSelectedMethod() {
        return method;        
    }

    /**********************************************************
     *                         Actions                        *
     **********************************************************/
    private class SelectAction extends AbstractAction {

        public SelectAction() {
            super("Select");
        }

        public void actionPerformed(ActionEvent e) {
            method = mainComponent.getSelectedMethod();
            close(OK_EXIT_CODE);
        }

    }
}
