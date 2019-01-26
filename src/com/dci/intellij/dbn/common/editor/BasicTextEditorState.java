package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.CodeFoldingState;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class BasicTextEditorState implements FileEditorState {
    private int line;
    private int column;
    private int selectionStart;
    private int selectionEnd;
    private float verticalScrollProportion;
    private CodeFoldingState foldingState;

    @Override
    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return fileEditorState instanceof BasicTextEditorState;
    }

    public CodeFoldingState getFoldingState() {
        return foldingState;
    }

    public void setFoldingState(CodeFoldingState foldingState) {
        this.foldingState = foldingState;
    }

    public void readState(@NotNull Element sourceElement, final Project project, final VirtualFile virtualFile) {
        line = Integer.parseInt(sourceElement.getAttributeValue("line"));
        column = Integer.parseInt(sourceElement.getAttributeValue("column"));
        selectionStart = Integer.parseInt(sourceElement.getAttributeValue("selection-start"));
        selectionEnd = Integer.parseInt(sourceElement.getAttributeValue("selection-end"));
        verticalScrollProportion = Float.parseFloat(sourceElement.getAttributeValue("vertical-scroll-proportion"));

        Element foldingElement = sourceElement.getChild("folding");
        if (foldingElement != null) {
            ReadActionRunner.invoke(false, () -> {
                Document document = DocumentUtil.getDocument(virtualFile);
                CodeFoldingManager instance = CodeFoldingManager.getInstance(project);
                if (document != null) {
                    CodeFoldingState foldingState = instance.readFoldingState(foldingElement, document);
                    setFoldingState(foldingState);
                }
                return null;
            });
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
            } catch (WriteExternalException ignore) { // TODO
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void loadFromEditor(@NotNull FileEditorStateLevel level, @NotNull final TextEditor textEditor) {
        Editor editor = textEditor.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();
        LogicalPosition logicalPosition = editor.getCaretModel().getLogicalPosition();

        line = logicalPosition.line;
        column = logicalPosition.column;

        if(FileEditorStateLevel.FULL == level) {
            selectionStart = selectionModel.getSelectionStart();
            selectionEnd = selectionModel.getSelectionEnd();
            Project project = editor.getProject();
            if (project != null && !editor.isDisposed()) {
                foldingState = CodeFoldingManager.getInstance(project).saveFoldingState(editor);
            }

/*
            new WriteActionRunner() {
                @Override
                public void run() {
                    Editor editor = textEditor.getEditor();
                    Project project = editor.getProject();
                    if (project != null && !editor.isDisposed()) {
                        try {
                            PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
                        } catch (ProcessCanceledException ignore) {
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        foldingState = CodeFoldingManager.getInstance(project).saveFoldingState(editor);
                    }
                }
            }.start();
*/
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
        final Document document = editor.getDocument();
        if (selectionStart == selectionEnd) {
            selectionModel.removeSelection();
        } else {
            int selectionStart = Math.min(this.selectionStart, document.getTextLength());
            int selectionEnd = Math.min(this.selectionEnd, document.getTextLength());
            selectionModel.setSelection(selectionStart, selectionEnd);
        }
        editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);

        if (foldingState != null) {
            WriteActionRunner.invoke(() -> {
                Project project = editor.getProject();
                if (project != null) {
                    try {
                        PsiDocumentManager.getInstance(project).commitDocument(document);
                    } catch (ProcessCanceledException ignore) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CodeFoldingManager.getInstance(project).
                            restoreFoldingState(editor, getFoldingState());
                }
            });
            //editor.getFoldingModel().runBatchFoldingOperation(runnable);
        }
    }

    /*****************************************************************
     *                     equals / hashCode                         *
     *****************************************************************/
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicTextEditorState that = (BasicTextEditorState) o;

        if (line != that.line) return false;
        if (column != that.column) return false;
        if (selectionStart != that.selectionStart) return false;
        if (selectionEnd != that.selectionEnd) return false;
        if (Float.compare(that.verticalScrollProportion, verticalScrollProportion) != 0) return false;
        if (foldingState != null ? !foldingState.equals(that.foldingState) : that.foldingState != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        result = 31 * result + selectionStart;
        result = 31 * result + selectionEnd;
        result = 31 * result + (verticalScrollProportion != +0.0f ? Float.floatToIntBits(verticalScrollProportion) : 0);
        result = 31 * result + (foldingState != null ? foldingState.hashCode() : 0);
        return result;
    }
}
