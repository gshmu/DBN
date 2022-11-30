package com.dci.intellij.dbn.editor.console.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SQLConsoleEditorToolbarForm extends DBNFormBase {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public SQLConsoleEditorToolbarForm(Project project, SQLConsoleEditor fileEditor) {
        super(fileEditor, project);
        this.mainPanel.setBorder(Borders.insetBorder(2));

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.FileEditor");
        this.actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        DBConsoleVirtualFile file = fileEditor.getVirtualFile();
        ConnectionHandler connection = file.getConnection();
        DatabaseSession session = file.getSession();
        this.autoCommitLabel.init(project, file, connection, session);
        Disposer.register(this, autoCommitLabel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
