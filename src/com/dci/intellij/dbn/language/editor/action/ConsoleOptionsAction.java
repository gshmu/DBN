package com.dci.intellij.dbn.language.editor.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.code.common.completion.options.CodeCompletionSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.options.ui.GlobalProjectSettingsDialog;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;

public class ConsoleOptionsAction extends DumbAwareAction {
    public ConsoleOptionsAction() {
        super("Options", null, Icons.ACTION_LOCAL_SETTINGS);
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        DefaultActionGroup actionGroup = new DefaultActionGroup();

        actionGroup.add(new SaveToFileEditorAction());
        actionGroup.add(new RenameConsoleEditorAction());
        actionGroup.add(new DeleteConsoleEditorAction());
        ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                "Options",
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
                (int) (locationOnScreen.getX() + 10),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }


    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.ACTION_OPTIONS);
        presentation.setText("Options");
    }
}
