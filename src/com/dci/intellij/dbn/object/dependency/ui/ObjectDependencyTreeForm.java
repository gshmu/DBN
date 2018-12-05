package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
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
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;

public class ObjectDependencyTreeForm extends DBNFormImpl<ObjectDependencyTreeDialog>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private ComboBox<ObjectDependencyType> dependencyTypeComboBox;
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


        initComboBox(dependencyTypeComboBox, ObjectDependencyType.values());
        setSelection(dependencyTypeComboBox, dependencyType);
        dependencyTypeComboBox.addActionListener(e -> {
            ObjectDependencyType selection = getSelection(dependencyTypeComboBox);
            dependencyTree.setDependencyType(selection);
        });

        this.objectRef = DBObjectRef.from(schemaObject);
        headerForm = new DBNHeaderForm(schemaObject, this);
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

        ExpandTreeAction() {
            super("Expand All", null, Icons.ACTION_EXPAND_ALL);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            TreeUtil.expandAll(dependencyTree);
        }

        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Expand All");
        }
    }

    public class PreviousSelectionAction extends DumbAwareAction {
        PreviousSelectionAction() {
            super("Previous Selection", null, Icons.BROWSER_BACK);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            DBObject previous = dependencyTree.getSelectionHistory().previous();
            dependencyTree.setRootObject((DBSchemaObject) previous, false);
        }

        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(dependencyTree.getSelectionHistory().hasPrevious());
        }
    }

    public class NextSelectionAction extends DumbAwareAction {
        NextSelectionAction() {
            super("Next Selection", null, Icons.BROWSER_NEXT);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            DBObject next = dependencyTree.getSelectionHistory().next();
            dependencyTree.setRootObject((DBSchemaObject) next, false);
        }

        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(dependencyTree.getSelectionHistory().hasNext());
        }
    }

    public class CollapseTreeAction extends DumbAwareAction {

        CollapseTreeAction() {
            super("Collapse All", null, Icons.ACTION_COLLAPSE_ALL);
        }

        public void actionPerformed(@NotNull AnActionEvent e) {
            TreeUtil.collapseAll(dependencyTree);
        }

        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Collapse All");
        }
    }
}
