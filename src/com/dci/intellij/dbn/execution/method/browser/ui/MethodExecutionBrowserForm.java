package com.dci.intellij.dbn.execution.method.browser.ui;

import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.method.browser.MethodBrowserSettings;
import com.dci.intellij.dbn.execution.method.browser.action.ConnectionSelectDropdownAction;
import com.dci.intellij.dbn.execution.method.browser.action.ObjectTypeToggleAction;
import com.dci.intellij.dbn.execution.method.browser.action.SchemaSelectDropdownAction;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.ui.ObjectTree;
import com.dci.intellij.dbn.object.common.ui.ObjectTreeModel;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.ActionToolbar;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;

public class MethodExecutionBrowserForm extends DBNFormImpl {

    private JPanel actionsPanel;
    private JPanel mainPanel;
    private DBNTree methodsTree;

    MethodExecutionBrowserForm(MethodExecutionBrowserDialog parent, ObjectTreeModel model, boolean debug) {
        super(parent);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(actionsPanel,"", true,
                new ConnectionSelectDropdownAction(this, debug),
                new SchemaSelectDropdownAction(this),
                ActionUtil.SEPARATOR,
                new ObjectTypeToggleAction(this, DBObjectType.PROCEDURE),
                new ObjectTypeToggleAction(this, DBObjectType.FUNCTION));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        methodsTree.setModel(model);
        TreePath selectionPath = model.getInitialSelection();
        if (selectionPath != null) {
            methodsTree.setSelectionPath(selectionPath);
            methodsTree.scrollPathToVisible(selectionPath.getParentPath());
        }
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
                DBObjectRef<?> objectRef = (DBObjectRef<?>) userObject;
                DBObject object = DBObjectRef.get(objectRef);
                if (object instanceof DBMethod) {
                    return (DBMethod) object;
                }
            }
        }
        return null;
    }

    private void updateTree() {
        Progress.prompt(getProject(), "Loading executable components", false,
                (progress) -> {
                    MethodBrowserSettings settings = getSettings();
                    ObjectTreeModel model = new ObjectTreeModel(settings.getSchema(), settings.getVisibleObjectTypes(), null);
                    Dispatch.run(() -> {
                        methodsTree.setModel(model);
                        GUIUtil.repaint(methodsTree);
                    });
                });
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    private void createUIComponents() {
        methodsTree = new ObjectTree(this);
    }
}
