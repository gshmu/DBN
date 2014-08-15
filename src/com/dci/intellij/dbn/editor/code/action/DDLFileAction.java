package com.dci.intellij.dbn.editor.code.action;

import java.awt.Component;
import java.awt.Point;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.ddl.action.AttachDDLFileAction;
import com.dci.intellij.dbn.ddl.action.CreateDDLFileAction;
import com.dci.intellij.dbn.ddl.action.DetachDDLFileAction;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

public class DDLFileAction extends AbstractSourceCodeEditorAction {
    DefaultActionGroup actionGroup;
    public DDLFileAction() {
        super("DDL File", null, Icons.CODE_EDITOR_DDL_FILE);
    }

    public void actionPerformed(AnActionEvent e) {
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        if (sourcecodeFile != null) {
            DBSchemaObject object = sourcecodeFile.getObject();
            actionGroup = new DefaultActionGroup();
            actionGroup.add(new CreateDDLFileAction(object));
            actionGroup.add(new AttachDDLFileAction(object));
            actionGroup.add(new DetachDDLFileAction(object));

            ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                    "DDL File",
                    actionGroup,
                    e.getDataContext(),
                    JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                    true, null, 10);

            //Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
            Component component = (Component) e.getInputEvent().getSource();
            showBelowComponent(popup, component);
        }
    }

    public void update(AnActionEvent e) {
        DBSourceCodeVirtualFile sourcecodeFile = getSourcecodeFile(e);
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.CODE_EDITOR_DDL_FILE);
        presentation.setText("DDL Files");
        presentation.setEnabled(sourcecodeFile != null);
    }

    private static void showBelowComponent(ListPopup popup, Component component) {
        Point locationOnScreen = component.getLocationOnScreen();
        Point location = new Point(
                (int) (locationOnScreen.getX()),
                (int) locationOnScreen.getY() + component.getHeight());
        popup.showInScreenCoordinates(component, location);
    }
}
