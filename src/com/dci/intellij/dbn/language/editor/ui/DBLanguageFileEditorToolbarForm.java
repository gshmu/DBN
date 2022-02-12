package com.dci.intellij.dbn.language.editor.ui;

import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
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

public class DBLanguageFileEditorToolbarForm extends DBNFormImpl {
    public static final Key<DBLanguageFileEditorToolbarForm> USER_DATA_KEY = new Key<>("fileEditorToolbarForm");
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public DBLanguageFileEditorToolbarForm(Disposable parent, Project project, VirtualFile file) {
        super(parent, project);
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.FileEditor");
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
        ConnectionHandler connectionHandler = mappingManager.getConnection(file);
        DatabaseSession databaseSession = mappingManager.getDatabaseSession(file);

        autoCommitLabel.init(project, file, connectionHandler, databaseSession);
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
