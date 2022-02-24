package com.dci.intellij.dbn.editor.console.ui;

import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class SQLConsoleEditorToolbarForm extends DBNFormImpl {
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
