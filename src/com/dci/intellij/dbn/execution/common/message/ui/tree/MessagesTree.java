package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.common.util.TextAttributesUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerMessage;
import com.dci.intellij.dbn.execution.explain.result.ExplainPlanMessage;
import com.dci.intellij.dbn.execution.statement.StatementExecutionMessage;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MessagesTree extends DBNTree implements Disposable {
    private Project project;
    private boolean ignoreSelectionEvent = false;
    public MessagesTree(Project project) {
        super(new MessagesTreeModel());
        this.project = project;
        setCellRenderer(new MessagesTreeCellRenderer());
        addTreeSelectionListener(treeSelectionListener);
        addMouseListener(mouseListener);
        addKeyListener(keyListener);
        setRootVisible(false);
        setShowsRootHandles(true);
        setOpaque(false);
        Color bgColor = TextAttributesUtil.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
    }

    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i=0; i<getRowCount();i++){
            TreePath treePath = getPathForRow(i);
            if (!isRowSelected(i)) {
                Object lastPathComponent = treePath.getLastPathComponent();
                if (lastPathComponent instanceof MessageTreeNode) {
                    MessageTreeNode node = (MessageTreeNode) lastPathComponent;
                    if (!node.isDisposed() && node.getMessage().isNew()) {
                        Rectangle r = getRowBounds(i);
                        g.setColor(MessagesTreeCellRenderer.HIGHLIGHT_BACKGROUND);
                        g.fillRect(0, r.y, getWidth(), r.height);
                    }
                }
            }
        }
        //super.paintComponent(g);
        if (ui != null) {
            Graphics scratchGraphics = g.create();
            try {
                ui.update(scratchGraphics, this);
            }
            finally {
                scratchGraphics.dispose();
            }
        }
    }

    @Override
    public MessagesTreeModel getModel() {
        return (MessagesTreeModel) super.getModel();
    }

    public void resetMessagesStatus() {
        getModel().resetMessagesStatus();
    }

    public void reset() {
        MessagesTreeModel oldModel = getModel();
        setModel(new MessagesTreeModel());
        Disposer.dispose(oldModel);
    }

    public TreePath addExecutionMessage(StatementExecutionMessage message, boolean select, boolean focus) {
        TreePath treePath = getModel().addExecutionMessage(message);
        scrollToPath(treePath, select, focus);
        return treePath;
    }

    public TreePath addCompilerMessage(CompilerMessage message, boolean select) {
        TreePath treePath = getModel().addCompilerMessage(message);
        scrollToPath(treePath, select, false);
        return treePath;
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage message, boolean select) {
        TreePath treePath = getModel().addExplainPlanMessage(message);
        scrollToPath(treePath, select, false);
        return treePath;
    }

    public void selectCompilerMessage(CompilerMessage message) {
        TreePath treePath = getModel().getTreePath(message);
        scrollToPath(treePath, true, false);
    }

    public void selectExecutionMessage(StatementExecutionMessage message, boolean focus) {
        TreePath treePath = getModel().getTreePath(message);
        scrollToPath(treePath, true, focus);
    }

    private void scrollToPath(TreePath treePath, boolean select, boolean focus) {
        if (treePath != null) {
            SimpleLaterInvocator.invokeNonModal(() -> {
                scrollPathToVisible(treePath);
                TreeSelectionModel selectionModel = getSelectionModel();
                if (select) {
                    try {
                        ignoreSelectionEvent = true;
                        selectionModel.setSelectionPath(treePath);
                    } finally {
                        ignoreSelectionEvent = false;
                    }
                } else {
                    selectionModel.clearSelection();
                }
                if (focus) {
                    requestFocus();
                } else {
                    navigateToCode(treePath.getLastPathComponent(), NavigationInstruction.FOCUS);
                }
            });
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

    private void navigateToCode(Object object, NavigationInstruction instruction) {
        if (object instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) object;
            StatementExecutionMessage executionMessage = execMessageNode.getExecutionMessage();
            if (!executionMessage.isOrphan()) {
                StatementExecutionResult executionResult = executionMessage.getExecutionResult();
                StatementExecutionProcessor executionProcessor = executionResult.getExecutionProcessor();
                EditorProviderId editorProviderId = executionProcessor.getEditorProviderId();
                VirtualFile virtualFile = executionProcessor.getVirtualFile();
                FileEditor fileEditor = executionProcessor.getFileEditor();
                fileEditor = EditorUtil.selectEditor(project, fileEditor, virtualFile, editorProviderId, instruction);
                if (fileEditor != null) {
                    ExecutablePsiElement cachedExecutable = executionProcessor.getCachedExecutable();
                    if (cachedExecutable != null) {
                        cachedExecutable.navigateInEditor(fileEditor, instruction);
                    }
                }
            }
        }
        else if (object instanceof CompilerMessageNode) {
            CompilerMessageNode compilerMessageNode = (CompilerMessageNode) object;
            CompilerMessage compilerMessage = compilerMessageNode.getCompilerMessage();

            CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
            if (compilerAction.isSave() || compilerAction.isCompile() || compilerAction.isBulkCompile()) {
                DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
                if (databaseFile != null) {
                    navigateInObjectEditor(compilerMessage, instruction);
                }
            } else if (compilerAction.isDDL()) {
                VirtualFile virtualFile = compilerAction.getVirtualFile();
                if (virtualFile instanceof DBConsoleVirtualFile) {
                    DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                    navigateInConsoleEditor(compilerMessage, consoleVirtualFile, instruction);
                } else if (virtualFile != null) {
                    navigateInScriptEditor(compilerMessage, virtualFile, instruction);
                }
            }
        }
    }

    private void navigateInConsoleEditor(CompilerMessage compilerMessage, DBConsoleVirtualFile virtualFile, NavigationInstruction instruction) {
        FileEditorManager editorManager = getFileEditorManager();
        if (instruction.isOpen()) {
            editorManager.openFile(virtualFile, instruction.isFocus());
        }

        FileEditor consoleFileEditor = compilerMessage.getCompilerResult().getCompilerAction().getFileEditor();
        if (consoleFileEditor == null) {
            FileEditor[] fileEditors = editorManager.getAllEditors(virtualFile);
            for (FileEditor fileEditor : fileEditors) {
                if (fileEditor instanceof SQLConsoleEditor) {
                    consoleFileEditor = fileEditor;
                }
            }
        }
        navigateInFileEditor(consoleFileEditor, compilerMessage, instruction);
    }

    private void navigateInScriptEditor(CompilerMessage compilerMessage, VirtualFile virtualFile, NavigationInstruction instruction) {
        CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
        FileEditor fileEditor = compilerAction.getFileEditor();
        EditorProviderId editorProviderId = compilerAction.getEditorProviderId();
        fileEditor = EditorUtil.selectEditor(project, fileEditor, virtualFile, editorProviderId, instruction);

        navigateInFileEditor(fileEditor, compilerMessage, instruction);
    }

    private void navigateInFileEditor(FileEditor fileEditor, CompilerMessage compilerMessage, NavigationInstruction instruction) {
        CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
        if (fileEditor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null) {
                if (!instruction.isOpen() && instruction.isFocus()) {
                    EditorUtil.focusEditor(editor);
                }

                if (instruction.isScroll()) {
                    int lineShifting = 1;
                    Document document = editor.getDocument();
                    CharSequence documentText = document.getCharsSequence();
                    String objectName = compilerMessage.getObjectName();
                    int objectStartOffset = StringUtil.indexOfIgnoreCase(documentText, objectName, compilerAction.getSourceStartOffset());
                    if (objectStartOffset > -1) {
                        lineShifting = document.getLineNumber(objectStartOffset);
                    }
                    navigateInEditor(editor, compilerMessage, lineShifting);
                }
            }
        }
    }

    private void navigateInObjectEditor(CompilerMessage compilerMessage, NavigationInstruction instruction) {
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        if (databaseFile != null && !databaseFile.isDisposed()) {
            DBContentVirtualFile contentFile = compilerMessage.getContentFile();
            if (contentFile instanceof DBSourceCodeVirtualFile) {
                CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
                FileEditor objectFileEditor = compilerAction.getFileEditor();
                EditorProviderId editorProviderId = compilerAction.getEditorProviderId();
                if (editorProviderId == null) {
                    DBContentType contentType = compilerMessage.getContentType();
                    switch (contentType) {
                        case CODE: editorProviderId = EditorProviderId.CODE; break;
                        case CODE_SPEC: editorProviderId = EditorProviderId.CODE_SPEC;  break;
                        case CODE_BODY: editorProviderId = EditorProviderId.CODE_BODY; break;
                    }
                }
                objectFileEditor = EditorUtil.selectEditor(project, objectFileEditor, databaseFile, editorProviderId, instruction);

                if (objectFileEditor instanceof SourceCodeEditor) {
                    SourceCodeEditor codeEditor = (SourceCodeEditor) objectFileEditor;
                    Editor editor = EditorUtil.getEditor(codeEditor);
                    if (editor != null) {
                        if (instruction.isScroll()) {
                            Document document = editor.getDocument();
                            int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());
                            navigateInEditor(editor, compilerMessage, lineShifting);
                        }
                        VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
                        if (virtualFile != null) {
                            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile);
                            codeEditor.navigateTo(openFileDescriptor);
                        }
                    }
                }
            }
        }

    }

    private static void navigateInEditor(Editor editor, CompilerMessage compilerMessage, int lineShifting) {
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


    private FileEditorManager getFileEditorManager() {
        return FileEditorManager.getInstance(project);
    }

    /*********************************************************
     *                   TreeSelectionListener               *
     *********************************************************/
    private TreeSelectionListener treeSelectionListener = event -> {
        if (event.isAddedPath() && !ignoreSelectionEvent) {
            Object object = event.getPath().getLastPathComponent();
            navigateToCode(object, NavigationInstruction.OPEN_SCROLL);
            //grabFocus();
        }
    };


    /*********************************************************
     *                        MouseListener                  *
     *********************************************************/
    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath != null) {
                    Object value = selectionPath.getLastPathComponent();
                    navigateToCode(value, NavigationInstruction.OPEN_FOCUS_SCROLL);
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
                    navigateToCode(value, NavigationInstruction.OPEN_FOCUS_SCROLL);
                }
            }
        }
    };

    @Override
    public void dispose() {
        super.dispose();
        project = null;
    }
}
