package com.dci.intellij.dbn.browser.model;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.dispose.RegisteredDisposable;
import com.dci.intellij.dbn.common.load.LoadInProgressRegistry;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.project.Project;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.Set;

@Nullifiable
public abstract class BrowserTreeModel extends DisposableBase implements TreeModel, RegisteredDisposable {

    private Set<TreeModelListener> treeModelListeners = new HashSet<>();
    private BrowserTreeNode root;

    private LoadInProgressRegistry<LoadInProgressTreeNode> loadInProgressRegistry =
            LoadInProgressRegistry.create(this,
                    node -> BrowserTreeModel.this.notifyListeners(node, TreeEventType.NODES_CHANGED));

    BrowserTreeModel(BrowserTreeNode root) {
        this.root = root;
        Project project = root.getProject();
        EventUtil.subscribe(project, this, BrowserTreeEventListener.TOPIC, browserTreeEventListener);
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
        if (Failsafe.check(this) && Failsafe.check(treeNode)) {
            TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
            TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
        }
    }

    public Project getProject() {
        return getRoot().getProject();
    }

    public abstract boolean contains(BrowserTreeNode node);


    /***************************************
     *              TreeModel              *
     ***************************************/
    @Override
    public BrowserTreeNode getRoot() {
        return Failsafe.get(root);
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
        return ((BrowserTreeNode) parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((BrowserTreeNode) node).isLeaf();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((BrowserTreeNode) parent).getIndex((BrowserTreeNode) child);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public void disposeInner() {
        Disposer.dispose(root);
        super.disposeInner();
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private BrowserTreeEventListener browserTreeEventListener = new BrowserTreeEventAdapter() {
        @Override
        public void nodeChanged(BrowserTreeNode node, TreeEventType eventType) {
            if (contains(node)) {
                notifyListeners(node, eventType);
            }
        }
    };
}
