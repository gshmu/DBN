package com.dci.intellij.dbn.execution.method.browser.ui;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.action.SelectConnectionComboBoxAction;
import com.dci.intellij.dbn.execution.method.browser.action.SelectSchemaComboBoxAction;
import com.dci.intellij.dbn.execution.method.browser.action.ShowObjectTypeToggleAction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.ui.ObjectTree;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeCellRenderer;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

public class MethodExecutionBrowserForm extends DBNFormImpl implements DBNForm {

    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JTree methodsTree;

    private MethodBrowserSettings settings;
    private Project project;

    public MethodExecutionBrowserForm(Project project, MethodBrowserSettings settings, ObjectTreeModel model) {
        this.project = project;
        this.settings = settings;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new SelectConnectionComboBoxAction(this),
                new SelectSchemaComboBoxAction(this),
                ActionUtil.SEPARATOR,
                new ShowObjectTypeToggleAction(this, DBObjectType.PROCEDURE),
                new ShowObjectTypeToggleAction(this, DBObjectType.FUNCTION));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        methodsTree.setCellRenderer(new ObjectTreeCellRenderer());
        methodsTree.setModel(model);
        TreePath selectionPath = model.getInitialSelection();
        if (selectionPath != null) {
            methodsTree.setSelectionPath(selectionPath);
            methodsTree.scrollPathToVisible(selectionPath.getParentPath());
        }
    }

    public MethodBrowserSettings getSettings() {
        return settings;
    }

    public void setObjectsVisible(DBObjectType objectType, boolean state) {
        if (settings.setObjectVisibility(objectType, state)) {
            updateTree();
        }
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        if (settings.getConnectionHandler() != connectionHandler) {
            settings.setConnectionHandler(connectionHandler);
            if (settings.getSchema() != null) {
                DBSchema schema  = connectionHandler.getObjectBundle().getSchema(settings.getSchema().getName());
                setSchema(schema);
            }
            updateTree();
        }
    }

    public void setSchema(final DBSchema schema) {
        if (settings.getSchema() != schema) {
            settings.setSchema(schema);
            updateTree();
        }
    }

    public void addTreeSelectionListener(TreeSelectionListener selectionListener) {
        methodsTree.addTreeSelectionListener(selectionListener);
    }

    public DBMethod getSelectedMethod() {
        TreePath selectionPath = methodsTree.getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof DBObjectRef) {
                DBObjectRef objectRef = (DBObjectRef) userObject;
                DBObject object = DBObjectRef.get(objectRef);
                if (object instanceof DBMethod) {
                    return (DBMethod) object;
                }
            }
        }
        return null;
    }

    void updateTree() {
        BackgroundTask backgroundTask = new BackgroundTask(project, "Loading executable components", false) {
            @Override
            public void execute(@NotNull ProgressIndicator progressIndicator) {
                initProgressIndicator(progressIndicator, true);
                final ObjectTreeModel model = new ObjectTreeModel(settings.getSchema(), settings.getVisibleObjectTypes(), null);
                new SimpleLaterInvocator() {
                    public void execute() {
                        methodsTree.setModel(model);
                        methodsTree.repaint();
                    }
                }.start();

            }
        };
        backgroundTask.start();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
    }

    private void createUIComponents() {
        methodsTree = new ObjectTree();
    }
}
