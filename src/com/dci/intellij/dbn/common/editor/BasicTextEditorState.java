package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.CodeFoldingState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class BasicTextEditorState implements FileEditorState {
    private int line;
    private int column;
    private int selectionStart;
    private int selectionEnd;
    private float verticalScrollProportion;
    private CodeFoldingState foldingState;

    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof BasicTextEditorState;
    }

    public CodeFoldingState getFoldingState() {
        return foldingState;
    }

    public void setFoldingState(CodeFoldingState foldingState) {
        this.foldingState = foldingState;
    }

    public void readState(@NotNull Element sourceElement, Project project, VirtualFile virtualFile) {
        line = Integer.parseInt(sourceElement.getAttributeValue("line"));
        column = Integer.parseInt(sourceElement.getAttributeValue("column"));
        selectionStart = Integer.parseInt(sourceElement.getAttributeValue("selection-start"));
        selectionEnd = Integer.parseInt(sourceElement.getAttributeValue("selection-end"));
        verticalScrollProportion = Float.parseFloat(sourceElement.getAttributeValue("vertical-scroll-proportion"));

        Element foldingElement = sourceElement.getChild("folding");
        if (foldingElement != null) {
            Document document = DocumentUtil.getDocument(virtualFile);
            foldingState = CodeFoldingManager.getInstance(project).readFoldingState(foldingElement, document);
        }

    }

    public void writeState(Element targetElement, Project project) {
        targetElement.setAttribute("line", Integer.toString(line));
        targetElement.setAttribute("column", Integer.toString(column));
        targetElement.setAttribute("selection-start", Integer.toString(selectionStart));
        targetElement.setAttribute("selection-end", Integer.toString(selectionEnd));
        targetElement.setAttribute("vertical-scroll-proportion", Float.toString(verticalScrollProportion));
        if (foldingState != null) {
            Element foldingElement = new Element("folding");
            targetElement.addContent(foldingElement);
            try {
                CodeFoldingManager.getInstance(project).writeFoldingState(foldingState, foldingElement);
            } catch (WriteExternalException e) {
            }
        }
    }

    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull TextEditor textEditor) {
        Editor editor = textEditor.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        line = logicalPosition.line;
        column = logicalPosition.column;

        if(FileEditorStateLevel.FULL == level) {
            selectionStart = selectionModel.getSelectionStart();
            selectionEnd = selectionModel.getSelectionEnd();

/*            if(project != null){
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                CodeFoldingState foldingState = CodeFoldingManager.getInstance(project).saveFoldingState(editor);
                setFoldingState(foldingState);
            }*/
        }
        verticalScrollProportion = level != FileEditorStateLevel.UNDO ? EditorUtil.calcVerticalScrollProportion(editor) : -1F;
    }

    public void applyToEditor(@NotNull TextEditor textEditor) {
        final Editor editor = textEditor.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();

        LogicalPosition logicalPosition = new LogicalPosition(line, column);
        editor.getCaretModel().moveToLogicalPosition(logicalPosition);
        selectionModel.removeSelection();
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        if (verticalScrollProportion != -1F)
            EditorUtil.setVerticalScrollProportion(editor, verticalScrollProportion);
        Document document = editor.getDocument();
        if (selectionStart == selectionEnd) {
            selectionModel.removeSelection();
        } else {
            int selectionStart = Math.min(this.selectionStart, document.getTextLength());
            int selectionEnd = Math.min(this.selectionEnd, document.getTextLength());
            selectionModel.setSelection(selectionStart, selectionEnd);
        }
        ((EditorEx) editor).stopOptimizedScrolling();
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);


/*
        if (project != null && getFoldingState() != null) {
            PsiDocumentManager.getInstance(project).commitDocument(document);
            new SimpleLaterInvocator() {
                @Override
                public void run() {
                    CodeFoldingManager.getInstance(project).
                            restoreFoldingState(editor, getFoldingState());
                }
            }.start();
            //editor.getFoldingModel().runBatchFoldingOperation(runnable);
        }
*/
    }

}
