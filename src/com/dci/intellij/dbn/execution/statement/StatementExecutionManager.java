package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ui.SelectConnectionDialog;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class StatementExecutionManager extends AbstractProjectComponent {
    private static int sequence;
    public int getNextSequence() {
        sequence++;
        return sequence;
    }

    private StatementExecutionManager(Project project) {
        super(project);
    }

    public static StatementExecutionManager getInstance(Project project) {
        return project.getComponent(StatementExecutionManager.class);
    }

    public void fireExecution(final StatementExecutionProcessor executionProcessor) {
        boolean continueExecution = selectConnection(executionProcessor.getFile());
        if (continueExecution) {
            continueExecution = executionProcessor.promptVariablesDialog();
            if (continueExecution) {
                new BackgroundTask(getProject(), "Executing statement", false, true) {
                    public void execute(@NotNull ProgressIndicator progressIndicator) {
                        initProgressIndicator(progressIndicator, true);
                        executionProcessor.execute(progressIndicator);
                    }
                }.start();
            }
        }
    }

    public void fireExecution(final List<StatementExecutionProcessor> executionProcessors) {
        if (executionProcessors.size() > 0) {
            DBLanguageFile file =  executionProcessors.get(0).getFile();
            boolean continueExecution = selectConnection(file);
            if (continueExecution) {
                for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                    continueExecution = executionProcessor.promptVariablesDialog();
                    if (!continueExecution) break;
                }
                if (continueExecution) {
                    new BackgroundTask(getProject(), "Executing statement", false, true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            boolean showIndeterminateProgress = executionProcessors.size() < 5;
                            initProgressIndicator(progressIndicator, showIndeterminateProgress);

                            for (int i = 0; i < executionProcessors.size(); i++) {
                                if (!progressIndicator.isCanceled()) {
                                    if (!progressIndicator.isIndeterminate()) {
                                        progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, executionProcessors.size()));
                                    }

                                    executionProcessors.get(i).execute(progressIndicator);
                                }
                            }
                        }
                    }.start();
                }
            }
        }
    }

    private boolean selectConnection(DBLanguageFile file) {
        ConnectionHandler activeConnection = file.getActiveConnection();
        DBSchema currentSchema = file.getCurrentSchema();
        if (activeConnection == null || currentSchema == null || activeConnection.isVirtual()) {
            String message =
                    activeConnection == null ?
                            "The file is not linked to any connection.\n" +
                            "To continue with the statement execution please select a target connection." :
                    activeConnection.isVirtual() ?
                            "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                            "You can not execute statements against this connection. Please select a proper connection to continue." :
                    currentSchema == null ?
                            "You did not select any schema to run the statement against.\n" +
                            "To continue with the statement execution please select a schema." : null;

            String okOption =
                    activeConnection == null || activeConnection.isVirtual() ? "Select Connection" :
                    currentSchema == null ? "Select Schema" : null;

            String[] options = {okOption, "Cancel"};

            int response = Messages.showDialog(message, Constants.DBN_TITLE_PREFIX + "No valid Connection / Schema", options, 0, Messages.getWarningIcon());

            if (response == 0) {
                SelectConnectionDialog selectConnectionDialog = new SelectConnectionDialog(file);
                selectConnectionDialog.show();
                return selectConnectionDialog.getExitCode() == SelectConnectionDialog.OK_EXIT_CODE;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public void executeSelectedStatement(Editor editor) {
        StatementExecutionProcessor executionProcessor = getExecutionProcessorAtCursor(editor);
        if (executionProcessor != null) {
            fireExecution(executionProcessor);
        } else {
            DBLanguageFile file = (DBLanguageFile) DocumentUtil.getFile(editor);
            List<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(file);
            fireExecution(executionProcessors);
        }
    }

    private StatementExecutionProcessor getExecutionProcessorAtCursor(Editor editor) {
        DBLanguageFile file = (DBLanguageFile) DocumentUtil.getFile(editor);
        String selection = editor.getSelectionModel().getSelectedText();
        if (selection != null) {
            return new StatementExecutionCursorProcessor(file, selection, getNextSequence());
        }

        ExecutablePsiElement executable = PsiUtil.lookupExecutableAtCaret(file);
        if (executable != null) {
            return executable.getExecutionProcessor();
        }

        return null;
    }

    public static List<StatementExecutionProcessor> getExecutionProcessors(DBLanguageFile file) {
        List<StatementExecutionProcessor> statements = new ArrayList<StatementExecutionProcessor>();

        PsiElement child = file.getFirstChild();
        while (child != null) {
            if (child instanceof RootPsiElement) {
                RootPsiElement root = (RootPsiElement) child;

                for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                    StatementExecutionProcessor executionProcessor = executable.getExecutionProcessor();
                    statements.add(executionProcessor);
                }
            }
            child = child.getNextSibling();
        }
        return statements;
    }

    public StatementExecutionBasicProcessor locateExecutionProcessor(ExecutablePsiElement executablePsiElement) {
        synchronized(executionProcessors) {
            for (StatementExecutionBasicProcessor executionProcessor : executionProcessors) {
                if (executionProcessor.getExecutablePsiElement() != executablePsiElement && executionProcessor.matches(executablePsiElement, false)) {
                    executionProcessor.bind(executablePsiElement);
                    return executionProcessor;
                }
            }


            for (StatementExecutionBasicProcessor executionProcessor : executionProcessors) {
                if (executionProcessor.isDirty() && executionProcessor.matches(executablePsiElement, true)) {
                    executionProcessor.bind(executablePsiElement);
                    return executionProcessor;
                }
            }

            return null;
        }
    }

    public StatementExecutionBasicProcessor createExecutionProcessor(ExecutablePsiElement executablePsiElement) {
        synchronized(executionProcessors) {
            StatementExecutionBasicProcessor executionProcessor =
                    executablePsiElement.isQuery() ?
                            new StatementExecutionCursorProcessor(executablePsiElement, getNextSequence()) :
                            new StatementExecutionBasicProcessor(executablePsiElement, getNextSequence());
            executionProcessors.add(executionProcessor);
            cleanup();
            return executionProcessor;
        }
    }


    private void cleanup() {
        synchronized(executionProcessors) {
            Iterator iterator = executionProcessors.iterator();
            while (iterator.hasNext()) {
                StatementExecutionProcessor executionProcessor = (StatementExecutionProcessor) iterator.next();
                if (executionProcessor.isOrphan()) {
                    iterator.remove();
                }
            }
            if (executionProcessors.size() == 0) {
                sequence = 0;
            }
        }
    }

    private final Set<StatementExecutionBasicProcessor> executionProcessors = new HashSet<StatementExecutionBasicProcessor>();

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.StatementExecutionManager";
    }
}
