package com.dci.intellij.dbn.editor.console;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

class SQLConsoleEditorState extends BasicTextEditorState {
    private String currentSchema = "";

    @Override
    public void writeState(Element targetElement, Project project) {
        super.writeState(targetElement, project);
        targetElement.setAttribute("current-schema", CommonUtil.nvl(currentSchema, ""));
    }

    @Override
    public void readState(@NotNull Element sourceElement, Project project, VirtualFile virtualFile) {
        super.readState(sourceElement, project, virtualFile);
        currentSchema = sourceElement.getAttributeValue("current-schema");
    }

    @Override
    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        super.loadFromEditor(level, textEditor);
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        if (file != null) {
            currentSchema = file.getCurrentSchemaName();
        }

        //content = textEditor.getEditor().getDocument().getText();
    }

    @Override
    public void applyToEditor(@NotNull final TextEditor textEditor) {
        super.applyToEditor(textEditor);
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        if (file != null && StringUtil.isNotEmpty(currentSchema)) {
            file.setCurrentSchemaName(currentSchema);
        }
    }
}
