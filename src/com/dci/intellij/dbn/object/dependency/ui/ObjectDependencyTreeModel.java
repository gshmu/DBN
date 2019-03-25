package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.ui.tree.TreeEventType;
import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Nullifiable
public class ObjectDependencyTreeModel extends DisposableBase implements TreeModel, Disposable{
    private Set<TreeModelListener> listeners = new HashSet<>();
    private ObjectDependencyTreeNode root;
    private ObjectDependencyType dependencyType;
    private ObjectDependencyTree tree;
    private DBObjectRef<DBSchemaObject> objectRef;


    ObjectDependencyTreeModel(DBSchemaObject object, ObjectDependencyType dependencyType) {
        this.objectRef = DBObjectRef.from(object);
        this.root = new ObjectDependencyTreeNode(this, object);
        this.dependencyType = dependencyType;
    }

    public DBSchemaObject getObject() {
        return DBObjectRef.get(objectRef);
    }

    public void setTree(ObjectDependencyTree tree) {
        this.tree = tree;
    }

    public ObjectDependencyTree getTree() {
        return tree;
    }

    @Nullable
    public Project getProject() {
        return tree.getProject();
    }

    ObjectDependencyType getDependencyType() {
        return dependencyType;
    }

    @Override
    @NotNull
    public ObjectDependencyTreeNode getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        List<ObjectDependencyTreeNode> children = getChildren(parent);
        if (children.size() <= index) throw AlreadyDisposedException.INSTANCE;
        return children.get(index);
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
        return parentNode.getChildren(true);
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

    @Override
    public void disposeInner() {
        Disposer.dispose(root);
        super.disposeInner();
    }

    void refreshLoadInProgressNode(ObjectDependencyTreeNode node) {
        TreePath treePath = new TreePath(node.getTreePath());
        TreeUtil.notifyTreeModelListeners(node, listeners, treePath, TreeEventType.STRUCTURE_CHANGED);
    }

    void notifyNodeLoaded(ObjectDependencyTreeNode node) {
        TreePath treePath = new TreePath(node.getTreePath());
        TreeUtil.notifyTreeModelListeners(node, listeners, treePath, TreeEventType.STRUCTURE_CHANGED);
    }
}
