package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
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

public class ObjectDependencyTree extends JTree implements Disposable{
    private final Set<ObjectDependencyTreeNode> loadInProgressNodes = new HashSet<ObjectDependencyTreeNode>();
    private DBObjectSelectionHistory selectionHistory =  new DBObjectSelectionHistory();
    private ObjectDependencyTreeSpeedSearch speedSearch;
    private Project project;

    public ObjectDependencyTree(Project project, DBSchemaObject schemaObject) {
        this.project = project;
        selectionHistory.add(schemaObject);
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        ObjectDependencyType dependencyType = dependencyManager.getLastUserDependencyType();
        ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(this, schemaObject, dependencyType);
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

        Disposer.register(this, selectionHistory);
        Disposer.register(this, speedSearch);
        Disposer.register(this, model);


        addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
            }

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

    public DBObjectSelectionHistory getSelectionHistory() {
        return selectionHistory;
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
                setRootObject(schemaObject, true);
            }
        }

        public void update(AnActionEvent e) {
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



    /****************************************************
     *              LoadInProgress handling             *
     ****************************************************/

    public void registerLoadInProgressNode(ObjectDependencyTreeNode node) {
        synchronized (loadInProgressNodes) {
            boolean startTimer = loadInProgressNodes.size() == 0;
            loadInProgressNodes.add(node);
            if (startTimer) {
                Timer reloader = new Timer("DBN Load in progress tree leaf reloader");
                reloader.schedule(new LoadInProgressRefreshTask(), 0, 50);
            }
        }
    }

    private class LoadInProgressRefreshTask extends TimerTask {
        int iterations = 0;
        public void run() {
            synchronized (loadInProgressNodes) {
                Iterator<ObjectDependencyTreeNode> loadInProgressNodesIterator = loadInProgressNodes.iterator();
                while (loadInProgressNodesIterator.hasNext()) {
                    ObjectDependencyTreeNode loadInProgressTreeNode = loadInProgressNodesIterator.next();
                    if (loadInProgressTreeNode.isDisposed()) {
                        loadInProgressNodesIterator.remove();
                    } else {
                        getModel().refreshLoadInProgressNode(loadInProgressTreeNode);
                    }
                }

                if (loadInProgressNodes.isEmpty()) {
                    cancel();
                }
            }

            iterations++;
        }
    }
    @Override
    public ObjectDependencyTreeModel getModel() {
        return (ObjectDependencyTreeModel) super.getModel();
    }

    public void setDependencyType(ObjectDependencyType dependencyType) {
        ObjectDependencyTreeModel oldModel = getModel();
        Project project = FailsafeUtil.get(oldModel.getProject());
        ObjectDependencyManager dependencyManager = ObjectDependencyManager.getInstance(project);
        dependencyManager.setLastUserDependencyType(dependencyType);

        DBSchemaObject object = oldModel.getObject();
        if (object != null) {
            ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(this, object, dependencyType);
            setModel(model);
            Disposer.dispose(oldModel);
        }
    }

    public void setRootObject(DBSchemaObject object, boolean addHistory) {
        ObjectDependencyTreeModel oldModel = getModel();
        if (addHistory) {
            selectionHistory.add(object);
        }

        ObjectDependencyType dependencyType = oldModel.getDependencyType();
        ObjectDependencyTreeModel model = new ObjectDependencyTreeModel(this, object, dependencyType);
        setModel(model);
        Disposer.dispose(oldModel);
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
        selectionHistory = null;
        project = null;
    }


}
