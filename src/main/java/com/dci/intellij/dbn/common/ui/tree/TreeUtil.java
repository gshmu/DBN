package com.dci.intellij.dbn.common.ui.tree;

import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.util.Listeners;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
public final class TreeUtil {
    public static void applySpeedSearchHighlighting(
            @NotNull JComponent tree,
            @NotNull SimpleColoredComponent coloredComponent,
            boolean mainTextOnly,
            boolean selected) {
        try {
            SpeedSearchUtil.applySpeedSearchHighlighting(tree, coloredComponent, true, selected);
        } catch (Throwable e) {
            log.warn("Error applying speed search highlighting");
        }
    }

    public static TreePath createTreePath(TreeNode treeNode) {
        List<TreeNode> list = new ArrayList<>();
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

    public static void notifyTreeModelListeners(Object source, Set<TreeModelListener> listeners, @Nullable TreePath path, TreeEventType eventType) {
        if (path == null) return;

        TreeModelEvent event = new TreeModelEvent(source, path);
        notifyTreeModelListeners(listeners, eventType, event);
    }

    private static void notifyTreeModelListeners(Set<TreeModelListener> listeners, final TreeEventType eventType, final TreeModelEvent event) {
        Dispatch.run(() -> {
            try {
                Object lastPathComponent = event.getTreePath().getLastPathComponent();
                if (lastPathComponent == null) return;

                Listeners.notify(listeners, l -> {
                    switch (eventType) {
                        case NODES_ADDED:       l.treeNodesInserted(event);    break;
                        case NODES_REMOVED:     l.treeNodesRemoved(event);     break;
                        case NODES_CHANGED:     l.treeNodesChanged(event);     break;
                        case STRUCTURE_CHANGED: l.treeStructureChanged(event); break;
                    }
                });
            } catch (IndexOutOfBoundsException ignore) {
                // tree may have mutated already
            }
        });
    }

    public static TreePath getPathAtMousePosition(JTree tree) {
        Point location = MouseInfo.getPointerInfo().getLocation();
        return tree.getPathForLocation((int) location.getX(), (int) location.getY());
    }
}
