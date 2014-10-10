package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
        addKeyListener(keyListener);
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
        getSelectionModel().setSelectionPath(treePath);
        scrollPathToVisible(treePath);
        if (focus) requestFocus();
        return treePath;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean select) {
        TreePath treePath = getModel().addCompilerMessage(compilerMessage);
        if (select) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
        }
        return treePath;
    }

    public void selectCompilerMessage(CompilerMessage compilerMessage) {
        TreePath treePath = getModel().getTreePath(compilerMessage);
        if (treePath != null) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
        }
    }

    public void selectExecutionMessage(StatementExecutionMessage statementExecutionMessage, boolean focus) {
        TreePath treePath = getModel().getTreePath(statementExecutionMessage);
        if (treePath != null) {
            getSelectionModel().setSelectionPath(treePath);
            scrollPathToVisible(treePath);
            if (focus) requestFocus();
        }
    }

/*
    private void focusTree() {
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
        StatementExecutionSettings statementExecutionSettings = executionEngineSettings.getStatementExecutionSettings();
        if (statementExecutionSettings.isFocusResult()) {
            grabFocus();
        }
    }
*/

    private void navigateToCode(Object object, boolean requestFocus) {
        if (object instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) object;
            StatementExecutionMessage executionMessage = execMessageNode.getExecutionMessage();
            if (!executionMessage.isOrphan()) {
                StatementExecutionResult executionResult = executionMessage.getExecutionResult();
                FileEditorManager editorManager = FileEditorManager.getInstance(executionResult.getProject());
                DBLanguagePsiFile psiFile = executionResult.getExecutionProcessor().getPsiFile();
                if (psiFile != null && psiFile.getVirtualFile() != null) {
                    editorManager.openFile(psiFile.getVirtualFile(), requestFocus);
                    executionMessage.navigateToEditor(requestFocus);
                }
            }
        }
        else if (object instanceof CompilerMessageNode) {
            CompilerMessageNode compilerMessageNode = (CompilerMessageNode) object;
            CompilerMessage compilerMessage = compilerMessageNode.getCompilerMessage();

            if (project != null) {
                CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
                if (sourceAction.isSave() || sourceAction.isCompile() || sourceAction.isBulkCompile()) {
                    DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
                    if (databaseFile != null) {
                        navigateInObjectEditor(compilerMessage, requestFocus);
                    }
                } else if (sourceAction.isDDL()) {
                    VirtualFile virtualFile = sourceAction.getVirtualFile();
                    if (virtualFile instanceof DBConsoleVirtualFile) {
                        DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                        navigateInConsoleEditor(compilerMessage, consoleVirtualFile, requestFocus);
                    } else if (virtualFile != null) {
                        navigateInScriptEditor(compilerMessage, virtualFile, requestFocus);
                    }
                }
            }
        }
    }

    private void navigateInConsoleEditor(CompilerMessage compilerMessage, DBConsoleVirtualFile virtualFile, boolean requestFocus) {
        FileEditorManager editorManager = FileEditorManager.getInstance(compilerMessage.getProject());
        editorManager.openFile(virtualFile, requestFocus);
        Editor editor = compilerMessage.getCompilerResult().getSourceAction().getEditor();
        if (editor == null) {
            FileEditor[] editors = editorManager.getAllEditors(virtualFile);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof SQLConsoleEditor) {
                    SQLConsoleEditor consoleEditor = (SQLConsoleEditor) fileEditor;
                    editor = consoleEditor.getEditor();
                }
            }
        }
        if (editor != null) {
            int lineShifting = 1;
            CharSequence documentText = editor.getDocument().getCharsSequence();
            String objectName = compilerMessage.getObjectName();
            CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
            int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, sourceAction.getStartOffset());
            if (objectStartOffset > -1) {
                lineShifting = editor.getDocument().getLineNumber(objectStartOffset);
            }

            navigateInEditor(editor, compilerMessage, lineShifting);

        }
    }

    private void navigateInScriptEditor(CompilerMessage compilerMessage, VirtualFile virtualFile, boolean requestFocus) {
        Editor editor = compilerMessage.getCompilerResult().getSourceAction().getEditor();
        if (editor == null) {
            FileEditorManager editorManager = FileEditorManager.getInstance(compilerMessage.getProject());
            editorManager.openFile(virtualFile, requestFocus);
            FileEditor[] editors = editorManager.getAllEditors(virtualFile);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof TextEditor) {
                    TextEditor textEditor = (TextEditor) fileEditor;
                    editor = textEditor.getEditor();
                }
            }
        }

        if (editor != null) {
            FileEditor fileEditor = TextEditorProvider.getInstance().getTextEditor(editor);
            EditorUtil.selectEditor(compilerMessage.getProject(), virtualFile, fileEditor, requestFocus);
            int lineShifting = 1;
            CharSequence documentText = editor.getDocument().getCharsSequence();
            String objectName = compilerMessage.getObjectName();
            CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
            int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, sourceAction.getStartOffset());
            if (objectStartOffset > -1) {
                lineShifting = editor.getDocument().getLineNumber(objectStartOffset);
            }

            navigateInEditor(editor, compilerMessage, lineShifting);

        }
    }

    private void navigateInObjectEditor(CompilerMessage compilerMessage, boolean requestFocus) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        if (databaseFile != null && !databaseFile.isDisposed()) {
            editorManager.openFile(databaseFile, requestFocus);
            DBContentVirtualFile contentFile = compilerMessage.getContentFile();
            if (contentFile != null && contentFile instanceof DBSourceCodeVirtualFile) {
                Editor editor = compilerMessage.getCompilerResult().getSourceAction().getEditor();
                BasicTextEditor textEditor = EditorUtil.getTextEditor(databaseFile, (DBSourceCodeVirtualFile) contentFile);
                if (editor == null && textEditor != null) {
                    editor = textEditor.getEditor();
                }

                if (editor != null && textEditor != null) {
                    Document document = editor.getDocument();
                    SourceCodeEditor codeEditor = (SourceCodeEditor) textEditor;
                    int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());

                    navigateInEditor(editor, compilerMessage, lineShifting);

                    EditorUtil.selectEditor(databaseFile.getProject(), databaseFile, textEditor, requestFocus);
                    VirtualFile virtualFile = DocumentUtil.getVirtualFile(textEditor.getEditor());
                    OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                    codeEditor.navigateTo(openFileDescriptor);
                }
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
                SelectionModel selectionModel = editor.getSelectionModel();
                selectionModel.removeSelection();
                if (identifier != null) {
                    int lineEndOffset = document.getLineEndOffset(compilerMessage.getLine() + lineShifting);
                    CharSequence lineText = document.getCharsSequence().subSequence(lineStartOffset, lineEndOffset);
                    int selectionOffsetInLine = StringUtil.indexOfIgnoreCase(lineText, identifier, compilerMessage.getPosition());
                    if (selectionOffsetInLine > -1) {
                        int selectionOffset = selectionOffsetInLine + lineStartOffset;
                        selectionModel.setSelection(selectionOffset, selectionOffset + identifier.length());
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
                navigateToCode(object, false);
                //grabFocus();
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
                    Object value = selectionPath.getLastPathComponent();
                    navigateToCode(value, event.getClickCount() > 1);
                }
            }
        }
    };

    /*********************************************************
     *                        KeyListener                    *
     *********************************************************/
    private KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER ) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath != null) {
                    Object value = selectionPath.getLastPathComponent();
                    navigateToCode(value, true);
                }
            }
        }
    };

    @Override
    public void dispose() {
        project = null;
    }
}
