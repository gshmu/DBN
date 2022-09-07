package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.TreeNavigationHistory;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.SimpleBrowserTreeModel;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.thread.Background;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.ui.util.Mouse;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.action.ConnectionActionGroup;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.action.ObjectActionGroup;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.action.ObjectListActionGroup;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.tree.TreeUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPopupMenu;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Getter
public final class DatabaseBrowserTree extends DBNTree {
    private final TreeNavigationHistory navigationHistory = new TreeNavigationHistory();
    private transient BrowserTreeNode targetSelection;
    private transient boolean listenersEnabled = true;

    public DatabaseBrowserTree(@NotNull DBNComponent parent, @Nullable ConnectionHandler connection) {
        super(parent, createModel(parent.ensureProject(), connection));
        BrowserTreeModel treeModel = getModel();

        addKeyListener(createKeyListener());
        addMouseListener(createMouseListener());
        addTreeSelectionListener(createTreeSelectionListener());

        setToggleClickCount(0);
        setRootVisible(treeModel instanceof TabbedBrowserTreeModel);
        setShowsRootHandles(true);
        setAutoscrolls(true);
        DatabaseBrowserTreeCellRenderer browserTreeCellRenderer = new DatabaseBrowserTreeCellRenderer(parent.ensureProject());
        setCellRenderer(browserTreeCellRenderer);
        //setExpandedState(DatabaseBrowserUtils.createTreePath(treeModel.getRoot()), false);

        new DatabaseBrowserTreeSpeedSearch(this);

        Disposer.register(parent, this);
        Disposer.register(this, navigationHistory);
    }

