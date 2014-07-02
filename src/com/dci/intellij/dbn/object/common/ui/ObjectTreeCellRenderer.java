package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

public class ObjectTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
        Object userObject = treeNode.getUserObject();
        if (userObject instanceof DBObject) {
            DBObject object = (DBObject) userObject;
            append(object.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(object.getOriginalIcon());
        } else {
            append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }
}
