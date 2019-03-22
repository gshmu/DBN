package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.intellij.openapi.Disposable;
import com.intellij.ui.SpeedSearchBase;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBrowserTreeSpeedSearch extends SpeedSearchBase<JTree> implements Disposable {
    private static final Object[] EMPTY_ARRAY = new Object[0];
    private Object[] elements = null;

    DatabaseBrowserTreeSpeedSearch(DatabaseBrowserTree tree) {
        super(tree);
        getComponent().getModel().addTreeModelListener(treeModelListener);
    }

    @Override
    protected int getSelectedIndex() {
        Object[] elements = getAllElements();
        BrowserTreeNode treeNode = getSelectedTreeElement();
        if (treeNode != null) {
            for (int i=0; i<elements.length; i++) {
                if (treeNode == elements[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private BrowserTreeNode getSelectedTreeElement() {
        TreePath selectionPath = getComponent().getSelectionPath();
        if (selectionPath != null) {
            return (BrowserTreeNode) selectionPath.getLastPathComponent();
        }
        return null;
    }

    @NotNull
    @Override
    protected Object[] getAllElements() {
        if (elements == null) {
            List<BrowserTreeNode> nodes = new ArrayList<>();
            BrowserTreeNode root = getComponent().getModel().getRoot();
            loadElements(nodes, root);
            this.elements = nodes.toArray();
        }
        return elements;
    }

    private static void loadElements(List<BrowserTreeNode> nodes, BrowserTreeNode browserTreeNode) {
        if (browserTreeNode.isTreeStructureLoaded()) {
            if (browserTreeNode instanceof ConnectionBundle) {
                ConnectionBundle connectionBundle = (ConnectionBundle) browserTreeNode;
                for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()){
                    DBObjectBundle objectBundle = connectionHandler.getObjectBundle();
                    loadElements(nodes, objectBundle);
                }
            }
            else {
                for (BrowserTreeNode treeNode : browserTreeNode.getChildren()) {
                    if (treeNode instanceof DBObject) {
                        nodes.add(treeNode);
                    }
                    loadElements(nodes, treeNode);
                }
            }
        }
    }

    @Override
    public DatabaseBrowserTree getComponent() {
        return (DatabaseBrowserTree) super.getComponent();
    }

    @Override
    protected String getElementText(Object o) {
        BrowserTreeNode treeNode = (BrowserTreeNode) o;
        return treeNode.getPresentableText();
    }

    @Override
    protected void selectElement(Object o, String s) {
        BrowserTreeNode treeNode = (BrowserTreeNode) o;
        getComponent().selectElement(treeNode, false);

/*
        TreePath treePath = DatabaseBrowserUtils.createTreePath(treeNode);
        tree.setSelectionPath(treePath);
        tree.scrollPathToVisible(treePath);
*/
    }

    private TreeModelListener treeModelListener = new TreeModelListener() {

        @Override
        public void treeNodesChanged(TreeModelEvent e) {
            elements = null;
        }

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            elements = null;
        }

        @Override
        public void treeNodesRemoved(TreeModelEvent e) {
            elements = null;
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            elements = null;
        }
    };

    @Override
    public void dispose() {
        getComponent().getModel().removeTreeModelListener(treeModelListener);
        Disposer.nullify(this);
        elements = EMPTY_ARRAY;
    }
}
