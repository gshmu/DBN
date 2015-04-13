package com.dci.intellij.dbn.browser.model;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.HashSet;
import gnu.trove.THashSet;

public abstract class BrowserTreeModel implements TreeModel, Disposable {
    private Set<TreeModelListener> treeModelListeners = new HashSet<TreeModelListener>();
    private BrowserTreeNode root;
    private boolean isDisposed  = false;
    private final Set<LoadInProgressTreeNode> loadInProgressNodes = new THashSet<LoadInProgressTreeNode>();

    protected BrowserTreeModel(BrowserTreeNode root) {
        this.root = root;
        EventUtil.subscribe(root.getProject(), this, BrowserTreeChangeListener.TOPIC, browserTreeChangeListener);
    }

    public void addTreeModelListener(TreeModelListener listener) {
        treeModelListeners.add(listener);
    }

    public void removeTreeModelListener(TreeModelListener listener) {
        treeModelListeners.remove(listener);
    }

    public void notifyListeners(final BrowserTreeNode treeNode, final TreeEventType eventType) {
        if (!isDisposed && !treeNode.isDisposed()) {
            TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
            TreeUtil.notifyTreeModelListeners(this, treeModelListeners, treePath, eventType);
        }
    }

    public Project getProject() {
        return getRoot().getProject();
    }

    public abstract boolean contains(BrowserTreeNode node);

    /****************************************************
     *              LoadInProgress handling             *
     ****************************************************/

    private void registerLoadInProgressNode(LoadInProgressTreeNode node) {
        synchronized (loadInProgressNodes) {
            boolean startTimer = loadInProgressNodes.size() == 0;
            loadInProgressNodes.add(node);
            if (startTimer) {
                Timer reloader = new Timer("DBN - Database Browser (load in progress reload timer)");
                reloader.schedule(new LoadInProgressRefreshTask(), 0, 50);
            }
        }
    }

    private class LoadInProgressRefreshTask extends TimerTask {
        int iterations = 0;
        public void run() {
            synchronized (loadInProgressNodes) {
                Iterator<LoadInProgressTreeNode> loadInProgressNodesIterator = loadInProgressNodes.iterator();
                while (loadInProgressNodesIterator.hasNext()) {
                    LoadInProgressTreeNode loadInProgressTreeNode = loadInProgressNodesIterator.next();
                    try {
                        if (loadInProgressTreeNode.isDisposed()) {
                            loadInProgressNodesIterator.remove();
                        } else {
                            notifyListeners(loadInProgressTreeNode, TreeEventType.NODES_CHANGED);
                        }
                    } catch (ProcessCanceledException e) {
                        loadInProgressNodesIterator.remove();
                    }
                }

                if (loadInProgressNodes.isEmpty()) {
                    cancel();
                }
            }

            iterations++;
        }
    }



    /***************************************
     *              TreeModel              *
     ***************************************/
    public BrowserTreeNode getRoot() {
        return FailsafeUtil.get(root);
    }

    public Object getChild(Object parent, int index) {
        BrowserTreeNode treeChild = ((BrowserTreeNode) parent).getTreeChild(index);
        if (treeChild instanceof LoadInProgressTreeNode) {
            registerLoadInProgressNode((LoadInProgressTreeNode) treeChild);
        }
        return treeChild;
    }

    public int getChildCount(Object parent) {
        return ((BrowserTreeNode) parent).getTreeChildCount();
    }

    public boolean isLeaf(Object node) {
        return ((BrowserTreeNode) node).isLeafTreeElement();
    }

    public int getIndexOfChild(Object parent, Object child) {
        return ((BrowserTreeNode) parent).getIndexOfTreeChild((BrowserTreeNode) child);
    }

    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    public void dispose() {
        if (!isDisposed) {
            isDisposed = true;
            treeModelListeners.clear();
            loadInProgressNodes.clear();
            root = null;
        }
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private BrowserTreeChangeListener browserTreeChangeListener = new BrowserTreeChangeListener() {
        @Override
        public void nodeChanged(BrowserTreeNode node, TreeEventType eventType) {
            if (contains(node)) {
                notifyListeners(node, eventType);
            }
        }
    };



}
