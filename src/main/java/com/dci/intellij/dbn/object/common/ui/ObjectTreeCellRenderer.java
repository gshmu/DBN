package com.dci.intellij.dbn.object.common.ui;

import com.dci.intellij.dbn.common.ui.tree.Trees;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

public class ObjectTreeCellRenderer extends ColoredTreeCellRenderer {
    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        guarded(() -> {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof DBObjectRef) {
                DBObjectRef<?> objectRef = (DBObjectRef) userObject;
                append(objectRef.getObjectName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

                DBObject object = DBObjectRef.get(objectRef);
                setIcon(object == null ? objectRef.getObjectType().getIcon() : object.getOriginalIcon());

                if (object instanceof DBMethod) {
                    DBMethod method = (DBMethod) object;
                    if (method.getOverload() > 0) {
                        append(" #" + method.getOverload(), SimpleTextAttributes.GRAY_ATTRIBUTES);
                    }
                }

            } else {
                append(userObject.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            Trees.applySpeedSearchHighlighting(tree, this, true, selected);
        });
    }
}
