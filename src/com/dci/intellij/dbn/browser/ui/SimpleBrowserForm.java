package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.model.BrowserTreeModel;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.model.SimpleBrowserTreeModel;
import com.dci.intellij.dbn.browser.model.TabbedBrowserTreeModel;
import com.dci.intellij.dbn.browser.options.listener.ObjectDetailSettingsListener;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ui.JBUI;

import javax.swing.*;

public class SimpleBrowserForm extends DatabaseBrowserForm{
    private JPanel mainPanel;
    private JScrollPane browserScrollPane;
    private DatabaseBrowserTree browserTree;

    public SimpleBrowserForm(DisposableProjectComponent parentComponent) {
        this(parentComponent, new SimpleBrowserTreeModel(parentComponent.getProject(), ConnectionManager.getInstance(parentComponent.getProject()).getConnectionBundle()));
    }

    public SimpleBrowserForm(DisposableProjectComponent parentComponent, ConnectionHandler connectionHandler) {
        this(parentComponent, new TabbedBrowserTreeModel(connectionHandler));
    }

    private SimpleBrowserForm(DisposableProjectComponent parentComponent, BrowserTreeModel treeModel) {
        super(parentComponent);
        browserTree = new DatabaseBrowserTree(treeModel);
        browserScrollPane.setViewportView(browserTree);
        browserScrollPane.setBorder(JBUI.Borders.emptyTop(1));
        ToolTipManager.sharedInstance().registerComponent(browserTree);

        EventUtil.subscribe(getProject(), this, ObjectDetailSettingsListener.TOPIC, objectDetailSettingsListener);
        Disposer.register(this, browserTree);
    }
    
    public ConnectionHandler getConnectionHandler(){
        if (browserTree.getModel() instanceof TabbedBrowserTreeModel) {
            TabbedBrowserTreeModel treeModel = (TabbedBrowserTreeModel) browserTree.getModel();
            return treeModel.getConnectionHandler();
        }
        throw new IncorrectOperationException("Multiple connection tabs can not return one connection.");
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public DatabaseBrowserTree getBrowserTree() {
        return browserTree;
    }

    @Override
    public void selectElement(BrowserTreeNode treeNode, boolean focus, boolean scroll) {
        browserTree.selectElement(treeNode, focus);
    }

    @Override
    public void rebuildTree() {
        browserTree.getModel().getRoot().rebuildTreeChildren();
    }

    @Override
    public void dispose() {
        super.dispose();
        browserTree = null;
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private ObjectDetailSettingsListener objectDetailSettingsListener = new ObjectDetailSettingsListener() {
        @Override
        public void displayDetailsChanged() {
            browserTree.revalidate();
            browserTree.repaint();
        }
    };
}
