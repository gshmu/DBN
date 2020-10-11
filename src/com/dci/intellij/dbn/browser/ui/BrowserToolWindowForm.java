package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.listener.DisplayModeSettingsListener;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.properties.ui.ObjectPropertiesForm;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.ui.GuiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class BrowserToolWindowForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel browserPanel;
    private JPanel closeActionPanel;
    private JPanel objectPropertiesPanel;
    private DatabaseBrowserForm browserForm;

    private BrowserDisplayMode displayMode;
    private final ObjectPropertiesForm objectPropertiesForm;

    public BrowserToolWindowForm(Project project) {
        super(project);
        //toolWindow.setIcon(dbBrowser.getIcon(0));
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);

        rebuild();

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.Controls");
        actionToolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(actionToolbar.getComponent());

        /*ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);*/

        objectPropertiesPanel.setVisible(browserManager.getShowObjectProperties().value());
        objectPropertiesForm = new ObjectPropertiesForm(this);
        objectPropertiesPanel.add(objectPropertiesForm.getComponent());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.7);


        subscribe(DisplayModeSettingsListener.TOPIC, displayModeSettingsListener);
        subscribe(ConnectionSettingsListener.TOPIC, connectionSettingsListener);
    }

    public void rebuild() {
        displayMode = DatabaseBrowserSettings.getInstance(getProject()).getGeneralSettings().getDisplayMode();
        DatabaseBrowserForm oldBrowserForm = this.browserForm;
        TabbedBrowserForm previousTabbedForm =
                oldBrowserForm instanceof TabbedBrowserForm ?
                (TabbedBrowserForm) oldBrowserForm : null;

        this.browserForm =
                displayMode == BrowserDisplayMode.TABBED ? new TabbedBrowserForm(this, previousTabbedForm) :
                displayMode == BrowserDisplayMode.SIMPLE ? new SimpleBrowserForm(this) : null;



        browserPanel.removeAll();
        browserPanel.add(this.browserForm.getComponent(), BorderLayout.CENTER);
        GUIUtil.repaint(browserPanel);
        Disposer.register(this, this.browserForm);
        Disposer.disposeInBackground(oldBrowserForm);
    }

    public DatabaseBrowserTree getBrowserTree(ConnectionId connectionId) {
        if (browserForm instanceof TabbedBrowserForm) {
            TabbedBrowserForm tabbedBrowserForm = (TabbedBrowserForm) browserForm;
            return tabbedBrowserForm.getBrowserTree(connectionId);
        }

        if (browserForm instanceof SimpleBrowserForm) {
            return browserForm.getBrowserTree();
        }

        return null;
    }



    public void showObjectProperties() {
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        BrowserTreeNode treeNode = activeBrowserTree == null ? null : activeBrowserTree.getSelectedNode();
        if (treeNode instanceof DBObject) {
            DBObject object = (DBObject) treeNode;
            objectPropertiesForm.setObject(object);
        }

        objectPropertiesPanel.setVisible(true);
    }

    public void hideObjectProperties() {
        objectPropertiesPanel.setVisible(false);
    }

    public BrowserDisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(BrowserDisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        return browserForm.getBrowserTree();
    }

    public DatabaseBrowserForm getBrowserForm() {
        return browserForm;
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private DisplayModeSettingsListener displayModeSettingsListener = displayMode -> {
        if (getDisplayMode() != displayMode) {
            setDisplayMode(displayMode);
            rebuild();
        }
    };


    private ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsListener() {
        @Override
        public void connectionsChanged() {
            rebuild();
        }

        @Override
        public void connectionNameChanged(ConnectionId connectionId) {
            if (browserForm instanceof TabbedBrowserForm && Failsafe.check(browserForm)) {
                TabbedBrowserForm tabbedBrowserForm = (TabbedBrowserForm) browserForm;
                tabbedBrowserForm.refreshTabInfo(connectionId);
            }
        }

    };
}
