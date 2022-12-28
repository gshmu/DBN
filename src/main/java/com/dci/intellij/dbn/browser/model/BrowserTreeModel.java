package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.load.LoadInProgressRegistry;
import com.dci.intellij.dbn.common.ref.WeakRef;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.ui.util.Listeners;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import static com.dci.intellij.dbn.common.dispose.Checks.allValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

public abstract class BrowserTreeModel extends StatefulDisposableBase implements TreeModel, StatefulDisposable {

    private final Listeners<TreeModelListener> listeners = Listeners.create(this);
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
        listeners.add(listener);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(BrowserTreeNode treeNode, final TreeEventType eventType) {
        if (allValid(this, treeNode)) {
            TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
            TreeUtil.notifyTreeModelListeners(this, listeners, treePath, eventType);
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
        return guarded(0, () -> ((BrowserTreeNode) parent).getChildCount());
    }

    @Override
    public boolean isLeaf(Object node) {
        return guarded(true, () -> ((BrowserTreeNode) node).isLeaf());
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return guarded(-1, () -> ((BrowserTreeNode) parent).getIndex((BrowserTreeNode) child));
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public void disposeInner() {
        nullify();
    }


}
