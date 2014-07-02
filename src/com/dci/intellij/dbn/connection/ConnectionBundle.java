package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.code.sql.color.SQLTextAttributesKeys;
import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableList;
import com.dci.intellij.dbn.common.options.Configuration;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ui.ConnectionBundleSettingsForm;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vcs.FileStatus;
import org.jdom.Element;

import javax.swing.Icon;
import java.util.List;

public abstract class ConnectionBundle
        extends Configuration<ConnectionBundleSettingsForm>
        implements Comparable, BrowserTreeNode, JDOMExternalizable, Disposable {

    protected Logger log = Logger.getInstance(this.getClass().getName());

    public static final Filter<ConnectionHandler> ACTIVE_CONNECTIONS_FILTER = new Filter<ConnectionHandler>() {
        public boolean accepts(ConnectionHandler connectionHandler) {
            return connectionHandler.isActive();
        }
    };


    private Project project;
    private FiltrableList<ConnectionHandler> connectionHandlers = new FiltrableList<ConnectionHandler>(ACTIVE_CONNECTIONS_FILTER);

    protected ConnectionBundle(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public boolean isDisposed() {
        return false;
    }

    public String getDisplayName() {
        return "Connections";
    }

    public String getHelpTopic() {
        return "connectionBundle";
    }

    public GenericDatabaseElement getUndisposedElement() {
        return this;
    }

    public DynamicContent getDynamicContent(DynamicContentType dynamicContentType) {
        return null;
    }

    public void addConnections(List<ConnectionHandler> connectionHandlers) {
        this.connectionHandlers.addAll(connectionHandlers);
    }

    public void setConnectionHandlers(List<ConnectionHandler> connectionHandlers) {
        this.connectionHandlers = new FiltrableList<ConnectionHandler>(connectionHandlers, ACTIVE_CONNECTIONS_FILTER);
    }

    public boolean containsConnection(ConnectionHandler connectionHandler) {
        return connectionHandlers.contains(connectionHandler);
    }

    public ConnectionHandler getConnection(String id) {
        for (ConnectionHandler connectionHandler : connectionHandlers.getFullList()){
            if (connectionHandler.getId().equals(id)) return connectionHandler;
        }
        return null;
    }

    public FiltrableList<ConnectionHandler> getConnectionHandlers() {
        return connectionHandlers;
    }


    public void dispose() {
        for (ConnectionHandler connectionHandler : connectionHandlers){
            connectionHandler.dispose();
        }
        connectionHandlers.clear();
        project = null;
    }

    public boolean isModified() {
        if (super.isModified()) return true;

        for (ConnectionHandler connectionHandler : connectionHandlers) {
            if (connectionHandler.getSettings().isModified()) return true;
        }
        return false;
    }


    public ConnectionBundleSettingsForm createConfigurationEditor() {
        return new ConnectionBundleSettingsForm(this);
    }

    /*********************************************************
     *                      Configurable                     *
     *********************************************************/
    public void readConfiguration(Element element) throws InvalidDataException {
        Element connectionsElement = element.getChild("connections");
        if (connectionsElement != null) {
            for (Object o : connectionsElement.getChildren()) {
                Element connectionElement = (Element) o;
                ConnectionSettings connectionConfig = new ConnectionSettings(this);
                connectionConfig.readConfiguration(connectionElement);
                ConnectionHandler connectionHandler = new ConnectionHandlerImpl(this, connectionConfig);
                connectionHandlers.add(connectionHandler);
            }
        }
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        Element connectionsElement = new Element("connections");
        element.addContent(connectionsElement);
        for (ConnectionHandler connectionHandler : connectionHandlers.getFullList()) {
            Element connectionElement = new Element("connection");
            ConnectionSettings connectionSettings = connectionHandler.getSettings();
            connectionSettings.writeConfiguration(connectionElement);
            connectionsElement.addContent(connectionElement);
        }
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readExternal(Element element) throws InvalidDataException {
        readConfiguration(element);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        writeConfiguration(element);
    }

    /*********************************************************
    *                    NavigationItem                      *
    *********************************************************/
    public void navigate(boolean requestFocus) {

    }

    public boolean canNavigate() {
        return true;
    }

    public boolean canNavigateToSource() {
        return false;
    }

    public FileStatus getFileStatus() {
        return FileStatus.NOT_CHANGED;
    }


    /*********************************************************
    *                  ItemPresentation                      *
    *********************************************************/
    public String getName() {
        return getPresentableText();
    }

    public TextAttributesKey getTextAttributesKey() {
        return SQLTextAttributesKeys.IDENTIFIER;
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

    public BrowserTreeNode getTreeParent() {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        return browserManager.isTabbedMode() ? null : activeBrowserTree == null ? null : activeBrowserTree.getModel().getRoot();
    }

    public List<? extends BrowserTreeNode> getTreeChildren() {
        return null;  //should never be used
    }

    public void rebuildTreeChildren() {
        for (ConnectionHandler connectionHandler : getConnectionHandlers()) {
            connectionHandler.getObjectBundle().rebuildTreeChildren();
        }
    }

    public BrowserTreeNode getTreeChild(int index) {
        return getConnectionHandlers().get(index).getObjectBundle();
    }

    public int getTreeChildCount() {
        return getConnectionHandlers().size();
    }

    public boolean isLeafTreeElement() {
        return getConnectionHandlers().size() == 0;
    }

    public int getIndexOfTreeChild(BrowserTreeNode child) {
        DBObjectBundle objectBundle = (DBObjectBundle) child;
        return getConnectionHandlers().indexOf(objectBundle.getConnectionHandler());
    }

    public int getTreeDepth() {
        return 1;
    }

    public String getPresentableText() {
        return "Database connections";
    }

    public String getPresentableTextDetails() {
        int size = getConnectionHandlers().size();
        return size == 0 ? "(no connections)" : "(" + size + ")";
    }

    public String getPresentableTextConditionalDetails() {
        return null;
    }

    public ConnectionHandler getConnectionHandler() {
        return null;
    }

   /*********************************************************
    *                    ToolTipProvider                    *
    *********************************************************/
    public String getToolTip() {
        return "";
    }    
}
