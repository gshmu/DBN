package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class ObjectDependencyTreeForm extends DBNFormImpl<ObjectDependencyTreeDialog>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JTree dependencyTree;

    private DBObjectRef<DBSchemaObject> objectRef;

    public ObjectDependencyTreeForm(ObjectDependencyTreeDialog parentComponent, DBSchemaObject schemaObject) {
        super(parentComponent);
        this.objectRef = DBObjectRef.from(schemaObject);
        DBNHeaderForm headerForm = new DBNHeaderForm(schemaObject);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
        dependencyTree.setModel(new ObjectDependencyTreeModel(getProject(), schemaObject, ObjectDependencyType.OUTGOING));
        dependencyTree.setCellRenderer(new TreeCellRenderer());
    }

    private class TreeCellRenderer extends ColoredTreeCellRenderer {

        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) value;
            DBObject object = node.getObject();
            if (object != null) {
                setIcon(object.getIcon());
                append(object.getQualifiedName());
            } else {
                append("Loading...", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
            }
        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