    private static BrowserTreeModel createModel(@NotNull Project project, @Nullable ConnectionHandler connection) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        return connection == null ?
                new SimpleBrowserTreeModel(project, connectionManager.getConnectionBundle()) :
                new TabbedBrowserTreeModel(connection);

    }

    @Override
    public BrowserTreeModel getModel() {
        return (BrowserTreeModel) super.getModel();
    }

    public void expandConnectionManagers() {
        Dispatch.run(() -> {
            ConnectionManager connectionManager = ConnectionManager.getInstance(ensureProject());
            ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
            TreePath treePath = DatabaseBrowserUtils.createTreePath(connectionBundle);
            setExpandedState(treePath, true);
        });
    }

    public void selectElement(BrowserTreeNode treeNode, boolean focus) {
        if (treeNode != null) {
            ConnectionHandler connection = treeNode.getConnection();
            Filter<BrowserTreeNode> filter = connection.getObjectTypeFilter();

            if (filter.accepts(treeNode)) {
                targetSelection = treeNode;
                scrollToSelectedElement();
                if (focus) requestFocus();
            }
        }
    }

    public void scrollToSelectedElement() {
        if (ensureProject().isOpen() && targetSelection != null) {
            Background.run(() -> {
                BrowserTreeNode targetSelection = this.targetSelection;
                if (targetSelection != null) {
                    targetSelection = targetSelection.getUndisposedEntity();
                    if (targetSelection != null) {
                        TreePath treePath = DatabaseBrowserUtils.createTreePath(targetSelection);
                        if (treePath != null) {
                            for (Object object : treePath.getPath()) {
                                BrowserTreeNode treeNode = (BrowserTreeNode) object;
                                if (!Failsafe.check(treeNode)) {
                                    this.targetSelection = null;
                                    return;
                                }


                                if (treeNode.equals(targetSelection)) {
                                    break;
                                }

                                if (!treeNode.isLeaf() && !treeNode.isTreeStructureLoaded()) {
                                    selectPath(DatabaseBrowserUtils.createTreePath(treeNode));
                                    treeNode.getChildren();
                                    return;
                                }
                            }

                            this.targetSelection = null;
                            selectPath(treePath);
                        }
                    }
                }
            });
        }
    }



    public BrowserTreeNode getSelectedNode() {
        TreePath selectionPath = getSelectionPath();
        return selectionPath == null ? null : (BrowserTreeNode) selectionPath.getLastPathComponent();
    }

    private void selectPath(TreePath treePath) {
        Dispatch.run(() -> TreeUtil.selectPath(DatabaseBrowserTree.this, treePath, true));
    }


    @Override
    public String getToolTipText(MouseEvent e) {
        TreePath path = getClosestPathForLocation(e.getX(), e.getY());
        if (path != null) {
            Rectangle pathBounds = getPathBounds(path);

            if (pathBounds != null) {
                Point mouseLocation = UserInterface.getRelativeMouseLocation(e.getComponent());
                if (mouseLocation != null && pathBounds.contains(mouseLocation)) {
                    Object object = path.getLastPathComponent();
                    if (object instanceof ToolTipProvider) {
                        ToolTipProvider toolTipProvider = (ToolTipProvider) object;
                        return toolTipProvider.getToolTip();
                    }
                }
            }
        }
        return null;
    }

    public void navigateBack() {
        BrowserTreeNode treeNode = navigationHistory.previous();
        if (treeNode != null) {
            selectPathSilently(DatabaseBrowserUtils.createTreePath(treeNode));
        }
    }

    public void navigateForward() {
        BrowserTreeNode treeNode = navigationHistory.next();
        if (treeNode != null) {
            selectPathSilently(DatabaseBrowserUtils.createTreePath(treeNode));
        }
    }


    private void selectPathSilently(TreePath treePath) {
        if (treePath != null) {
            listenersEnabled = false;
            selectionModel.setSelectionPath(treePath);
            TreeUtil.selectPath(DatabaseBrowserTree.this, treePath, true);
            listenersEnabled = true;
        }
    }

    public void expandAll() {
        BrowserTreeNode root = getModel().getRoot();
        expand(root);
    }

    public void expand(BrowserTreeNode treeNode) {
        if (treeNode.canExpand()) {
            expandPath(DatabaseBrowserUtils.createTreePath(treeNode));
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                BrowserTreeNode childTreeNode = treeNode.getChildAt(i);
                expand(childTreeNode);
            }
        }
    }

    public void collapseAll() {
        BrowserTreeNode root = getModel().getRoot();
        collapse(root);
    }

    private void collapse(BrowserTreeNode treeNode) {
        if (!treeNode.isLeaf() && treeNode.isTreeStructureLoaded()) {
            for (int i = 0; i < treeNode.getChildCount(); i++) {
                BrowserTreeNode childTreeNode = treeNode.getChildAt(i);
                collapse(childTreeNode);
                collapsePath(DatabaseBrowserUtils.createTreePath(childTreeNode));
            }
        }
    }

    private void processSelectEvent(InputEvent event, TreePath path, boolean deliberate) {
        if (path != null) {
            Object lastPathEntity = path.getLastPathComponent();
            if (lastPathEntity instanceof DBObject) {
                DBObject object = (DBObject) lastPathEntity;
                DatabaseFileSystem databaseFileSystem = DatabaseFileSystem.getInstance();
                Project project = ensureProject();
                if (object instanceof DBConsole) {
                    DBConsole console = (DBConsole) object;
                    FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                    fileEditorManager.openFile(console.getVirtualFile(), true);
                    event.consume();
                } else if (object.is(DBObjectProperty.EDITABLE)) {
                    DBSchemaObject schemaObject = (DBSchemaObject) object;
                    databaseFileSystem.connectAndOpenEditor(schemaObject, null, false, deliberate);
                    event.consume();

                } else if (object.is(DBObjectProperty.NAVIGABLE)) {
                    databaseFileSystem.connectAndOpenEditor(object, null, false, deliberate);
                    event.consume();

                } else if (deliberate) {
                    Progress.prompt(project, "Loading object reference", true, progress -> {
                        DBObject navigationObject = object.getDefaultNavigationObject();
                        if (navigationObject != null) {
                            Progress.check(progress);
                            Dispatch.run(() -> navigationObject.navigate(true));
                        }
                    });
                }
            } else if (lastPathEntity instanceof DBObjectBundle) {
                DBObjectBundle objectBundle = (DBObjectBundle) lastPathEntity;
                ConnectionHandler connection = objectBundle.getConnection();
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(connection.getProject());
                DBConsole defaultConsole = connection.getConsoleBundle().getDefaultConsole();
                fileEditorManager.openFile(defaultConsole.getVirtualFile(), deliberate);
            }
        }
    }
    
