package com.dci.intellij.dbn.execution.method.browser.ui;

import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.action.SelectConnectionComboBoxAction;
import com.dci.intellij.dbn.execution.method.browser.action.SelectSchemaComboBoxAction;
import com.dci.intellij.dbn.execution.method.browser.action.ShowObjectTypeToggleAction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.ui.ObjectTree;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class MethodExecutionBrowserForm extends DBNFormImpl<MethodExecutionBrowserDialog> {

    private JPanel actionsPanel;
    private JPanel mainPanel;
    private DBNTree methodsTree;

    private MethodBrowserSettings settings;

    MethodExecutionBrowserForm(MethodExecutionBrowserDialog parentComponent, ObjectTreeModel model, boolean debug) {
        super(parentComponent);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new SelectConnectionComboBoxAction(this, debug),
                new SelectSchemaComboBoxAction(this),
                ActionUtil.SEPARATOR,
                new ShowObjectTypeToggleAction(this, DBObjectType.PROCEDURE),
                new ShowObjectTypeToggleAction(this, DBObjectType.FUNCTION));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        methodsTree.setModel(model);
        TreePath selectionPath = model.getInitialSelection();
        if (selectionPath != null) {
            methodsTree.setSelectionPath(selectionPath);
            methodsTree.scrollPathToVisible(selectionPath.getParentPath());
        }
        Disposer.register(this, methodsTree);
    }

    public MethodBrowserSettings getSettings() {
        MethodExecutionManager methodExecutionManager = MethodExecutionManager.getInstance(getProject());
        return methodExecutionManager.getBrowserSettings();
    }

    public void setObjectsVisible(DBObjectType objectType, boolean state) {
        if (getSettings().setObjectVisibility(objectType, state)) {
            updateTree();
        }
    }

    public void setConnectionHandler(ConnectionHandler connectionHandler) {
        MethodBrowserSettings settings = getSettings();
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
        MethodBrowserSettings settings = getSettings();
        if (settings.getSchema() != schema) {
            settings.setSchema(schema);
            updateTree();
        }
    }

    void addTreeSelectionListener(TreeSelectionListener selectionListener) {
        methodsTree.addTreeSelectionListener(selectionListener);
    }

    DBMethod getSelectedMethod() {
        TreePath selectionPath = methodsTree.getSelectionPath();
        if (selectionPath != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
            Object userObject = node.getUserObject();
            if (userObject instanceof DBObjectRef) {
                DBObjectRef<?> objectRef = (DBObjectRef) userObject;
                DBObject object = DBObjectRef.get(objectRef);
                if (object instanceof DBMethod) {
                    return (DBMethod) object;
                }
            }
        }
        return null;
    }

    private void updateTree() {
        BackgroundTask.invoke(getProject(),
                instructions("Loading executable components"),
                (data, progress) -> {
                    MethodBrowserSettings settings = getSettings();
                    ObjectTreeModel model = new ObjectTreeModel(settings.getSchema(), settings.getVisibleObjectTypes(), null);
                    SimpleLaterInvocator.invoke(this, () -> {
                        methodsTree.setModel(model);
                        GUIUtil.repaint(methodsTree);
                    });
                });
    }

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void createUIComponents() {
        methodsTree = new ObjectTree();
    }
}
