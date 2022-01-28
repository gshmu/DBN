package com.dci.intellij.dbn.browser.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.options.BrowserDisplayMode;
import com.dci.intellij.dbn.browser.options.DatabaseBrowserSettings;
import com.dci.intellij.dbn.browser.options.listener.DisplayModeSettingsListener;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.properties.ui.ObjectPropertiesForm;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class BrowserToolWindowForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel browserPanel;
    private JPanel closeActionPanel;
    private JPanel objectPropertiesPanel;
    private @Getter DatabaseBrowserForm browserForm;

    private @Getter @Setter BrowserDisplayMode displayMode;
    private final ObjectPropertiesForm objectPropertiesForm;

    public BrowserToolWindowForm(Disposable parent, @NotNull Project project) {
        super(parent, project);
        //toolWindow.setIcon(dbBrowser.getIcon(0));
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);

        rebuild();

        ActionToolbar actionToolbar = Actions.createActionToolbar(
                actionsPanel,
                "",
                true,
                "DBNavigator.ActionGroup.Browser.Controls");
        actionsPanel.add(actionToolbar.getComponent());

        /*ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", false, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);*/

        objectPropertiesPanel.setVisible(browserManager.getShowObjectProperties().value());
        objectPropertiesForm = new ObjectPropertiesForm(this);
        objectPropertiesPanel.add(objectPropertiesForm.getComponent());


        ProjectEvents.subscribe(project, this, DisplayModeSettingsListener.TOPIC, displayModeSettingsListener);
        ProjectEvents.subscribe(project, this, ConnectionSettingsListener.TOPIC, connectionSettingsListener);
    }

    public void rebuild() {
        Project project = ensureProject();
        DatabaseBrowserSettings browserSettings = DatabaseBrowserSettings.getInstance(project);
        displayMode = browserSettings.getGeneralSettings().getDisplayMode();
        DatabaseBrowserForm oldBrowserForm = this.browserForm;
        TabbedBrowserForm previousTabbedForm =
                oldBrowserForm instanceof TabbedBrowserForm ?
                (TabbedBrowserForm) oldBrowserForm : null;

        this.browserForm =
                displayMode == BrowserDisplayMode.TABBED ? new TabbedBrowserForm(project, previousTabbedForm) :
                displayMode == BrowserDisplayMode.SIMPLE ? new SimpleBrowserForm(project) : null;



        browserPanel.removeAll();
        browserPanel.add(this.browserForm.getComponent(), BorderLayout.CENTER);
        GUIUtil.repaint(browserPanel);

        SafeDisposer.dispose(oldBrowserForm, true, true);
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
        Project project = ensureProject();
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
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

    @Nullable
    public DatabaseBrowserTree getActiveBrowserTree() {
        return browserForm.getBrowserTree();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    /********************************************************
     *                       Listeners                      *
     ********************************************************/
    private final DisplayModeSettingsListener displayModeSettingsListener = displayMode -> {
        if (getDisplayMode() != displayMode) {
            setDisplayMode(displayMode);
            rebuild();
        }
    };


    private final ConnectionSettingsListener connectionSettingsListener = new ConnectionSettingsListener() {
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

    @Override
    protected void disposeInner() {
        SafeDisposer.dispose(browserForm);
        super.disposeInner();
    }
}
