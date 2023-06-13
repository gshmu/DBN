package com.dci.intellij.dbn.execution.statement.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Checks;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.execution.statement.StatementExecutionContext;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Checks.isValid;
import static com.dci.intellij.dbn.execution.ExecutionStatus.EXECUTING;
import static com.dci.intellij.dbn.execution.ExecutionStatus.QUEUED;

public class StatementGutterAction extends AnAction {
    private final PsiFileRef<DBLanguagePsiFile> psiFileRef;
    private final int elementOffset;


    public StatementGutterAction(ExecutablePsiElement executablePsiElement) {
        psiFileRef = PsiFileRef.of(executablePsiElement.getFile());
        elementOffset = executablePsiElement.getTextOffset();
    }


    @Nullable
    private DBLanguagePsiFile getPsiFile() {
        return psiFileRef.get();
    }

    private VirtualFile getVirtualFile() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getVirtualFile();
    }

    @Nullable
    private ExecutablePsiElement getExecutablePsiElement() {
        return Read.call(this, a -> {
            DBLanguagePsiFile psiFile = a.getPsiFile();
            if (isNotValid(psiFile)) return null;

            PsiElement elementAtOffset = psiFile.findElementAt(a.elementOffset);
            if (elementAtOffset != null && !(elementAtOffset instanceof BasePsiElement)) {
                elementAtOffset = elementAtOffset.getParent();
            }
            if (elementAtOffset instanceof ExecutablePsiElement) {
                return (ExecutablePsiElement) elementAtOffset;
            } else if (elementAtOffset instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) elementAtOffset;
                ExecutablePsiElement executablePsiElement = (ExecutablePsiElement)
                        basePsiElement.findEnclosingElement(ExecutablePsiElement.class);

                if (isValid(executablePsiElement)) {
                    return executablePsiElement;
                }
            }
            return null;
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        DataContext dataContext = e.getDataContext();

        if (executionProcessor != null && !executionProcessor.isDirty()) {
            Project project = executionProcessor.getProject();
            StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
            StatementExecutionContext context = executionProcessor.getExecutionContext();
            if (context.is(EXECUTING) || context.is(QUEUED)) {
                executionProcessor.cancelExecution();
            } else {
                StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
                if (executionResult == null || !(executionProcessor instanceof StatementExecutionCursorProcessor) || executionProcessor.isDirty()) {
                    executionManager.executeStatement(executionProcessor, dataContext);
                } else {
                    executionProcessor.navigateToResult();
                }
            }
        } else {
            executionProcessor = getExecutionProcessor(true);
            if (executionProcessor != null) {
                Project project = executionProcessor.getProject();
                StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
                executionManager.executeStatement(executionProcessor, dataContext);
            }
        }
    }


    @NotNull
    public Icon getIcon() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (executionProcessor != null) {
            StatementExecutionContext context = executionProcessor.getExecutionContext();
            if (context.is(EXECUTING)) return Icons.STMT_EXECUTION_STOP;
            if (context.is(QUEUED)) return Icons.STMT_EXECUTION_STOP_QUEUED;

            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult == null)  {
                return Icons.STMT_EXECUTION_RUN;
            } else {
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS){
                    if (executionProcessor instanceof StatementExecutionCursorProcessor) {
                        return executionProcessor.isDirty() ?
                                Icons.STMT_EXEC_RESULTSET_RERUN :
                                Icons.STMT_EXEC_RESULTSET;
                    } else {
                        return Icons.STMT_EXECUTION_INFO_RERUN;
                    }
                }

                if (executionStatus == StatementExecutionStatus.ERROR) return Icons.STMT_EXECUTION_ERROR_RERUN;
                if (executionStatus == StatementExecutionStatus.WARNING) return Icons.STMT_EXECUTION_WARNING_RERUN;
            }
        }


        return Icons.STMT_EXECUTION_RUN;
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessor(boolean create) {
        DBLanguagePsiFile psiFile = getPsiFile();
        ExecutablePsiElement executablePsiElement = getExecutablePsiElement();
        if (psiFile != null && executablePsiElement != null) {
            Project project = psiFile.getProject();
            Document document = Documents.getDocument(psiFile);
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            FileEditor[] selectedEditors = fileEditorManager.getSelectedEditors();
            for (FileEditor fileEditor : selectedEditors) {
                Editor editor = Editors.getEditor(fileEditor);
                if (editor != null) {
                    if (editor.getDocument() == document) {
                        StatementExecutionManager executionManager = StatementExecutionManager.getInstance(project);
                        return executionManager.getExecutionProcessor(fileEditor, executablePsiElement, create);
                    }
                }
            }
        }
        return null;
    }


    @Nullable
    public String getTooltipText() {
        StatementExecutionProcessor executionProcessor = getExecutionProcessor(false);
        if (Checks.isValid(executionProcessor)) {
            StatementExecutionResult executionResult = executionProcessor.getExecutionResult();
            if (executionResult == null) {
                StatementExecutionContext context = executionProcessor.getExecutionContext();
                if (context.is(EXECUTING)) {
                    return "Statement execution is in progress. Cancel?";
                } else  if (context.is(QUEUED)) {
                    return "Statement execution is queued. Cancel?";
                }
            }
            else {
                StatementExecutionStatus executionStatus = executionResult.getExecutionStatus();
                if (executionStatus == StatementExecutionStatus.SUCCESS) {
                    return "Statement executed successfully. Execute again?";
                } else if (executionStatus == StatementExecutionStatus.ERROR) {
                    return "Statement executed with errors. Execute again?";
                } else if (executionStatus == StatementExecutionStatus.WARNING) {
                    return "Statement executed with warnings. Execute again?";
                }
            }
        }
        return null;//"Execute statement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatementGutterAction)) return false;

        StatementGutterAction that = (StatementGutterAction) o;

        if (elementOffset != that.elementOffset) return false;

        VirtualFile thisVirtualFile = this.getVirtualFile();
        VirtualFile thatVirtualFile = that.getVirtualFile();

        return thisVirtualFile != null ? thisVirtualFile.equals(thatVirtualFile) : thatVirtualFile == null;
    }

    @Override
    public int hashCode() {
        VirtualFile virtualFile = getVirtualFile();
        int result = virtualFile != null ? virtualFile.hashCode() : 0;
        result = 31 * result + elementOffset;
        return result;
    }
}
