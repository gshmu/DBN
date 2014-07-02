package com.dci.intellij.dbn.browser;

import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.browser.ui.BrowserToolWindowForm;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.options.setting.BooleanSetting;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectionManagerListener;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.dci.intellij.dbn.vfs.SQLConsoleFile;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBrowserManager extends AbstractProjectComponent implements JDOMExternalizable{
    public static final String TOOL_WINDOW_ID = "DB Browser";

    private BooleanSetting autoscrollFromEditor = new BooleanSetting("autoscroll-from-editor", true);
    private BooleanSetting autoscrollToEditor   = new BooleanSetting("autoscroll-to-editor", false);
    private BooleanSetting showObjectProperties = new BooleanSetting("show-object-properties", true);
    public static final ThreadLocal<Boolean> AUTOSCROLL_FROM_EDITOR = new ThreadLocal<Boolean>();
    private BrowserToolWindowForm toolWindowForm;

    private DatabaseBrowserManager(Project project) {
        super(project);
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        return getToolWindowForm().getActiveBrowserTree();
    }

    public ConnectionHandler getActiveConnection() {
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            BrowserTreeModel browserTreeModel = activeBrowserTree.getModel();
            if (browserTreeModel instanceof TabbedBrowserTreeModel) {
                TabbedBrowserTreeModel tabbedBrowserTreeModel = (TabbedBrowserTreeModel) browserTreeModel;
                return tabbedBrowserTreeModel.getConnectionHandler();
            }

            BrowserTreeNode browserTreeNode = activeBrowserTree.getSelectedNode();
            if (browserTreeNode != null) {
                return browserTreeNode.getConnectionHandler();
            }
        }

        return null;
    }

    @NotNull
    public ToolWindow getBrowserToolWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(getProject());
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    }

    public synchronized BrowserToolWindowForm getToolWindowForm() {
        if (toolWindowForm == null) {
            toolWindowForm = new BrowserToolWindowForm(getProject());
        }
        return toolWindowForm;
    }

    public BooleanSetting getAutoscrollFromEditor() {
        return autoscrollFromEditor;
    }

    public BooleanSetting getAutoscrollToEditor() {
        return autoscrollToEditor;
    }

    public BooleanSetting getShowObjectProperties() {
        return showObjectProperties;
    }

    public boolean isDisposed() {
        return false;
    }

    public String toString() {
        return "DB Browser";
    }

    public synchronized void navigateToElement(BrowserTreeNode treeNode, boolean requestFocus) {
        ToolWindow toolWindow = getBrowserToolWindow();

        toolWindow.show(null);
        if (treeNode != null) {
            getToolWindowForm().getBrowserForm().selectElement(treeNode, requestFocus);
        }
    }

    public synchronized void navigateToElement(BrowserTreeNode treeNode) {
        if (treeNode != null) {
            getToolWindowForm().getBrowserForm().selectElement(treeNode, false);
        }
    }

    public boolean isVisible() {
        ToolWindow toolWindow = getBrowserToolWindow();
        return toolWindow.isVisible();
    }

    /***************************************
     *     FileEditorManagerListener       *
     ***************************************/

    private boolean scroll() {
        return autoscrollFromEditor.value() && (AUTOSCROLL_FROM_EDITOR.get() == null || AUTOSCROLL_FROM_EDITOR.get());
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DatabaseBrowserManager getInstance(Project project) {
        return project.getComponent(DatabaseBrowserManager.class);
    }

    @NonNls @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DatabaseBrowserManager";
    }

    public void initComponent() {
        Project project = getProject();
        EventManager.subscribe(project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventManager.subscribe(project, ObjectFilterChangeListener.TOPIC, filterChangeListener);
        EventManager.subscribe(project, ConnectionManagerListener.TOPIC, connectionManagerListener);
    }

    public void disposeComponent() {
        EventManager.unsubscribe(
                fileEditorManagerListener,
                filterChangeListener,
                connectionManagerListener);

        DisposerUtil.dispose(toolWindowForm);
        toolWindowForm = null;
        super.disposeComponent();
    }

    /**
     *
     * @deprecated
     */
    public static void scrollToSelectedElement(final ConnectionHandler connectionHandler) {
        if (connectionHandler != null && !connectionHandler.isDisposed()) {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(connectionHandler.getProject());
            BrowserToolWindowForm toolWindowForm = browserManager.getToolWindowForm();
            if (toolWindowForm != null) {
                final DatabaseBrowserTree browserTree = toolWindowForm.getBrowserTree(connectionHandler);
                if (browserTree != null && browserTree.getTargetSelection() != null) {
                    new ConditionalLaterInvocator() {
                        public void execute() {
                            browserTree.scrollToSelectedElement();
                        }
                    }.start();
                }
            }
        }
    }

    public void dispose() {
    }

    public boolean isTabbedMode() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getGeneralSettings().getDisplayMode() == BrowserDisplayMode.TABBED;
    }


    /***************************************
     *         JDOMExternalizable          *
     ***************************************/
    public void readExternal(Element element) throws InvalidDataException {
        autoscrollToEditor.readConfiguration(element);
        autoscrollFromEditor.readConfiguration(element);
        showObjectProperties.readConfiguration(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        autoscrollToEditor.writeConfiguration(element);
        autoscrollFromEditor.writeConfiguration(element);
        showObjectProperties.writeConfiguration(element);
    }

    /***************************************
     *           ModuleListener            *
     ***************************************/

    /**********************************************************
     *                       Listeners                        *
     **********************************************************/
    private ConnectionManagerListener connectionManagerListener = new ConnectionManagerListener() {
        @Override
        public void connectionsChanged() {
            if (toolWindowForm != null) {
                toolWindowForm.getBrowserForm().rebuild();
            }
        }
    };

    private ObjectFilterChangeListener filterChangeListener = new ObjectFilterChangeListener() {
        public void filterChanged(Filter<BrowserTreeNode> filter) {
            if (filter == getObjectFilter()) {
                getToolWindowForm().getBrowserForm().updateTree();
            } else {
                ConnectionHandler connectionHandler = getConnectionHandler(filter);
                if (connectionHandler != null) {
                    connectionHandler.getObjectBundle().rebuildTreeChildren();
                }
            }
        }

        private ConnectionHandler getConnectionHandler(Filter<BrowserTreeNode> filter) {
            ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
            for (ConnectionBundle connectionBundle : connectionManager.getConnectionBundles()) {
                for (ConnectionHandler connectionHandler : connectionBundle.getConnectionHandlers()) {
                    if (filter == connectionHandler.getObjectFilter()) {
                        return connectionHandler;
                    }
                }
            }
            return null;
        }
    };

    public Filter<BrowserTreeNode> getObjectFilter() {
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(getProject());
        return browserSettings.getFilterSettings().getObjectTypeFilterSettings().getElementFilter();
    }

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerAdapter() {
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            if (scroll()) {
                if (file instanceof DatabaseEditableObjectFile) {
                    DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) file;
                    navigateToElement(databaseFile.getObject());
                }
            }
        }

        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            if (scroll()) {
                VirtualFile newFile = event.getNewFile();
                VirtualFile oldFile = event.getOldFile();

                if (newFile != oldFile) {
                    if (newFile instanceof DatabaseEditableObjectFile) {
                        DatabaseEditableObjectFile databaseFile = (DatabaseEditableObjectFile) newFile;
                        navigateToElement(databaseFile.getObject());
                    }

                    if (newFile instanceof SQLConsoleFile) {
                        SQLConsoleFile sqlConsoleFile = (SQLConsoleFile) newFile;
                        ConnectionHandler connectionHandler = sqlConsoleFile.getConnectionHandler();
                        if (connectionHandler!= null && !connectionHandler.isDisposed()) {
                            navigateToElement(connectionHandler.getObjectBundle());
                        }

                    }
                }
            }
        }
    };

    public void showObjectProperties(boolean visible) {
        BrowserToolWindowForm toolWindowForm = getToolWindowForm();
        if (visible)
            toolWindowForm.showObjectProperties(); else
            toolWindowForm.hideObjectProperties();
        showObjectProperties.setValue(visible);
    }

    public List<DBObject> getSelectedObjects() {
        List<DBObject> selectedObjects = new ArrayList<DBObject>();
        DatabaseBrowserTree activeBrowserTree = getActiveBrowserTree();
        if (activeBrowserTree != null) {
            TreePath[] selectionPaths = activeBrowserTree.getSelectionPaths();
            if (selectionPaths != null) {
                for (TreePath treePath : selectionPaths) {
                    Object lastPathComponent = treePath.getLastPathComponent();
                    if (lastPathComponent instanceof DBObject) {
                        DBObject object = (DBObject) lastPathComponent;
                        selectedObjects.add(object);
                    }
                }
            }
        }
        return selectedObjects;
    }
}
