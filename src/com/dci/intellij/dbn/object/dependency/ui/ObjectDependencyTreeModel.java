package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.intellij.openapi.project.Project;

public class ObjectDependencyTreeModel implements TreeModel, Disposable{
    private Set<TreeModelListener> listeners = new HashSet<TreeModelListener>();
    private ObjectDependencyTreeNode root;
    private ObjectDependencyType dependencyType;
    private Project project;


    public ObjectDependencyTreeModel(Project project, DBSchemaObject schemaObject, ObjectDependencyType dependencyType) {
        this.project = project;
        this.root = new ObjectDependencyTreeNode(this, schemaObject);
        this.dependencyType = dependencyType;
    }

    public Project getProject() {
        return project;
    }

    public ObjectDependencyType getDependencyType() {
        return dependencyType;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return getChildren(parent).get(index);
    }

    @Override
    public int getChildCount(Object parent) {
        return getChildren(parent).size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return getChildren(parent).indexOf(child);
    }

    private List<ObjectDependencyTreeNode> getChildren(Object parent) {
        ObjectDependencyTreeNode parentNode = (ObjectDependencyTreeNode) parent;
        return parentNode.getChildren();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        DisposerUtil.dispose(root);
        project = null;
    }

    public void notifyNodeLoaded(ObjectDependencyTreeNode node) {
        TreePath treePath = new TreePath(node.getTreePath());
        TreeUtil.notifyTreeModelListeners(node, listeners, treePath, TreeEventType.STRUCTURE_CHANGED);
    }
}
