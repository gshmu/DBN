package com.dci.intellij.dbn.common.action;

import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DataProviderSupplier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;

public abstract class GroupPopupAction extends DumbAwareAction {
    private String groupTitle;
    public GroupPopupAction(String name, @Nullable String groupTitle, @Nullable Icon icon) {
        super(name, null, icon);
        this.groupTitle = groupTitle;
    }

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        for (AnAction action : getActions(e)) {
            actionGroup.add(action);
        }
        InputEvent inputEvent = e.getInputEvent();
        if (inputEvent != null) {
            Component component = (Component) inputEvent.getSource();
            if (component.isShowing()) {
                ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                        groupTitle,
                        actionGroup,
                        e.getDataContext(),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true, null, 10);

                //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
                DataProviderSupplier dataProvider = getDataProviderSupplier(e);
                if (dataProvider != null) {
                    ActionUtil.registerDataProvider(popup.getContent(), dataProvider);
                }
                showBelowComponent(popup, component);
            }
        }
    }

    public DataProviderSupplier getDataProviderSupplier(AnActionEvent e) {
        return null;
    }

    public static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX() + 10),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }

    protected abstract AnAction[] getActions(AnActionEvent e);
}
