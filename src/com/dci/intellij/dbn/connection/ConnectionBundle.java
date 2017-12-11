package com.dci.intellij.dbn.connection;

import javax.swing.Icon;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.BrowserTreeNodeBase;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.AbstractFiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.list.FiltrableListImpl;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

public class ConnectionBundle extends BrowserTreeNodeBase implements BrowserTreeNode, Disposable {

    private static final Filter<ConnectionHandler> ACTIVE_CONNECTIONS_FILTER = new Filter<ConnectionHandler>() {
        public boolean accepts(ConnectionHandler connectionHandler) {
            return connectionHandler != null && connectionHandler.isEnabled();
        }
    };


    private ProjectRef projectRef;
    private AbstractFiltrableList<ConnectionHandler> connectionHandlers = new FiltrableListImpl<ConnectionHandler>(ACTIVE_CONNECTIONS_FILTER);
    private List<ConnectionHandler> virtualConnections = new ArrayList<ConnectionHandler>();

    public ConnectionBundle(Project project) {
        this.projectRef = new ProjectRef(project);
        virtualConnections.add(new VirtualConnectionHandler(
                ConnectionId.VIRTUAL_ORACLE_CONNECTION,
                "Virtual - Oracle 10.1",
                DatabaseType.ORACLE,
                10.1,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                ConnectionId.VIRTUAL_MYSQL_CONNECTION,
                "Virtual - MySQL 5.0",
                DatabaseType.MYSQL,
                5.0,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                ConnectionId.VIRTUAL_POSTGRES_CONNECTION,
                "Virtual - PostgreSQL 9.3.4",
                DatabaseType.POSTGRES,
                9.3,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                ConnectionId.VIRTUAL_SQLITE_CONNECTION,
                "Virtual - SQLite 3.10.2",
                DatabaseType.SQLITE,
                3.10,
                project));

        virtualConnections.add(new VirtualConnectionHandler(
                ConnectionId.VIRTUAL_ISO92_SQL_CONNECTION,
                "Virtual - ISO-92 SQL",
                DatabaseType.UNKNOWN,
                92,
                project));
    }

    public List<ConnectionHandler> getVirtualConnections() {
        return virtualConnections;
    }

    public ConnectionHandler getVirtualConnection(ConnectionId id) {
        for (ConnectionHandler virtualConnection : virtualConnections) {
            if (virtualConnection.getId() == id) {
                return virtualConnection;
            }
        }
        return null;
    }

    public void applySettings(ConnectionBundleSettings settings) {
        AbstractFiltrableList<ConnectionHandler> newConnectionHandlers = new FiltrableListImpl<ConnectionHandler>(ACTIVE_CONNECTIONS_FILTER);
        final List<ConnectionHandler> oldConnectionHandlers = new ArrayList<ConnectionHandler>(this.connectionHandlers.getFullList());
        List<ConnectionSettings> connections = settings.getConnections();
        boolean listChanged = false;
        for (ConnectionSettings connection : connections) {
            ConnectionId connectionId = connection.getConnectionId();
            ConnectionHandler connectionHandler = getConnectionHandler(oldConnectionHandlers, connectionId);
            if (connectionHandler == null) {
                connectionHandler = new ConnectionHandlerImpl(this, connection);
                newConnectionHandlers.add(connectionHandler);
                Disposer.register(this, connectionHandler);
                listChanged = true;
            } else {
                listChanged = listChanged || connectionHandler.isEnabled() != connection.isActive();
                connectionHandler.setSettings(connection);
                newConnectionHandlers.add(connectionHandler);
                oldConnectionHandlers.remove(connectionHandler);
            }
        }
        this.connectionHandlers = newConnectionHandlers;



        final Project project = getProject();
        listChanged = listChanged || oldConnectionHandlers.size() > 0;
        if (listChanged) {
            new SettingsChangeNotifier() {
                @Override
                public void notifyChanges() {
                    EventUtil.notify(project, ConnectionSettingsListener.TOPIC).connectionsChanged();
                    ConnectionManager connectionManager = ConnectionManager.getInstance(project);
                    connectionManager.disposeConnections(oldConnectionHandlers);
                }
            };
        }
    }

