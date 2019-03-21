package com.dci.intellij.dbn.object.factory.ui.common;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ObjectListItemForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel removeActionPanel;
    private JPanel objectDetailsComponent;

    private ObjectListForm parent;
    private ObjectFactoryInputForm inputForm;

    ObjectListItemForm(ObjectListForm parent, ObjectFactoryInputForm inputForm) {
        this.parent = parent;
        this.inputForm = inputForm;
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ObjectFactory.AddElement", true,
                new RemoveObjectAction());
        removeActionPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);

    }

    @NotNull
    @Override
    public JPanel getComponent(){
        return mainPanel;
    }

    private void createUIComponents() {
        objectDetailsComponent = inputForm.getComponent();
    }

    public class RemoveObjectAction extends AnAction {
        RemoveObjectAction() {
            super("Remove " + parent.getObjectType().getName(), null, Icons.ACTION_CLOSE);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            parent.removeObjectPanel(ObjectListItemForm.this);
        }
    }

    ObjectFactoryInputForm getObjectDetailsPanel() {
        return inputForm;
    }
}
