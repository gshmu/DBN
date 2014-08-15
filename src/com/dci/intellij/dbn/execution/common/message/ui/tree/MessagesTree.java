package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.ui.StatementViewerPopup;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

public class MessagesTree extends DBNTree implements TreeSelectionListener, MouseListener {
    public MessagesTree() {
        super(new MessagesTreeModel());
        setCellRenderer(new MessagesTreeCellRenderer());
        addTreeSelectionListener(this);
        addMouseListener(this);
        setRootVisible(false);
        setShowsRootHandles(true);
        Color bgColor = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
    }

    public MessagesTreeModel getModel() {
        return (MessagesTreeModel) super.getModel();
    }

    public void reset() {
        Disposer.dispose(getModel());
        setModel(new MessagesTreeModel());
    }

    public void addExecutionMessage(StatementExecutionMessage executionMessage, boolean focus) {
        TreePath treePath = getModel().addExecutionMessage(executionMessage);
        if (focus) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            requestFocus();
        }
    }

    public void addCompilerMessage(CompilerMessage compilerMessage, boolean focus) {
        TreePath treePath = getModel().addCompilerMessage(compilerMessage);
        if (focus) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            grabFocus();
        }
    }

    public void selectCompilerMessage(CompilerMessage compilerMessage) {
        TreePath treePath = getModel().getTreePath(compilerMessage);
        getSelectionModel().setSelectionPath(treePath);
        scrollPathToVisible(treePath);
        grabFocus();

    }


    public void valueChanged(TreeSelectionEvent event) {
        if (event.isAddedPath()) {
            Object object = event.getPath().getLastPathComponent();
            navigateToCode(object);
            grabFocus();
        }
    }

    private void navigateToCode(Object object) {
        if (object instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) object;
            StatementExecutionMessage executionMessage = execMessageNode.getExecutionMessage();
            if (!executionMessage.isOrphan()) {
                executionMessage.navigateToEditor();
            }
        }
        else if (object instanceof CompilerMessageNode) {
            CompilerMessageNode compilerMessageNode = (CompilerMessageNode) object;
            CompilerMessage compilerMessage = compilerMessageNode.getCompilerMessage();
            FileEditorManager editorManager = FileEditorManager.getInstance(compilerMessage.getProject());

            DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
            if (compilerMessage.isError() || editorManager.isFileOpen(databaseFile)) {
                editorManager.openFile(databaseFile, false);
                if (compilerMessage.getContentFile() != null) {
                    BasicTextEditor textEditor = EditorUtil.getFileEditor(databaseFile, compilerMessage.getContentFile());
                    if (textEditor != null) {
                        navigateInEditor(compilerMessage, textEditor, databaseFile);
                    }
                }
            }
        }
    }

    private void navigateInEditor(CompilerMessage compilerMessage, BasicTextEditor textEditor, DBEditableObjectVirtualFile databaseFile) {
        Editor editor = textEditor.getEditor();
        Document document = editor.getDocument();
        SourceCodeEditor codeEditor = (SourceCodeEditor) textEditor;
        int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());

        if (document.getLineCount() <= compilerMessage.getLine()) {
            compilerMessage.setLine(0);
            compilerMessage.setPosition(0);
            compilerMessage.setSubjectIdentifier(null);
        }
        int lineStartOffset = document.getLineStartOffset(compilerMessage.getLine() + lineShifting);
        int newCaretOffset = lineStartOffset + compilerMessage.getPosition();
        if (document.getTextLength() > newCaretOffset) {
            editor.getCaretModel().moveToOffset(newCaretOffset);

            String identifier = compilerMessage.getSubjectIdentifier();
            if (identifier != null) {
                int lineEndOffset = document.getLineEndOffset(compilerMessage.getLine() + lineShifting);
                String lineText = document.getText().substring(lineStartOffset, lineEndOffset).toUpperCase();
                int selectionOffset = lineText.indexOf(identifier, compilerMessage.getPosition()) + lineStartOffset;
                if (selectionOffset > -1) {
                    editor.getSelectionModel().setSelection(selectionOffset, selectionOffset + identifier.length());
                }
            }
            editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
        }

        EditorUtil.selectEditor(databaseFile, textEditor);
        VirtualFile virtualFile = DocumentUtil.getVirtualFile(textEditor.getEditor());
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(compilerMessage.getProject(), virtualFile);
        codeEditor.navigateTo(openFileDescriptor);
    }

    /*********************************************************
     *                        MouseListener                  *
     *********************************************************/
    public void mouseClicked(MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON1) {
            TreePath selectionPath = getSelectionPath();
            if (selectionPath != null) {
                if (event.getClickCount() > 1 ) {
                    Object value = selectionPath.getLastPathComponent();
                    if (value instanceof StatementExecutionMessageNode) {
                        StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) value;
                        StatementExecutionResult executionResult = execMessageNode.getExecutionMessage().getExecutionResult();
                        StatementViewerPopup statementViewer = new StatementViewerPopup(executionResult);
                        statementViewer.show(event.getComponent(), event.getPoint());
                        event.consume();
                    }
                } else {
                    Object value = selectionPath.getLastPathComponent();
                    navigateToCode(value);
                }
            }
        }
    }
    public void mousePressed(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    public void mouseReleased(MouseEvent event) {}
}
