package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectDependencyTreeNode extends StatefulDisposable.Base implements StatefulDisposable {
    private final DBObjectRef<DBObject> object;

    private List<ObjectDependencyTreeNode> dependencies;
    private ObjectDependencyTreeModel model;
    private ObjectDependencyTreeNode parent;
    private boolean shouldLoad = true;
    private boolean isLoading = false;
    private static int loaderCount = 0;

    private ObjectDependencyTreeNode(ObjectDependencyTreeNode parent, DBObject object) {
        this.parent = parent;
        this.object = DBObjectRef.of(object);
    }

    ObjectDependencyTreeNode(ObjectDependencyTreeModel model, DBObject object) {
        this.model = model;
        this.object = DBObjectRef.of(object);
    }

    @Nullable
    DBObject getObject() {
        return DBObjectRef.get(object);
    }

    public ObjectDependencyTreeModel getModel() {
        if (model == null && parent == null) {
            throw AlreadyDisposedException.INSTANCE;
        }
        return model == null ? getParent().getModel() : model;
    }

    public ObjectDependencyTreeNode getParent() {
        return parent;
    }

    public synchronized List<ObjectDependencyTreeNode> getChildren(final boolean load) {
        final ObjectDependencyTreeModel model = getModel();
        if (object == null || model == null)  {
            return Collections.emptyList();
        }

        if (dependencies == null && load) {
            DBObject object = getObject();
            if (isDisposed() || object == null || isRecursive(object)) {
                dependencies = Collections.emptyList();
                shouldLoad = false;
            } else {
                dependencies = new ArrayList<>();
                if (getTreePath().length < 2) {
                    ObjectDependencyTreeNode loadInProgressNode = new ObjectDependencyTreeNode(this, null);
                    dependencies.add(loadInProgressNode);
                    model.getTree().registerLoadInProgressNode(loadInProgressNode);
                }
            }
        }

        if (load && shouldLoad) {
            isLoading = true;

            if (loaderCount < 10) {
                shouldLoad = false;
                loaderCount++;
                Background.run(() -> {
                    try {
                        DBObject object = getObject();
                        if (object instanceof DBSchemaObject) {
                            List<ObjectDependencyTreeNode> newDependencies = new ArrayList<>();
                            DBSchemaObject schemaObject = (DBSchemaObject) object;
                            List<DBObject> dependentObjects = loadDependencies(schemaObject);

                            if (dependentObjects != null) {
                                for (DBObject dependentObject : dependentObjects) {
                                        /*if (dependentObject instanceof DBSchemaObject) {
                                            loadDependencies((DBSchemaObject) dependentObject);
                                        }*/
                                    ObjectDependencyTreeNode node = new ObjectDependencyTreeNode(ObjectDependencyTreeNode.this, dependentObject);
                                    newDependencies.add(node);
                                }
                            }

                            List<ObjectDependencyTreeNode> oldDependencies = dependencies;
                            dependencies = newDependencies;
                            SafeDisposer.dispose(oldDependencies, false, true);

                            getModel().notifyNodeLoaded(ObjectDependencyTreeNode.this);
                        }
                    } finally {
                        isLoading = false;
                        loaderCount--;
                    }
                });
            }
        }
        return dependencies;
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Nullable
    private List<DBObject> loadDependencies(DBSchemaObject schemaObject) {
        ObjectDependencyType dependencyType = getModel().getDependencyType();
        return
            dependencyType == ObjectDependencyType.INCOMING ? schemaObject.getReferencedObjects() :
            dependencyType == ObjectDependencyType.OUTGOING ? schemaObject.getReferencingObjects() : null;
    }

    private boolean isRecursive(DBObject object) {
        if (object != null) {
            ObjectDependencyTreeNode parent = getParent();
            while (parent != null) {
                if (object.equals(parent.getObject())) {
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;
    }

    public ObjectDependencyTreeNode[] getTreePath() {
        List<ObjectDependencyTreeNode> path = new ArrayList<>();
        path.add(this);
        ObjectDependencyTreeNode parent = getParent();
        while (parent != null) {
            path.add(0, parent);
            parent = parent.getParent();
        }
        return path.toArray(new ObjectDependencyTreeNode[0]);
    }

    @Override
    public void disposeInner() {
        SafeDisposer.dispose(dependencies, false, false);
        nullify();
    }
}
