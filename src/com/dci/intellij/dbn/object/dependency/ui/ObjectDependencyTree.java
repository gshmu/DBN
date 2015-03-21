package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyManager;
import com.dci.intellij.dbn.object.dependency.ObjectDependencyType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.tree.TreeUtil;

public class ObjectDependencyTree extends JTree implements Disposable{
    private ObjectDependencyTreeSpeedSearch speedSearch;
    public ObjectDependencyTree(Project project, DBSchemaObject schemaObject) {
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        ObjectDependencyType dependencyType = dependencyManager.getLastUserDependencyType();
        ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, schemaObject, dependencyType);
        setModel(model);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        setCellRenderer(new ObjectDependencyTreeCellRenderer());
        addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                revalidate();
                repaint();
            }
        });

        speedSearch = new ObjectDependencyTreeSpeedSearch(this);
        Disposer.register(this, speedSearch);
        Disposer.register(this, model);


        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                    final TreePath path = getPathForLocation(event.getX(), event.getY());
                    if (path != null) {
                        final ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) path.getLastPathComponent();
                        DBObject object = node.getObject();
                        if (object != null) {
                            event.consume();
                            object.navigate(true);
                        }
                    }
                }
            }

            public void mouseReleased(final MouseEvent event) {
                if (event.getButton() == MouseEvent.BUTTON3) {
                    final TreePath path = getPathForLocation(event.getX(), event.getY());
                    if (path != null) {
                        final ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) path.getLastPathComponent();
                        DefaultActionGroup actionGroup = new DefaultActionGroup();
                        if (node != null) {
                            ObjectDependencyTreeNode rootNode = (ObjectDependencyTreeNode) node.getModel().getRoot();
                            DBObject object = node.getObject();
                            if (object instanceof DBSchemaObject && !CommonUtil.safeEqual(rootNode.getObject(), object)) {
                                actionGroup.add(new SelectObjectAction((DBSchemaObject) object));
                            }
                        }

                        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                        final JPopupMenu popupMenu = actionPopupMenu.getComponent();
                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                popupMenu.show(ObjectDependencyTree.this, event.getX(), event.getY());
                            }
                        }.start();
                    }
                }
            }
        });
    }

    public void selectElement(ObjectDependencyTreeNode treeNode) {
        TreePath treePath = new TreePath(treeNode.getTreePath());
        TreeUtil.selectPath(this, treePath);
    }

    public class SelectObjectAction extends DumbAwareAction {
        private DBObjectRef<DBSchemaObject> objectRef;
        public SelectObjectAction(DBSchemaObject object) {
            super("Select");
            objectRef = DBObjectRef.from(object);
        }

        public void actionPerformed(AnActionEvent e) {
            DBSchemaObject schemaObject = DBObjectRef.get(objectRef);
            if (schemaObject != null) {
                setRootObject(schemaObject);
            }
        }

        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Select");
        }
    }

    @Override
    public ObjectDependencyTreeModel getModel() {
        return (ObjectDependencyTreeModel) super.getModel();
    }

    public void setDependencyType(ObjectDependencyType dependencyType) {
        ObjectDependencyManager.getInstance(getModel().getProject()).setLastUserDependencyType(dependencyType);
        ObjectDependencyTreeModel oldModel = getModel();
        DBSchemaObject object = oldModel.getObject();
        Project project = oldModel.getProject();
        if (object != null && project != null && !project.isDisposed()) {
            ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, object, dependencyType);
            setModel(model);
            Disposer.dispose(oldModel);
        }
    }

    public void setRootObject(DBSchemaObject object) {
        ObjectDependencyTreeModel oldModel = getModel();
        ObjectDependencyType dependencyType = oldModel.getDependencyType();
        Project project = oldModel.getProject();
        if (project != null && !project.isDisposed()) {
            ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(project, object, dependencyType);
            setModel(model);
            Disposer.dispose(oldModel);
        }
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        speedSearch = null;
    }


}
