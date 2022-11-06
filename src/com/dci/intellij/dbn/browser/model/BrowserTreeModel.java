package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.load.LoadInProgressRegistry;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.Cancellable;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;

public abstract class BrowserTreeModel extends StatefulDisposable.Base implements TreeModel, StatefulDisposable {

    private final Set<TreeModelListener> treeModelListeners = new HashSet<>();
    private final WeakRef<BrowserTreeNode> root;

    private final LoadInProgressRegistry<LoadInProgressTreeNode> loadInProgressRegistry =
            LoadInProgressRegistry.create(this,
                    node -> BrowserTreeModel.this.notifyListeners(node, TreeEventType.NODES_CHANGED));

    BrowserTreeModel(BrowserTreeNode root) {
        this.root = WeakRef.of(root);
        ProjectEvents.subscribe(getProject(), this, BrowserTreeEventListener.TOPIC, browserTreeEventListener());
    }

    @NotNull
    private BrowserTreeEventListener browserTreeEventListener() {
        return new BrowserTreeEventListener() {
            @Override
            public void nodeChanged(BrowserTreeNode node, TreeEventType eventType) {
                if (contains(node)) {
                    notifyListeners(node, eventType);
                }
            }
        };
    }

    @Override
    public void addTreeModelListener(TreeModelListener listener) {
        treeModelListeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        treeModelListeners.remove(listener);
    }

    public void notifyListeners(BrowserTreeNode treeNode, final TreeEventType eventType) {
        if (Failsafe.check(this, treeNode)) {
            TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
            TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
        }
    }

    @NotNull
    public Project getProject() {
        return getRoot().getProject();
    }

    public abstract boolean contains(BrowserTreeNode node);


    /***************************************
     *              TreeModel              *
     ***************************************/
    @Override
    public BrowserTreeNode getRoot() {
        return root.ensure();
    }

    @Override
    public Object getChild(Object parent, int index) {
        BrowserTreeNode treeChild = ((BrowserTreeNode) parent).getChildAt(index);
        if (treeChild instanceof LoadInProgressTreeNode) {
            loadInProgressRegistry.register((LoadInProgressTreeNode) treeChild);
        }
        return treeChild;
    }

    @Override
    public int getChildCount(Object parent) {
        return Cancellable.call(0, () -> ((BrowserTreeNode) parent).getChildCount());
    }

    @Override
    public boolean isLeaf(Object node) {
        return Cancellable.call(true, () -> ((BrowserTreeNode) node).isLeaf());
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return Cancellable.call(-1, () -> ((BrowserTreeNode) parent).getIndex((BrowserTreeNode) child));
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public void disposeInner() {
        nullify();
    }


}
