package com.dci.intellij.dbn.execution.common.message.ui.tree;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.tree.DBNTree;
import com.dci.intellij.dbn.common.ui.util.Mouse;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.common.util.TextAttributes;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributesKeys;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.execution.common.message.ui.tree.node.CompilerMessageNode;
import com.dci.intellij.dbn.execution.common.message.ui.tree.node.StatementExecutionMessageNode;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;

import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;

public class MessagesTree extends DBNTree implements Disposable {
    private boolean ignoreSelectionEvent = false;

    public MessagesTree(@NotNull DBNComponent parent) {
        super(parent, new MessagesTreeModel());
        setCellRenderer(new MessagesTreeCellRenderer());
        addTreeSelectionListener(treeSelectionListener);
        addMouseListener(mouseListener);
        addKeyListener(keyListener);
        setRootVisible(false);
        setShowsRootHandles(true);
        setOpaque(false);
        Color bgColor = TextAttributes.getSimpleTextAttributes(DataGridTextAttributesKeys.PLAIN_DATA).getBgColor();
        setBackground(bgColor == null ? UIUtil.getTableBackground() : bgColor);
    }

    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int i=0; i<getRowCount();i++){
            TreePath treePath = getPathForRow(i);
            if (!isRowSelected(i)) {
                Object lastPathComponent = treePath.getLastPathComponent();
                if (lastPathComponent instanceof MessagesTreeLeafNode) {
                    MessagesTreeLeafNode node = (MessagesTreeLeafNode) lastPathComponent;
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

    public void removeMessages(ConnectionId connectionId) {
        getModel().removeMessages(connectionId);
    }

    public void resetMessagesStatus() {
        getModel().resetMessagesStatus();
    }

    public void reset() {
        MessagesTreeModel oldModel = getModel();
        setModel(new MessagesTreeModel());
        Disposer.dispose(oldModel);
    }

    public TreePath addExecutionMessage(StatementExecutionMessage message, NavigationInstructions instructions) {
        TreePath treePath = getModel().addExecutionMessage(message);
        scrollToPath(treePath, instructions);
        return treePath;
    }

    public TreePath addCompilerMessage(CompilerMessage message, NavigationInstructions instructions) {
        TreePath treePath = getModel().addCompilerMessage(message);
        scrollToPath(treePath, instructions);
        return treePath;
    }

    public TreePath addExplainPlanMessage(ExplainPlanMessage message, NavigationInstructions instructions) {
        TreePath treePath = getModel().addExplainPlanMessage(message);
        scrollToPath(treePath, instructions);
        return treePath;
    }

    public void selectCompilerMessage(CompilerMessage message, NavigationInstructions instructions) {
        TreePath treePath = getModel().getTreePath(message);
        scrollToPath(treePath, instructions);
    }

    public void selectExecutionMessage(StatementExecutionMessage message, NavigationInstructions instructions) {
        TreePath treePath = getModel().getTreePath(message);
        scrollToPath(treePath, instructions);
    }

    private void scrollToPath(TreePath treePath, NavigationInstructions instructions) {
        if (treePath != null) {
            Dispatch.run(() -> {
                if (instructions.isScroll()) {
                    scrollPathToVisible(treePath);
                }

                TreeSelectionModel selectionModel = getSelectionModel();
                if (instructions.isSelect()) {
                    try {
                        ignoreSelectionEvent = true;
                        selectionModel.setSelectionPath(treePath);
                    } finally {
                        ignoreSelectionEvent = false;
                    }
                } else {
                    selectionModel.clearSelection();
                }
                if (instructions.isFocus()) {
                    requestFocus();

                } else if (instructions.isOpen()){
                    navigateToCode(treePath.getLastPathComponent(), NavigationInstructions.create(OPEN, SCROLL, FOCUS, SELECT));
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

    private void navigateToCode(Object object, NavigationInstructions instructions) {
        if (object instanceof StatementExecutionMessageNode) {
            StatementExecutionMessageNode execMessageNode = (StatementExecutionMessageNode) object;
            StatementExecutionMessage executionMessage = execMessageNode.getMessage();
            if (!executionMessage.isOrphan()) {
                StatementExecutionResult executionResult = executionMessage.getExecutionResult();
                StatementExecutionProcessor executionProcessor = executionResult.getExecutionProcessor();
                EditorProviderId editorProviderId = executionProcessor.getEditorProviderId();
                VirtualFile virtualFile = executionProcessor.getVirtualFile();
                if (virtualFile != null) {
                    FileEditor fileEditor = executionProcessor.getFileEditor();
                    fileEditor = Editors.selectEditor(ensureProject(), fileEditor, virtualFile, editorProviderId, instructions);
                    if (fileEditor != null) {
                        ExecutablePsiElement cachedExecutable = executionProcessor.getCachedExecutable();
                        if (cachedExecutable != null) {
                            cachedExecutable.navigateInEditor(fileEditor, instructions);
                        }
                    }
                }
            }
        }
        else if (object instanceof CompilerMessageNode) {
            CompilerMessageNode compilerMessageNode = (CompilerMessageNode) object;
            CompilerMessage compilerMessage = compilerMessageNode.getMessage();

            CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
            if (compilerAction.isSave() || compilerAction.isCompile() || compilerAction.isBulkCompile()) {
                DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
                if (databaseFile != null) {
                    navigateInObjectEditor(compilerMessage, instructions);
                }
            } else if (compilerAction.isDDL()) {
                VirtualFile virtualFile = compilerAction.getVirtualFile();
                if (virtualFile instanceof DBConsoleVirtualFile) {
                    DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                    navigateInConsoleEditor(compilerMessage, consoleVirtualFile, instructions);
                } else if (virtualFile != null) {
                    navigateInScriptEditor(compilerMessage, virtualFile, instructions);
                }
            }
        }
    }

    private void navigateInConsoleEditor(CompilerMessage compilerMessage, DBConsoleVirtualFile virtualFile, NavigationInstructions instructions) {
        FileEditorManager editorManager = getFileEditorManager();
        if (instructions.isOpen()) {
            editorManager.openFile(virtualFile, instructions.isFocus());
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
        navigateInFileEditor(consoleFileEditor, compilerMessage, instructions);
    }

    private void navigateInScriptEditor(CompilerMessage compilerMessage, VirtualFile virtualFile, NavigationInstructions instructions) {
        CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
        FileEditor fileEditor = compilerAction.getFileEditor();
        EditorProviderId editorProviderId = compilerAction.getEditorProviderId();
        fileEditor = Editors.selectEditor(ensureProject(), fileEditor, virtualFile, editorProviderId, instructions);

        navigateInFileEditor(fileEditor, compilerMessage, instructions);
    }

    private void navigateInFileEditor(FileEditor fileEditor, CompilerMessage compilerMessage, NavigationInstructions instructions) {
        CompilerAction compilerAction = compilerMessage.getCompilerResult().getCompilerAction();
        if (fileEditor != null) {
            Editor editor = Editors.getEditor(fileEditor);
            if (editor != null) {
                if (!instructions.isOpen() && instructions.isFocus()) {
                    Editors.focusEditor(editor);
                }

                if (instructions.isScroll()) {
                    int lineShifting = 1;
                    Document document = editor.getDocument();
                    CharSequence documentText = document.getCharsSequence();
                    String objectName = compilerMessage.getObjectName();
                    int objectStartOffset = Strings.indexOfIgnoreCase(documentText, objectName, compilerAction.getSourceStartOffset());
                    if (objectStartOffset > -1) {
                        lineShifting = document.getLineNumber(objectStartOffset);
                    }
                    navigateInEditor(editor, compilerMessage, lineShifting);
                }
            }
        }
    }

    private void navigateInObjectEditor(CompilerMessage compilerMessage, NavigationInstructions instructions) {
        DBEditableObjectVirtualFile databaseFile = compilerMessage.getDatabaseFile();
        if (Failsafe.check(databaseFile)) {
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
                Project project = ensureProject();
                objectFileEditor = Editors.selectEditor(project, objectFileEditor, databaseFile, editorProviderId, instructions);

                if (objectFileEditor instanceof SourceCodeEditor) {
                    SourceCodeEditor codeEditor = (SourceCodeEditor) objectFileEditor;
                    Editor editor = Editors.getEditor(codeEditor);
                    if (editor != null) {
                        if (instructions.isScroll()) {
                            Document document = editor.getDocument();
                            int lineShifting = document.getLineNumber(codeEditor.getHeaderEndOffset());
                            navigateInEditor(editor, compilerMessage, lineShifting);
                        }
                        VirtualFile virtualFile = Documents.getVirtualFile(editor);
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
                    int selectionOffsetInLine = Strings.indexOfIgnoreCase(lineText, identifier, compilerMessage.getPosition());
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
        Project project = ensureProject();
        return FileEditorManager.getInstance(project);
    }

    /*********************************************************
     *                   TreeSelectionListener               *
     *********************************************************/
    private final TreeSelectionListener treeSelectionListener = event -> {
        if (event.isAddedPath() && !ignoreSelectionEvent) {
            Object object = event.getPath().getLastPathComponent();
            navigateToCode(object, NavigationInstructions.create(OPEN, SCROLL));
            //grabFocus();
        }
    };


    /*********************************************************
     *                        MouseListener                  *
     *********************************************************/
    private final MouseListener mouseListener = Mouse.listener().onClick(e -> {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
            TreePath selectionPath = getSelectionPath();
            if (selectionPath != null) {
                Object value = selectionPath.getLastPathComponent();
                navigateToCode(value, NavigationInstructions.create(OPEN, FOCUS, SCROLL));
            }
        }
    });

    /*********************************************************
     *                        KeyListener                    *
     *********************************************************/
    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER ) {
                TreePath selectionPath = getSelectionPath();
                if (selectionPath != null) {
                    Object value = selectionPath.getLastPathComponent();
                    navigateToCode(value, NavigationInstructions.create(OPEN, FOCUS, SCROLL));
                }
            }
        }
    };

}
