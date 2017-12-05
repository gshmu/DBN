package com.dci.intellij.dbn.browser.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.options.EnvironmentVisibilitySettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerAdapter;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.ui.tab.TabbedPane;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.ui.tabs.TabsListener;

public class TabbedBrowserForm extends DatabaseBrowserForm{
    private TabbedPane connectionTabs;
    private JPanel mainPanel;

    public TabbedBrowserForm(BrowserToolWindowForm parentComponent) {
        super(parentComponent);
        connectionTabs = new TabbedPane(this);
        //connectionTabs.setBackground(GUIUtil.getListBackground());
        //mainPanel.add(connectionTabs, BorderLayout.CENTER);
        initTabs();
        connectionTabs.addListener(new TabsListener() {
            public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
/*
                ToolWindow toolWindow = browserManager.getBrowserToolWindow();
                if (toolWindow.isVisible()) {
                    toolWindow.activate(null);
                }
*/
            }

            public void beforeSelectionChanged(TabInfo oldSelection, TabInfo newSelection) {
            }

            public void tabRemoved(TabInfo tabInfo) {
            }

            public void tabsMoved() {
            }
        });

        Project project = getProject();
        EventUtil.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        Disposer.register(this, connectionTabs);
    }


    private void initTabs() {
        Project project = getProject();

        TabbedPane oldConnectionTabs = this.connectionTabs;
        this.connectionTabs = new TabbedPane(this);

        ConnectionManager connectionManager = ConnectionManager.getInstance(project);
        ConnectionBundle connectionBundle = connectionManager.getConnectionBundle();
        for (ConnectionHandler connectionHandler: connectionBundle.getConnectionHandlers()) {
            SimpleBrowserForm browserForm = new SimpleBrowserForm(this, connectionHandler);
            JComponent component = browserForm.getComponent();
            TabInfo tabInfo = new TabInfo(component);
            tabInfo.setText(CommonUtil.nvl(connectionHandler.getName(), "[unnamed connection]"));
            tabInfo.setObject(browserForm);
            //tabInfo.setIcon(connectionHandler.getIcon());
            this.connectionTabs.addTab(tabInfo);

            EnvironmentType environmentType = connectionHandler.getEnvironmentType();
            tabInfo.setTabColor(environmentType.getColor());
        }
        if (this.connectionTabs.getTabCount() == 0) {
            mainPanel.removeAll();
            mainPanel.add(new JBList(new ArrayList()), BorderLayout.CENTER);
        } else {
            if (mainPanel.getComponentCount() > 0) {
                Component component = mainPanel.getComponent(0);
                if (component != this.connectionTabs) {
                    mainPanel.removeAll();
                    mainPanel.add(this.connectionTabs, BorderLayout.CENTER);
                }
            } else {
                mainPanel.add(this.connectionTabs, BorderLayout.CENTER);
            }
        }
        DisposerUtil.dispose(oldConnectionTabs);
    }

    @Nullable
    private SimpleBrowserForm getBrowserForm(ConnectionHandler connectionHandler) {
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            if (browserForm.getConnectionHandler() == connectionHandler) {
                return browserForm;
            }
        }
        return null;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    @Nullable
    public DatabaseBrowserTree getBrowserTree() {
        return getActiveBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getBrowserTree(ConnectionHandler connectionHandler) {
        SimpleBrowserForm browserForm = getBrowserForm(connectionHandler);
        return browserForm== null ? null : browserForm.getBrowserTree();
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        TabInfo tabInfo = connectionTabs.getSelectedInfo();
        if (tabInfo != null) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            return browserForm.getBrowserTree();
        }
        return null;
    }

    public void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll) {
        ConnectionHandler connectionHandler = treeNode.getConnectionHandler();
        SimpleBrowserForm browserForm = getBrowserForm(connectionHandler);
        if (browserForm != null) {
            connectionTabs.select(browserForm.getComponent(), focus);
            if (scroll) {
                browserForm.selectElement(treeNode, focus, scroll);
            }
        }
    }

    @Override
    public void rebuildTree() {
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            browserForm.rebuildTree();
        }
    }

    public void dispose() {
        super.dispose();
    }


    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerAdapter() {
        @Override
        public void configurationChanged() {
            Project project = getProject();
            EnvironmentVisibilitySettings visibilitySettings = getEnvironmentSettings(project).getVisibilitySettings();
            for (TabInfo tabInfo : connectionTabs.getTabs()) {
                SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
                ConnectionHandler connectionHandler = browserForm.getConnectionHandler();
                JBColor environmentColor = connectionHandler.getEnvironmentType().getColor();
                if (visibilitySettings.getConnectionTabs().value()) {
                    tabInfo.setTabColor(environmentColor);
                } else {
                    tabInfo.setTabColor(null);
                }
            }
        }
    };

    public void refreshTabInfo(ConnectionId connectionId) {
        for (TabInfo tabInfo : connectionTabs.getTabs()) {
            SimpleBrowserForm browserForm = (SimpleBrowserForm) tabInfo.getObject();
            ConnectionHandler connectionHandler = browserForm.getConnectionHandler();
            if (connectionHandler.getId() == connectionId) {
                tabInfo.setText(connectionHandler.getName());
                break;
            }
        }

    }
}

