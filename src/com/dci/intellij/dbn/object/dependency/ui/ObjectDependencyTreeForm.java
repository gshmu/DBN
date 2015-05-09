package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.tree.TreeModel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyManager;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;

public class ObjectDependencyTreeForm extends DBNFormImpl<ObjectDependencyTreeDialog>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private DBNComboBox<ObjectDependencyType> dependencyTypeComboBox;
    private JBScrollPane treeScrollPane;


    private DBNHeaderForm headerForm;
    private ObjectDependencyTree dependencyTree;

    private DBObjectRef<DBSchemaObject> objectRef;

    public ObjectDependencyTreeForm(ObjectDependencyTreeDialog parentComponent, final DBSchemaObject schemaObject) {
        super(parentComponent);
        Project project = getProject();
        dependencyTree = new ObjectDependencyTree(project, schemaObject) {
            @Override
            public void setModel(TreeModel model) {
                super.setModel(model);
                if (headerForm != null && model instanceof ObjectDependencyTreeModel) {
                    ObjectDependencyTreeModel dependencyTreeModel = (ObjectDependencyTreeModel) model;
                    ObjectDependencyTreeNode rootNode = dependencyTreeModel.getRoot();
                    DBObject object = rootNode.getObject();
                    if (object != null) {
                        headerForm.update(object);
                    }
                }
            }
        };
        treeScrollPane.setViewportView(dependencyTree);
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        ObjectDependencyType dependencyType = dependencyManager.getLastUserDependencyType();


        dependencyTypeComboBox.setValues(ObjectDependencyType.values());
        dependencyTypeComboBox.setSelectedValue(dependencyType);
        dependencyTypeComboBox.addListener(new ValueSelectorListener<ObjectDependencyType>() {
            @Override
            public void selectionChanged(ObjectDependencyType oldValue, ObjectDependencyType newValue) {
                dependencyTree.setDependencyType(newValue);
            }
        });

        this.objectRef = DBObjectRef.from(schemaObject);
        headerForm = new DBNHeaderForm(schemaObject);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true,
                new PreviousSelectionAction(),
                new NextSelectionAction(),
                ActionUtil.SEPARATOR,
                new ExpandTreeAction(),
                new CollapseTreeAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        Disposer.register(this, dependencyTree);
    }



    private DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public class ExpandTreeAction extends DumbAwareAction {

        public ExpandTreeAction() {
            super("Expand All", null, Icons.ACTION_EXPAND_ALL);
        }

        public void actionPerformed(AnActionEvent e) {
            TreeUtil.expandAll(dependencyTree);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Expand All");
        }
    }

    public class PreviousSelectionAction extends DumbAwareAction {
        public PreviousSelectionAction() {
            super("Previous Selection", null, Icons.BROWSER_BACK);
        }

        public void actionPerformed(AnActionEvent e) {
            DBObject previous = dependencyTree.getSelectionHistory().previous();
            dependencyTree.setRootObject((DBSchemaObject) previous, false);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(dependencyTree.getSelectionHistory().hasPrevious());
        }
    }

    public class NextSelectionAction extends DumbAwareAction {
        public NextSelectionAction() {
            super("Next Selection", null, Icons.BROWSER_NEXT);
        }

        public void actionPerformed(AnActionEvent e) {
            DBObject next = dependencyTree.getSelectionHistory().next();
            dependencyTree.setRootObject((DBSchemaObject) next, false);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(dependencyTree.getSelectionHistory().hasNext());
        }
    }

    public class CollapseTreeAction extends DumbAwareAction {

        public CollapseTreeAction() {
            super("Collapse All", null, Icons.ACTION_COLLAPSE_ALL);
        }

        public void actionPerformed(AnActionEvent e) {
            TreeUtil.collapseAll(dependencyTree);
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Collapse All");
        }
    }
}
