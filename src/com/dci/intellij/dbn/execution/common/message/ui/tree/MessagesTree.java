package com.dci.intellij.dbn.execution.common.message.ui.tree;

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

    public TreePath addExecutionMessage(StatementExecutionMessage executionMessage, boolean select, boolean focus) {
        TreePath treePath = getModel().addExecutionMessage(executionMessage);
        scrollPathToVisible(treePath);
        if (select) {
            getSelectionModel().setSelectionPath(treePath);
        }
        if (focus) requestFocus();
        return treePath;
    }

    public TreePath addCompilerMessage(CompilerMessage compilerMessage, boolean select) {
        TreePath treePath = getModel().addCompilerMessage(compilerMessage);
        scrollPathToVisible(treePath);
        if (select) {
            getSelectionModel().setSelectionPath(treePath);
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
                FileEditorManager editorManager = FileEditorManager.getInstance(project);
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

    private void navigateInConsoleEditor(CompilerMessage compilerMessage, DBConsoleVirtualFile virtualFile, boolean requestFocus) {
        FileEditorManager editorManager = getFileEditorManager();
        editorManager.openFile(virtualFile, requestFocus);
        FileEditor consoleFileEditor = compilerMessage.getCompilerResult().getSourceAction().getFileEditor();
        if (consoleFileEditor == null) {
            FileEditor[] fileEditors = editorManager.getAllEditors(virtualFile);
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof SQLConsoleEditor) {
                    consoleFileEditor = fileEditor;
                }
            }
        }
        if (consoleFileEditor != null) {
            Editor editor = EditorUtil.getEditor(consoleFileEditor);
            if (editor != null) {
                int lineShifting = 1;
                Document document = editor.getDocument();
                CharSequence documentText = document.getCharsSequence();
                String objectName = compilerMessage.getObjectName();
                CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
                int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, sourceAction.getStartOffset());
                if (objectStartOffset > -1) {
                    lineShifting = document.getLineNumber(objectStartOffset);
                }

                navigateInEditor(editor, compilerMessage, lineShifting);
            }

        }
    }

    private FileEditorManager getFileEditorManager() {
        return FileEditorManager.getInstance(project);
    }

    private void navigateInScriptEditor(CompilerMessage compilerMessage, VirtualFile virtualFile, boolean requestFocus) {
        FileEditor scriptFileEditor = compilerMessage.getCompilerResult().getSourceAction().getFileEditor();
        if (scriptFileEditor == null) {
            FileEditorManager editorManager = getFileEditorManager();
            editorManager.openFile(virtualFile, requestFocus);
            FileEditor[] editors = editorManager.getAllEditors(virtualFile);
            for (FileEditor fileEditor : editors) {
                if (fileEditor instanceof TextEditor) {
                    scriptFileEditor = fileEditor;
                }
            }
        }

        if (scriptFileEditor != null) {
            Editor editor = EditorUtil.getEditor(scriptFileEditor);
            if (editor != null) {
                EditorUtil.selectEditor(project, virtualFile, scriptFileEditor, requestFocus);
                int lineShifting = 1;
                Document document = editor.getDocument();
                CharSequence documentText = document.getCharsSequence();
                String objectName = compilerMessage.getObjectName();
                CompilerAction sourceAction = compilerMessage.getCompilerResult().getSourceAction();
                int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, sourceAction.getStartOffset());
                if (objectStartOffset > -1) {
                    lineShifting = document.getLineNumber(objectStartOffset);
                }

                navigateInEditor(editor, compilerMessage, lineShifting);
            }
        }
    }

    private void navigateInObjectEditor(CompilerMessage compilerMessage, boolean requestFocus) {
        FileEditorManager editorManager = getFileEditorManager();
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        if (databaseFile != null && !databaseFile.isDisposed()) {
            editorManager.openFile(databaseFile, requestFocus);
            DBContentVirtualFile contentFile = compilerMessage.getContentFile();
            if (contentFile != null && contentFile instanceof DBSourceCodeVirtualFile) {
                FileEditor objectFileEditor = compilerMessage.getCompilerResult().getSourceAction().getFileEditor();
                if (objectFileEditor == null) {
                    FileEditor[] fileEditors = getFileEditorManager().getAllEditors(databaseFile);
                    for (FileEditor fileEditor : fileEditors) {
                        if (fileEditor instanceof SourceCodeEditor) {
                            SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                            if (sourceCodeEditor.getContentType() == compilerMessage.getContentType()) {
                                objectFileEditor = sourceCodeEditor;
                                break;
                            }
                        }
                    }
                }

                if (objectFileEditor != null && objectFileEditor instanceof SourceCodeEditor) {
                    SourceCodeEditor codeEditor = (SourceCodeEditor) objectFileEditor;
                    Editor editor = EditorUtil.getEditor(codeEditor);
                    if (editor != null) {
                        Document document = editor.getDocument();
                        int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());
                        navigateInEditor(editor, compilerMessage, lineShifting);
                        EditorUtil.selectEditor(project, databaseFile, objectFileEditor, requestFocus);
                        VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
                        OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                        codeEditor.navigateTo(openFileDescriptor);
                    }

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
                navigateToCode(object, true);
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
