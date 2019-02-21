package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

public class ObjectNavigationListShowAllAction extends DumbAwareAction {
    private DBObjectNavigationList navigationList;
    private DBObject parentObject;

    public ObjectNavigationListShowAllAction(DBObject parentObject, DBObjectNavigationList navigationList) {
        super("Show all...");
        this.parentObject = parentObject;
        this.navigationList = navigationList;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        ObjectNavigationListActionGroup navigationListActionGroup =
                new ObjectNavigationListActionGroup(parentObject, navigationList, true);

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Dependencies",
                navigationListActionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        Project project = ActionUtil.ensureProject(e);
        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        if (activeBrowserTree != null) {
            popup.showInCenterOf(activeBrowserTree);
        }
        //popup.show(DatabaseBrowserComponent.getInstance(project).getBrowserPanel().getTree());
    }
}
