package com.dci.intellij.dbn.editor.console;

import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

class SQLConsoleEditorState extends BasicTextEditorState {
    private String currentSchema = "";
    private SessionId sessionId = SessionId.MAIN;

    @Override
    public void writeState(Element targetElement, Project project) {
        super.writeState(targetElement, project);
        targetElement.setAttribute("current-schema", CommonUtil.nvl(currentSchema, ""));
        targetElement.setAttribute("session-id", sessionId.id());
    }

    @Override
    public void readState(@NotNull Element sourceElement, Project project, VirtualFile virtualFile) {
        super.readState(sourceElement, project, virtualFile);
        currentSchema = sourceElement.getAttributeValue("current-schema");
        sessionId = CommonUtil.nvl(SessionId.get(sourceElement.getAttributeValue("session-id")), SessionId.MAIN);
    }

    @Override
    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        super.loadFromEditor(level, textEditor);
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        if (file != null) {
            currentSchema = file.getDatabaseSchemaName();
            DatabaseSession databaseSession = file.getDatabaseSession();
            sessionId = databaseSession == null ? SessionId.MAIN : databaseSession.getId();
        }

        //content = textEditor.getEditor().getDocument().getText();
    }

    @Override
    public void applyToEditor(@NotNull final TextEditor textEditor) {
        super.applyToEditor(textEditor);
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        if (file != null && StringUtil.isNotEmpty(currentSchema)) {
            file.setDatabaseSchemaName(currentSchema);
            file.setDatabaseSessionId(sessionId);
        }
    }
}
