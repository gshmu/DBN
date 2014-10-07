package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
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
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, boolean focus) {
        TreePath treePath = getModel().addExecutionMessage(executionMessage);
        if (focus) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            requestFocus();
        }
        return treePath;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean focus) {
        TreePath treePath = getModel().addCompilerMessage(compilerMessage);
        if (focus) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            grabFocus();
        }
        return treePath;
    }

    public void focus(CompilerMessage compilerMessage) {
        TreePath treePath = getModel().getTreePath(compilerMessage);
        if (treePath != null) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            grabFocus();
        }
    }

    public void focus(StatementExecutionMessage statementExecutionMessage) {
        TreePath treePath = getModel().getTreePath(statementExecutionMessage);
        if (treePath != null) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
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

            if (project != null) {
                FileEditorManager editorManager = FileEditorManager.getInstance(project);

                CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
                CompilerAction.Type sourceActionType = sourceAction.getType();
                if (sourceActionType == CompilerAction.Type.SAVE || sourceActionType == CompilerAction.Type.COMPILE) {
                    DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
                    if (compilerMessage.isError() || editorManager.isFileOpen(databaseFile)) {
                        editorManager.openFile(databaseFile, false);
                        navigateInObjectEditor(compilerMessage);
                    }
                } else if (sourceActionType == CompilerAction.Type.DDL) {
                    VirtualFile virtualFile = sourceAction.getVirtualFile();
                    if (virtualFile != null) {
                        editorManager.openFile(virtualFile, false);
                        navigateInScriptEditor(compilerMessage, virtualFile, sourceAction.getStartOffset());
                    }
                }
            }
        }
    }

    private void navigateInScriptEditor(CompilerMessage compilerMessage, VirtualFile virtualFile, int startOffset) {
        FileEditorManager editorManager = FileEditorManager.getInstance(compilerMessage.getProject());
        FileEditor[] editors = editorManager.getAllEditors(virtualFile);
        for (FileEditor fileEditor : editors) {
            if (fileEditor instanceof TextEditor) {
                TextEditor textEditor = (TextEditor) fileEditor;
                Editor editor = textEditor.getEditor();

                int lineShifting = 1;
                CharSequence documentText = editor.getDocument().getCharsSequence();
                String objectName = compilerMessage.getObjectName();
                int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, startOffset);
                if (objectStartOffset > -1) {
                    lineShifting = editor.getDocument().getLineNumber(objectStartOffset);
                }

                navigateInEditor(editor, compilerMessage, lineShifting);
            }
        }
    }

    private void navigateInObjectEditor(CompilerMessage compilerMessage) {
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        DBContentVirtualFile contentFile = compilerMessage.getContentFile();
        if (contentFile != null && contentFile instanceof DBSourceCodeVirtualFile) {
            BasicTextEditor textEditor = EditorUtil.getTextEditor(databaseFile, (DBSourceCodeVirtualFile) contentFile);
            if (textEditor != null) {
                Editor editor = textEditor.getEditor();
                Document document = editor.getDocument();
                SourceCodeEditor codeEditor = (SourceCodeEditor) textEditor;
                int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());

                navigateInEditor(editor, compilerMessage, lineShifting);

                EditorUtil.selectEditor(databaseFile, textEditor);
                VirtualFile virtualFile = DocumentUtil.getVirtualFile(textEditor.getEditor());
                OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                codeEditor.navigateTo(openFileDescriptor);
            }
        }
    }

    private void navigateInEditor(Editor editor, CompilerMessage compilerMessage, int lineShifting) {
        Document document = editor.getDocument();
        if (document.getLineCount() > compilerMessage.getLine() + lineShifting) {
            int lineStartOffset = document.getLineStartOffset(compilerMessage.getLine() + lineShifting);
            int newCaretOffset = lineStartOffset + compilerMessage.getPosition();
            if (document.getTextLength() > newCaretOffset) {
                editor.getCaretModel().moveToOffset(newCaretOffset);

                String identifier = compilerMessage.getSubjectIdentifier();
                if (identifier != null) {
                    int lineEndOffset = document.getLineEndOffset(compilerMessage.getLine() + lineShifting);
                    CharSequence lineText = document.getCharsSequence().subSequence(lineStartOffset, lineEndOffset);
                    int selectionOffsetInLine = StringUtil.indexOfIgnoreCase(lineText, identifier, compilerMessage.getPosition());
                    if (selectionOffsetInLine > -1) {
                        int selectionOffset = selectionOffsetInLine + lineStartOffset;
                        editor.getSelectionModel().setSelection(selectionOffset, selectionOffset + identifier.length());
                    }
                }
                editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
            }
        }
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
