package com.dci.intellij.dbn.language.editor.ui;

import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DBLanguageFileEditorToolbarForm extends DBNFormBase {
    public static final Key<DBLanguageFileEditorToolbarForm> USER_DATA_KEY = new Key<>("fileEditorToolbarForm");
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public DBLanguageFileEditorToolbarForm(Disposable parent, Project project, VirtualFile file) {
        super(parent, project);
        this.mainPanel.setBorder(Borders.insetBorder(2));

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.FileEditor");
        this.actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(file);
        DatabaseSession databaseSession = contextManager.getDatabaseSession(file);

        this.autoCommitLabel.init(project, file, connection, databaseSession);
        Disposer.register(this, autoCommitLabel);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public AutoCommitLabel getAutoCommitLabel() {
        return autoCommitLabel;
    }
}
