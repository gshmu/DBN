package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.load.LoadInProgressRegistry;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectSelectionHistory;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ObjectDependencyTree extends DBNTree implements Disposable{
    private DBObjectSelectionHistory selectionHistory =  new DBObjectSelectionHistory();
    private ObjectDependencyTreeSpeedSearch speedSearch;
    private Project project;

    private LoadInProgressRegistry<ObjectDependencyTreeNode> loadInProgressRegistry =
            LoadInProgressRegistry.create(this,
                    node -> getModel().refreshLoadInProgressNode(node));

    ObjectDependencyTree(Project project, DBSchemaObject schemaObject) {
        setModel(createModel(project, schemaObject));
        this.project = project;
        selectionHistory.add(schemaObject);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        setCellRenderer(new ObjectDependencyTreeCellRenderer());
        addTreeSelectionListener((TreeSelectionEvent e) -> GUIUtil.repaint(ObjectDependencyTree.this));

        speedSearch = new ObjectDependencyTreeSpeedSearch(this);

        Disposer.register(this, selectionHistory);
        Disposer.register(this, speedSearch);


        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
            }

            @Override
            public void mouseReleased(final MouseEvent event) {
                if (false && event.getButton() == MouseEvent.BUTTON3) {
                    final TreePath path = getPathForLocation(event.getX(), event.getY());
                    if (path != null) {
                        final ObjectDependencyTreeNode node = (ObjectDependencyTreeNode) path.getLastPathComponent();
                        DefaultActionGroup actionGroup = new DefaultActionGroup();
                        if (node != null) {
                            ObjectDependencyTreeNode rootNode = node.getModel().getRoot();
                            DBObject object = node.getObject();
                            if (object instanceof DBSchemaObject && !CommonUtil.safeEqual(rootNode.getObject(), object)) {
                                actionGroup.add(new SelectObjectAction((DBSchemaObject) object));
                            }
                        }

                        ActionPopupMenu actionPopupMenu = ActionManager.getInstance().createActionPopupMenu("", actionGroup);
                        JPopupMenu popupMenu = actionPopupMenu.getComponent();
                        SimpleLaterInvocator.invoke(() -> {
                            if (isShowing()) {
                                popupMenu.show(ObjectDependencyTree.this, event.getX(), event.getY());
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void setModel(TreeModel model) {
        if (model instanceof ObjectDependencyTreeModel) {
            ObjectDependencyTreeModel treeModel = (ObjectDependencyTreeModel) model;
            treeModel.setTree(this);
            super.setModel(model);
            Disposer.register(this, treeModel);
        }
    }

    @NotNull
    private static ObjectDependencyTreeModel createModel(Project project, DBSchemaObject schemaObject) {
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        ObjectDependencyType dependencyType = dependencyManager.getLastUserDependencyType();
        return new ObjectDependencyTreeModel(schemaObject, dependencyType);
    }

    private long selectionTimestamp = System.currentTimeMillis();
    @Override
    protected void processMouseEvent(MouseEvent e) {
        int button = e.getButton();
        int clickCount = e.getClickCount();
        if (button == MouseEvent.BUTTON1) {
            if (e.isControlDown()) {
                if (clickCount == 1) {
                    DBObject object = getMouseEventObject(e);
                    if (object != null) {
                        object.navigate(true);
                        e.consume();
                    }
                }
            } else if (clickCount == 2) {
                DBObject object = getMouseEventObject(e);
                if (object != null && TimeUtil.isOlderThan(selectionTimestamp, TimeUtil.ONE_SECOND)) {
                    selectionTimestamp = System.currentTimeMillis();
                    setRootObject((DBSchemaObject) object, true);
                    e.consume();
                }
            }
        }

        if (!e.isConsumed()) {
            super.processMouseEvent(e);
        }
    }

    private DBObject getMouseEventObject(MouseEvent e) {
        TreePath path = getPathForLocation(e.getX(), e.getY());
        Object lastPathComponent = path == null ? null : path.getLastPathComponent();
        if (lastPathComponent instanceof ObjectDependencyTreeNode) {
            ObjectDependencyTreeNode dependencyTreeNode = (ObjectDependencyTreeNode) lastPathComponent;
            return dependencyTreeNode.getObject();
        }
        return null;
    }

    public Project getProject() {
        return project;
    }

    DBObjectSelectionHistory getSelectionHistory() {
        return selectionHistory;
    }

    public void selectElement(ObjectDependencyTreeNode treeNode) {
        TreePath treePath = new TreePath(treeNode.getTreePath());
        TreeUtil.selectPath(this, treePath);
    }

    void registerLoadInProgressNode(ObjectDependencyTreeNode loadInProgressNode) {
        loadInProgressRegistry.register(loadInProgressNode);
    }

    public class SelectObjectAction extends DumbAwareAction {
        private DBObjectRef<DBSchemaObject> objectRef;
        SelectObjectAction(DBSchemaObject object) {
            super("Select");
            objectRef = DBObjectRef.from(object);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DBSchemaObject schemaObject = DBObjectRef.get(objectRef);
            if (schemaObject != null) {
                setRootObject(schemaObject, true);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setText("Select");
        }
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        boolean navigable = false;
        if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && !e.isConsumed()) {
            DBObject object = getMouseEventObject(e);
            if (object != null && !object.equals(getModel().getObject())) {
                navigable = true;
            }
        }

        if (navigable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            super.processMouseMotionEvent(e);
            setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public ObjectDependencyTreeModel getModel() {
        TreeModel model = super.getModel();
        return model instanceof ObjectDependencyTreeModel ? (ObjectDependencyTreeModel) model : null;
    }

    void setDependencyType(ObjectDependencyType dependencyType) {
        ObjectDependencyTreeModel oldModel = getModel();
        Project project = Failsafe.get(oldModel.getProject());
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        dependencyManager.setLastUserDependencyType(dependencyType);

        DBSchemaObject object = oldModel.getObject();
        if (object != null) {
            setModel(new ObjectDependencyTreeModel(object, dependencyType));
            Disposer.dispose(oldModel);
        }
    }

    void setRootObject(DBSchemaObject object, boolean addHistory) {
        ObjectDependencyTreeModel oldModel = getModel();
        if (addHistory) {
            selectionHistory.add(object);
        }

        ObjectDependencyType dependencyType = oldModel.getDependencyType();
        setModel(new ObjectDependencyTreeModel(object, dependencyType));
        Disposer.dispose(oldModel);
    }

    @Override
    public void dispose() {
        super.dispose();
        speedSearch = null;
        selectionHistory = null;
        project = null;
    }


}
