package com.dci.intellij.dbn.debugger.execution.statement.action;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class DebugStatementEditorAction extends AnAction {
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (project != null && editor != null) {
            VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
            ExecutablePsiElement executablePsiElement = null;
            if (virtualFile instanceof DBConsoleVirtualFile) {
                DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                if (consoleVirtualFile.getType() == DBConsoleType.DEBUG) {
                    PsiFile file = DocumentUtil.getFile(editor);
                    BasePsiElement basePsiElement = PsiUtil.lookupElementAtOffset(file, ElementTypeAttribute.EXECUTABLE, 100);
                    if (basePsiElement instanceof ExecutablePsiElement) {
                        executablePsiElement = (ExecutablePsiElement) basePsiElement;
                    }

                }
            }

            if (executablePsiElement == null) {
                executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
            }

            if (executablePsiElement != null && executablePsiElement.is(ElementTypeAttribute.DEBUGGABLE)) {
                FileEditor fileEditor = EditorUtil.getFileEditor(editor);
                StatementExecutionManager statementExecutionManager = StatementExecutionManager.getInstance(project);
                StatementExecutionProcessor executionProcessor = statementExecutionManager.getExecutionProcessor(fileEditor, executablePsiElement, true);
                if (executionProcessor != null) {
                    DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                    debuggerManager.startStatementDebugger(executionProcessor);
                }
            }
        }
    }

    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(Icons.STMT_EXECUTION_DEBUG);
        presentation.setText("Debug Statement");
        Project project = ActionUtil.getProject(e);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        boolean enabled = false;
        boolean visible = false;
        if (project != null && editor != null) {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);
            VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
            if (virtualFile != null) {
                if (virtualFile instanceof DBConsoleVirtualFile) {
                    DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
                    enabled = consoleVirtualFile.getType() == DBConsoleType.DEBUG;
                }

                ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);
                if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)){
                    visible = true;
                    if (!enabled) {
                        PsiFile psiFile = PsiUtil.getPsiFile(project, editor.getDocument());
                        if (psiFile != null) {
                            ExecutablePsiElement executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
                            if (executablePsiElement != null && executablePsiElement.is(ElementTypeAttribute.DEBUGGABLE)) {
                                enabled = true;
                            }
                        }
                    }
                }
            }
        }
        presentation.setEnabled(enabled);
        presentation.setVisible(visible);

    }
}
