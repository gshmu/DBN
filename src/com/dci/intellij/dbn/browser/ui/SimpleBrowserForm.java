package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.listener.ObjectDetailSettingsListener;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

public class SimpleBrowserForm extends DatabaseBrowserForm{
    private JPanel mainPanel;
    private JScrollPane browserScrollPane;
    private final DatabaseBrowserTree browserTree;

    public SimpleBrowserForm(@NotNull TabbedBrowserForm parent, @NotNull ConnectionHandler connection) {
        super(parent);
        browserTree = createBrowserTree(connection);
    }

    public SimpleBrowserForm(@NotNull Project project) {
        super(project);
        browserTree = createBrowserTree(null);
    }

    @NotNull
    private DatabaseBrowserTree createBrowserTree(@Nullable ConnectionHandler connection) {
        DatabaseBrowserTree browserTree = new DatabaseBrowserTree(this, connection);
        browserScrollPane.setViewportView(browserTree);
        browserScrollPane.setBorder(JBUI.Borders.emptyTop(1));
        ToolTipManager.sharedInstance().registerComponent(browserTree);

        ProjectEvents.subscribe(ensureProject(), this, ObjectDetailSettingsListener.TOPIC, objectDetailSettingsListener);
        return browserTree;
    }


    @Nullable
    public ConnectionId getConnectionId(){
        ConnectionHandler connection = getConnection();
        return connection == null ? null : connection.getConnectionId();
    }

    @Nullable
    public ConnectionHandler getConnection(){
        DatabaseBrowserTree browserTree = getBrowserTree();
        if (browserTree.getModel() instanceof TabbedBrowserTreeModel) {
            TabbedBrowserTreeModel treeModel = (TabbedBrowserTreeModel) browserTree.getModel();
            return treeModel.getConnection();
        }
        return null;
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    @NotNull
    public DatabaseBrowserTree getBrowserTree() {
        return Failsafe.nn(browserTree);
    }

    @Override
    public void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll) {
        getBrowserTree().selectElement(treeNode, focus);
    }

    @Override
    public void rebuildTree() {
        getBrowserTree().getModel().getRoot().rebuildTreeChildren();
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private final ObjectDetailSettingsListener objectDetailSettingsListener = new ObjectDetailSettingsListener() {
        @Override
        public void displayDetailsChanged() {
            GUIUtil.repaint(browserTree);
        }
    };
}
