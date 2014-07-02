package com.dci.intellij.dbn.editor.data.filter.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetFilterList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import java.awt.Component;
import java.awt.Point;

public class CreateFilterAction extends AbstractFilterListAction {
    private DefaultActionGroup actionGroup;

    public CreateFilterAction(DatasetFilterList filterList) {
        super(filterList, "Create filter", Icons.ACTION_ADD);
        actionGroup = new DefaultActionGroup();
        actionGroup.add(new CreateBasicFilterAction(filterList));
        actionGroup.add(new CreateCustomFilterAction(filterList));
    }

    public void actionPerformed(AnActionEvent e) {
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Create filter",
                actionGroup,
                e.getDataContext(),
                JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                true, null, 10);

        //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
        Component component = (Component) e.getInputEvent().getSource();
        showBelowComponent(popup, component);
    }

    private static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX()),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }
}
