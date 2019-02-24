package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeUtil {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    public static void applySpeedSearchHighlighting(
            @NotNull JComponent tree,
            @NotNull SimpleColoredComponent coloredComponent,
            boolean mainTextOnly,
            boolean selected) {
        try {
            SpeedSearchUtil.applySpeedSearchHighlighting(tree, coloredComponent, true, selected);
        } catch (Throwable e) {
            LOGGER.warn("Error applying speed search highlighting");
        }
    }

    public static TreePath createTreePath(TreeNode treeNode) {
        List<TreeNode> list =  new ArrayList<TreeNode>();
        list.add(treeNode);
        TreeNode parent = treeNode.getParent();
        while (parent != null) {
            list.add(0, parent);
            parent = parent.getParent();
        }
        return new TreePath(list.toArray());
    }

    public static void collapseAll(JTree tree) {
        int row = tree.getRowCount() - 1;
        while (row >= 0) {
            tree.collapseRow(row);
            row--;
        }
    }

    public static void expandAll(JTree tree) {
        Object root = tree.getModel().getRoot();
        if (root != null) {
            tree.expandPath(new TreePath(root));
            int oldRowCount = 0;
            do {
                int rowCount = tree.getRowCount();
                if (rowCount == oldRowCount) break;
                oldRowCount = rowCount;
                for (int i = 0; i < rowCount; i++) {
                    tree.expandRow(i);
                }
            }
            while (true);
        }
    }

    public static void notifyTreeModelListeners(Object source, Set<TreeModelListener> treeModelListeners, @Nullable TreePath path, TreeEventType eventType) {
        if (path != null) {
            TreeModelEvent event = new TreeModelEvent(source, path);
            notifyTreeModelListeners(treeModelListeners, eventType, event);
        }
    }

    private static void notifyTreeModelListeners(final Set<TreeModelListener> treeModelListeners, final TreeEventType eventType, final TreeModelEvent event) {
        Dispatch.invoke(() -> {
            Object lastPathComponent = event.getTreePath().getLastPathComponent();
            if (lastPathComponent != null) {
                for (TreeModelListener treeModelListener : treeModelListeners) {
                    switch (eventType) {
                        case NODES_ADDED:       treeModelListener.treeNodesInserted(event);    break;
                        case NODES_REMOVED:     treeModelListener.treeNodesRemoved(event);     break;
                        case NODES_CHANGED:     treeModelListener.treeNodesChanged(event);     break;
                        case STRUCTURE_CHANGED: treeModelListener.treeStructureChanged(event); break;
                    }
                }
            }
        });
    }

    public static TreePath getPathAtMousePosition(JTree tree) {
        Point location = MouseInfo.getPointerInfo().getLocation();
        return tree.getPathForLocation((int) location.getX(), (int) location.getY());
    }
}
