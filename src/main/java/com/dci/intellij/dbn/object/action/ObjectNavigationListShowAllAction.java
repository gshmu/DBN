package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.action.DumbAwareProjectAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

public class ObjectNavigationListShowAllAction extends DumbAwareProjectAction {
    private final DBObjectNavigationList navigationList;
    private final DBObject parentObject;

    ObjectNavigationListShowAllAction(DBObject parentObject, DBObjectNavigationList navigationList) {
        super("Show all...");
        this.parentObject = parentObject;
        this.navigationList = navigationList;
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project) {
        ObjectNavigationListActionGroup navigationListActionGroup =
                new ObjectNavigationListActionGroup(parentObject, navigationList, true);

        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Dependencies",
                navigationListActionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(project);
        DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
        if (activeBrowserTree != null) {
            popup.showInCenterOf(activeBrowserTree);
        }
        //popup.show(DatabaseBrowserComponent.getInstance(project).getBrowserPanel().getTree());
    }
}
