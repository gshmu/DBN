package com.dci.intellij.dbn.editor.console;

import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.editor.BasicTextEditorState;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class SQLConsoleEditorState extends BasicTextEditorState {
    @Deprecated
    private String content = "";
    private String currentSchema = "";

    @Override
    public void writeState(Element targetElement, Project project) {
        super.writeState(targetElement, project);
        targetElement.setAttribute("current-schema", currentSchema);
/*
        Element contentElement = new Element("content");
        contentElement.setAttribute("current-schema", currentSchema);
        targetElement.addContent(contentElement);
        String content = StringUtil.replace(this.content, "\n", "<br>");
        content = StringUtil.replace(content, "  ", "<sp>");
        CDATA cdata = new CDATA(content);
        contentElement.setContent(cdata);
*/

    }

    @Override
    public void readState(@NotNull Element sourceElement, Project project, VirtualFile virtualFile) {
        super.readState(sourceElement, project, virtualFile);
        currentSchema = sourceElement.getAttributeValue("current-schema");

        // TODO remove (backward compatibility)
        Element contentElement = sourceElement.getChild("content");
        if (contentElement != null) {
            currentSchema = contentElement.getAttributeValue("current-schema");
            if (contentElement.getContentSize() > 0) {

                Content content = contentElement.getContent(0);
                String textContent = "";
                if (content instanceof Text) {
                    Text cdata = (Text) content;
                    textContent = cdata.getText();
                }

                textContent = StringUtil.replace(textContent, "<br>", "\n");
                textContent = StringUtil.replace(textContent, "<sp>", "  ");
                this.content = textContent;
            }
        }
    }

    @Override
    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        super.loadFromEditor(level, textEditor);
        DBConsoleVirtualFile file = (DBConsoleVirtualFile) DocumentUtil.getVirtualFile(textEditor.getEditor());
        if (file != null) {
            DBSchema schema = file.getCurrentSchema();
            currentSchema = schema == null ? "" : schema.getName();
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

        // TODO remove (backward compatibility)
        new WriteActionRunner() {
            public void run() {
                Document document = textEditor.getEditor().getDocument();

                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                if (virtualFile != null && StringUtil.isNotEmpty(content)) {
                    document.setText(CommonUtil.nvl(content, ""));
                }
            }
        }.start();
    }
}