    ConnectionHandler getConnectionHandler(List<ConnectionHandler> list, ConnectionId connectionId) {
        for (ConnectionHandler connectionHandler : list) {
            if (connectionHandler.getId() == connectionId) {
                return connectionHandler;
            }
        }
        return null;
    }

    public Icon getIcon(int flags) {
        return Icons.PROJECT;
    }


    @NotNull
    public Project getProject() {
        return FailsafeUtil.get(projectRef.get());
    }

    @Nullable
    @Override
    public GenericDatabaseElement getParentElement() {
        return null;
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    @Nullable
    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void addConnection(ConnectionHandler connectionHandler) {
        connectionHandlers.add(connectionHandler);
        Disposer.register(this, connectionHandler);
    }

    public void setConnectionHandlers(List<ConnectionHandler> connectionHandlers) {
        this.connectionHandlers = new FiltrableListImpl<ConnectionHandler>(connectionHandlers, ACTIVE_CONNECTIONS_FILTER);
    }

    public boolean containsConnection(ConnectionHandler connectionHandler) {
        return connectionHandlers.contains(connectionHandler);
    }

    public ConnectionHandler getConnection(ConnectionId id) {
        for (ConnectionHandler connectionHandler : connectionHandlers.getFullList()){
            if (connectionHandler.getId() == id) return connectionHandler;
        }
        return null;
    }

    public FiltrableList<ConnectionHandler> getConnectionHandlers() {
        return connectionHandlers;
    }

    public List<ConnectionHandler> getAllConnectionHandlers() {
        return connectionHandlers.getFullList();
    }

    public void dispose() {
        super.dispose();
        connectionHandlers.clear();
        virtualConnections.clear();
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {}

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    /*********************************************************
    *                  ItemPresentation                      *
    *********************************************************/
    public String getName() {
        return getPresentableText();
    }

    public String getLocationString() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return this;
    }

    public Icon getIcon(boolean open) {
        return getIcon(0);
    }

    /*********************************************************
    *                       TreeElement                     *
    *********************************************************/
    public boolean isTreeStructureLoaded() {
        return true;
    }

    public void initTreeElement() {}

    public boolean canExpand() {
        return true;
    }

    @Nullable
    public BrowserTreeNode getParent() {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        return browserManager.isTabbedMode() ? null : activeBrowserTree == null ? null : activeBrowserTree.getModel().getRoot();
    }

    public List<? extends BrowserTreeNode> getChildren() {
        return null;  //should never be used
    }

    public void refreshTreeChildren(@NotNull DBObjectType... objectTypes) {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.getObjectBundle().refreshTreeChildren(objectTypes);
        }
    }

    public void rebuildTreeChildren() {
        for (ConnectionHandler connectionHandler : connectionHandlers) {
            connectionHandler.getObjectBundle().rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getChildAt(int index) {
        return connectionHandlers.get(index).getObjectBundle();
    }

    public int getChildCount() {
        return connectionHandlers.size();
    }

    public boolean isLeaf() {
        return connectionHandlers.size() == 0;
    }

    public int getIndex(BrowserTreeNode child) {
        DBObjectBundle objectBundle = (DBObjectBundle) child;
        return connectionHandlers.indexOf(objectBundle.getConnectionHandler());
    }

    public int getTreeDepth() {
        return 1;
    }

    public String getPresentableText() {
        return "Database connections";
    }

    public String getPresentableTextDetails() {
        int size = connectionHandlers.size();
        return size == 0 ? "(no connections)" : "(" + size + ')';
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return null;
    }

   /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return "";
    }


    public boolean isEmpty() {
        return connectionHandlers.getFullList().isEmpty();
    }
}
