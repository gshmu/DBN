package com.dci.intellij.dbn.editor.console.ui;

import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;
import java.awt.*;

public class SQLConsoleEditorToolbarForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public SQLConsoleEditorToolbarForm(Project project, SQLConsoleEditor fileEditor) {
        super(project);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.FileEditor");
        actionToolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        ConnectionHandler connectionHandler = fileEditor.getVirtualFile().getConnectionHandler();
        autoCommitLabel.setConnectionHandler(connectionHandler);
        Disposer.register(this, autoCommitLabel);
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
