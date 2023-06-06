package com.dci.intellij.dbn.language.editor.ui;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.form.DBNToolbarForm;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Actions;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DBLanguageFileEditorToolbarForm extends DBNToolbarForm {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private AutoCommitLabel autoCommitLabel;

    public DBLanguageFileEditorToolbarForm(FileEditor fileEditor, Project project, VirtualFile file) {
        super(fileEditor, project);
        this.mainPanel.setBorder(Borders.insetBorder(2));

        ActionToolbar actionToolbar = Actions.createActionToolbar(actionsPanel,"", true, "DBNavigator.ActionGroup.FileEditor");
        this.actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(project);
        ConnectionHandler connection = contextManager.getConnection(file);
        DatabaseSession databaseSession = contextManager.getDatabaseSession(file);

        this.autoCommitLabel.init(project, file, connection, databaseSession);
        Disposer.register(this, autoCommitLabel);
    }

    @Nullable
    @Override
    public Object getData(@NotNull String dataId) {
        FileEditor fileEditor = ensureParentComponent();
        if (fileEditor instanceof TextEditor) {
            TextEditor textEditor = (TextEditor) fileEditor;
            if (PlatformDataKeys.VIRTUAL_FILE.is(dataId)) return textEditor.getFile();
            if (PlatformDataKeys.FILE_EDITOR.is(dataId))  return textEditor;
            if (PlatformDataKeys.EDITOR.is(dataId)) return textEditor.getEditor();
        }

        return null;
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
