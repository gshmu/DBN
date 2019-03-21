package com.dci.intellij.dbn.language.editor.ui;

import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DBLanguageFileEditorToolbarForm extends DBNFormImpl {
    public static final Key<DBLanguageFileEditorToolbarForm> USER_DATA_KEY = new Key<DBLanguageFileEditorToolbarForm>("fileEditorToolbarForm");
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public DBLanguageFileEditorToolbarForm(Project project, VirtualFile file) {
        super(project);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.FileEditor");
        actionToolbar.setTargetComponent(actionsPanel);
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
        ConnectionHandler connectionHandler = mappingManager.getConnectionHandler(file);
        DatabaseSession databaseSession = mappingManager.getDatabaseSession(file);

        autoCommitLabel.init(project, file, connectionHandler, databaseSession);
        Disposer.register(this, autoCommitLabel);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public AutoCommitLabel getAutoCommitLabel() {
        return autoCommitLabel;
    }
}