/*    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        boolean navigable = false;
        if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && !e.isConsumed()) {
            TreePath path = getPathForLocation(e.getX(), e.getY());
            Object lastPathEntity = path == null ? null : path.getLastPathComponent();
            if (lastPathEntity instanceof DBObject) {
                DBObject object = (DBObject) lastPathEntity;
                DBObject navigationObject = object.getDefaultNavigationObject();
                navigable = navigationObject != null;
            }
            
        }

        if (navigable) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            super.processMouseMotionEvent(e);
            setCursor(Cursor.getDefaultCursor());
        }
    }  */

    /********************************************************
     *                 TreeSelectionListener                *
     ********************************************************/
    private TreeSelectionListener createTreeSelectionListener() {
        return new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (Failsafe.check(this) && listenersEnabled) {
                    Object object = e.getPath().getLastPathComponent();
                    if (object instanceof BrowserTreeNode) {
                        BrowserTreeNode treeNode = (BrowserTreeNode) object;
                        if (targetSelection == null || treeNode.equals(targetSelection)) {
                            navigationHistory.add(treeNode);
                        }
                    }

                    ProjectEvents.notify(ensureProject(),
                            BrowserTreeEventListener.TOPIC,
                            (listener) -> listener.selectionChanged());
                }
            }
        };
    }

    /********************************************************
     *                      MouseListener                   *
     ********************************************************/
    private MouseListener createMouseListener() {
        return Mouse.listener().
                onClick(e -> {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(ensureProject());
                        if (browserManager.getAutoscrollToEditor().value() || e.getClickCount() > 1) {
                            TreePath path = getPathForLocation(e.getX(), e.getY());
                            processSelectEvent(e, path, e.getClickCount() > 1);
                        }
                    }}).
                onRelease(e -> {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        TreePath path = getPathForLocation(e.getX(), e.getY());
                        if (path != null) {
                            BrowserTreeNode lastPathEntity = (BrowserTreeNode) path.getLastPathComponent();
                            if (lastPathEntity.isDisposed()) return;

                            ActionGroup actionGroup = null;
                            if (lastPathEntity instanceof DBObjectList) {
                                DBObjectList<?> objectList = (DBObjectList<?>) lastPathEntity;
                                actionGroup = new ObjectListActionGroup(objectList);
                            } else if (lastPathEntity instanceof DBObject) {
                                DBObject object = (DBObject) lastPathEntity;
                                actionGroup = new ObjectActionGroup(object);
                            } else if (lastPathEntity instanceof DBObjectBundle) {
                                DBObjectBundle objectsBundle = (DBObjectBundle) lastPathEntity;
                                ConnectionHandler connection = objectsBundle.getConnection();
                                actionGroup = new ConnectionActionGroup(connection);
                            }

                            if (actionGroup != null) {
                                ActionPopupMenu actionPopupMenu = Actions.createActionPopupMenu(DatabaseBrowserTree.this, "", actionGroup);
                                JPopupMenu popupMenu = actionPopupMenu.getComponent();
                                popupMenu.show(DatabaseBrowserTree.this, e.getX(), e.getY());
                            }
                        }
                    }
                });
    }

    /********************************************************
     *                      KeyListener                     *
     ********************************************************/
    private KeyListener createKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {  // ENTER
                    TreePath path = getSelectionPath();
                    processSelectEvent(e, path, true);
                }
            }
        };
    }

    /********************************************************
     *                    Disposable                        *
     *******************************************************  */
    @Override
    public void disposeInner() {
        setModel(new SimpleBrowserTreeModel());
        super.disposeInner();
    }
}
