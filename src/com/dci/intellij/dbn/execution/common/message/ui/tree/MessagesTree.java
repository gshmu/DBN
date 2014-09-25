package com.dci.intellij.dbn.execution.common.message.ui.tree;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.event.MouseAdapter;
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
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

public class MessagesTree extends DBNTree implements Disposable {
    private Project project;
    public MessagesTree(Project project) {
        super(new MessagesTreeModel());
        this.project = project;
        setCellRenderer(new MessagesTreeCellRenderer());
        addTreeSelectionListener(treeSelectionListener);
        addMouseListener(mouseListener);
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

            if (project != null) {
                FileEditorManager editorManager = FileEditorManager.getInstance(project);

                DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
                if (compilerMessage.isError() || editorManager.isFileOpen(databaseFile)) {
                    editorManager.openFile(databaseFile, false);
                    DBContentVirtualFile contentFile = compilerMessage.getContentFile();
                    if (contentFile != null && contentFile instanceof DBSourceCodeVirtualFile) {
                        BasicTextEditor textEditor = EditorUtil.getTextEditor(databaseFile, (DBSourceCodeVirtualFile) contentFile);
                        if (textEditor != null) {
                            navigateInEditor(compilerMessage, textEditor, databaseFile);
                        }
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
        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
        codeEditor.navigateTo(openFileDescriptor);
    }

    /*********************************************************
     *                   TreeSelectionListener               *
     *********************************************************/
    private TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent event) {
            if (event.isAddedPath()) {
                Object object = event.getPath().getLastPathComponent();
                navigateToCode(object);
                grabFocus();
            }
        }
    };


    /*********************************************************
     *                        MouseListener                  *
     *********************************************************/
    private MouseListener mouseListener = new MouseAdapter() {
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
    };

    @Override
    public void dispose() {
        project = null;
    }
}
