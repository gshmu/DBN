package com.dci.intellij.dbn.execution.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.execution.statement.variables.ui.StatementExecutionVariablesDialog;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement.MatchType;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class StatementExecutionManager extends AbstractProjectComponent {
    public static final String[] OPTIONS_MULTIPLE_STATEMENT_EXEC = new String[]{"Execute All", "Execute All from Caret", "Cancel"};
    private final Map<PsiFile, List<StatementExecutionProcessor>> fileExecutionProcessors = new THashMap<PsiFile, List<StatementExecutionProcessor>>();

    private static int sequence;
    public int getNextSequence() {
        sequence++;
        return sequence;
    }

    private StatementExecutionManager(Project project) {
        super(project);
        EventManager.subscribe(project, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
    }

    public static StatementExecutionManager getInstance(Project project) {
        return project.getComponent(StatementExecutionManager.class);
    }

    private PsiDocumentTransactionListener psiDocumentTransactionListener = new PsiDocumentTransactionListener() {
        @Override
        public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {}

        @Override
        public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
            Collection<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(file);
            if (!executionProcessors.isEmpty()) {
                for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                    executionProcessor.unbind();
                }

                bindExecutionProcessors(file, MatchType.STRONG);
                bindExecutionProcessors(file, MatchType.CACHED);
                bindExecutionProcessors(file, MatchType.SOFT);

                Iterator<StatementExecutionProcessor> cleanupIterator = executionProcessors.iterator();
                while (cleanupIterator.hasNext()) {
                    if (cleanupIterator.next().getCachedExecutable() == null) {
                        cleanupIterator.remove();
                    }
                }
            }
        }
    };

    @NotNull
    private List<StatementExecutionProcessor> getExecutionProcessors(PsiFile psiFile) {
        List<StatementExecutionProcessor> executionProcessors = fileExecutionProcessors.get(psiFile);
        if (executionProcessors == null) {
            executionProcessors = new ArrayList<StatementExecutionProcessor>();
            fileExecutionProcessors.put(psiFile, executionProcessors);
        }
        return executionProcessors;
    }

    @Override
    public void disposeComponent() {
        EventManager.unsubscribe(psiDocumentTransactionListener);
        super.disposeComponent();
    }

    private void bindExecutionProcessors(PsiFile file, MatchType matchType) {
        PsiElement child = file.getFirstChild();
        while (child != null) {
            if (child instanceof RootPsiElement) {
                RootPsiElement root = (RootPsiElement) child;
                for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                    if (matchType == MatchType.CACHED) {
                        StatementExecutionProcessor executionProcessor = executable.getExecutionProcessor();
                        if (executionProcessor != null && !executionProcessor.isBound()) {
                            executionProcessor.bind(executable);
                        }
                    } else {
                        StatementExecutionProcessor executionProcessor = findExecutionProcessor(executable, matchType);
                        if (executionProcessor != null) {
                            executionProcessor.bind(executable);
                        }
                    }
                }
            }
            child = child.getNextSibling();
        }
    }

    private StatementExecutionProcessor findExecutionProcessor(ExecutablePsiElement executablePsiElement, MatchType matchType) {
        DBLanguagePsiFile psiFile = executablePsiElement.getFile();
        Collection<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(psiFile);

        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (!executionProcessor.isBound()) {
                ExecutablePsiElement execPsiElement = executionProcessor.getExecutionInput().getExecutablePsiElement();
                if (execPsiElement != null && execPsiElement.matches(executablePsiElement, matchType)) {
                    return executionProcessor;
                }
            }
        }
        return null;
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/
    public void executeStatement(final StatementExecutionProcessor executionProcessor) {
        SimpleTask executionTask = new SimpleTask() {
            @Override
            public void execute() {
                executionProcessor.initExecutionInput();
                promptVariablesDialog(executionProcessor,
                        new BackgroundTask(getProject(), "Executing " + executionProcessor.getStatementName(), false, true) {
                            public void execute(@NotNull ProgressIndicator progressIndicator) {
                                initProgressIndicator(progressIndicator, true);
                                executionProcessor.execute(progressIndicator);
                            }
                        });
            }
        };

        selectConnectionAndSchema(executionProcessor.getPsiFile(), executionTask);
    }

    public void executeStatements(final List<StatementExecutionProcessor> executionProcessors) {
        if (executionProcessors.size() > 0) {
            DBLanguagePsiFile file =  executionProcessors.get(0).getPsiFile();

            SimpleTask executionTask = new SimpleTask() {
                @Override
                public void execute() {
                    new BackgroundTask(getProject(), "Executing statements", false, true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            boolean showIndeterminateProgress = executionProcessors.size() < 5;
                            initProgressIndicator(progressIndicator, showIndeterminateProgress);

                            for (int i = 0; i < executionProcessors.size(); i++) {
                                if (!progressIndicator.isCanceled()) {
                                    if (!progressIndicator.isIndeterminate()) {
                                        progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, executionProcessors.size()));
                                    }

                                    final StatementExecutionProcessor executionProcessor = executionProcessors.get(i);
                                    executionProcessor.initExecutionInput();
                                    promptVariablesDialog(executionProcessor, new BackgroundTask(getProject(), "Executing " + executionProcessor.getStatementName(), false, true) {
                                        @Override
                                        protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                                            executionProcessor.execute(progressIndicator);
                                        }
                                    });

                                }
                            }
                        }
                    }.start();
                }
            };

            selectConnectionAndSchema(file, executionTask);
        }
    }

    public void executeStatementAtCursor(final Editor editor) {
            StatementExecutionProcessor executionProcessor = getExecutionProcessorAtCursor(editor);
            if (executionProcessor != null) {
                executeStatement(executionProcessor);
            } else {
                MessageUtil.showQuestionDialog(
                        "Multiple Statement Execution", "No statement found under the caret. \nExecute all statements in the file or just the ones after the cursor?",
                        OPTIONS_MULTIPLE_STATEMENT_EXEC, 0, new SimpleTask() {
                            @Override
                            public void execute() {
                                int option = getOption();
                                if (option == 0 || option == 1) {
                                    int offset = option == 0 ? 0 : editor.getCaretModel().getOffset();
                                    List<StatementExecutionProcessor> executionProcessors = getExecutionProcessorsFromOffset(editor, offset);
                                    executeStatements(executionProcessors);
                                }
                            }
                        });
            }
    }

    public void promptVariablesDialog(@NotNull final StatementExecutionProcessor executionProcessor, @NotNull final RunnableTask callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
                Set<ExecVariablePsiElement> bucket = new THashSet<ExecVariablePsiElement>();
                ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
                if (executablePsiElement != null) {
                    executablePsiElement.collectExecVariablePsiElements(bucket);
                }

                StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
                if (bucket.isEmpty()) {
                    executionVariables = null;
                    executionInput.setExecutionVariables(null);
                } else {
                    if (executionVariables == null)
                        executionVariables = new StatementExecutionVariablesBundle(bucket); else
                        executionVariables.initialize(bucket);
                }

                if (executionVariables != null) {
                    StatementExecutionVariablesDialog dialog = new StatementExecutionVariablesDialog(executionProcessor, executionInput.getExecutableStatementText());
                    dialog.show();
                    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                        callback.start();
                    }
                } else {
                    callback.start();
                }
            }
        }.start();
    }

    private void selectConnectionAndSchema(@NotNull final DBLanguagePsiFile file, @NotNull final SimpleTask callback) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                ConnectionHandler activeConnection = file.getActiveConnection();
                final FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
                if (activeConnection == null || activeConnection.isVirtual()) {
                    String message =
                            activeConnection == null ?
                                    "The file is not linked to any connection.\nTo continue with the statement execution please select a target connection." :
                                    "The connection you selected for this file is a virtual connection, used only to decide the SQL dialect.\n" +
                                            "You can not execute statements against this connection. Please select a proper connection to continue.";


                    MessageUtil.showWarningDialog(message, "No valid Connection", new String[]{"Select Connection", "Cancel"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getOption() == 0) {
                                        connectionMappingManager.promptConnectionSelector(file, true, true,
                                                new SimpleTask() {
                                                    @Override
                                                    public void execute() {
                                                        if (file.getCurrentSchema() == null) {
                                                            connectionMappingManager.promptSchemaSelector(file, callback);
                                                        }
                                                        else {
                                                            callback.start();
                                                        }

                                                    }
                                                });
                                    }
                                }
                            });

                } else if (file.getCurrentSchema() == null) {
                    String message =
                            "You did not select any schema to run the statement against.\n" +
                                    "To continue with the statement execution please select a schema.";
                    MessageUtil.showWarningDialog(message, "No valid Schema", new String[]{"Select Schema", "Cancel"}, 0,
                            new SimpleTask() {
                                @Override
                                public void execute() {
                                    if (getOption() == 0) {
                                        connectionMappingManager.promptSchemaSelector(file, callback);
                                    }
                                }
                            });
                } else {
                    new ConnectionAction(file) {
                        @Override
                        public void execute() {
                            callback.start();
                        }
                    }.start();
                }
            }
        }.start();
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessorAtCursor(Editor editor) {
        DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
        String selection = editor.getSelectionModel().getSelectedText();
        if (selection != null) {
            return new StatementExecutionCursorProcessor(editor, file, selection, getNextSequence());
        }

        ExecutablePsiElement executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
        if (executablePsiElement != null) {
            return getExecutionProcessor(editor, executablePsiElement, true);
        }

        return null;
    }

    public List<StatementExecutionProcessor> getExecutionProcessorsFromOffset(Editor editor, int offset) {
        DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
        List<StatementExecutionProcessor> executionProcessors = new ArrayList<StatementExecutionProcessor>();

        PsiElement child = file.getFirstChild();
        while (child != null) {
            if (child instanceof RootPsiElement) {
                RootPsiElement root = (RootPsiElement) child;

                for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                    if (executable.getTextOffset() > offset) {
                        StatementExecutionProcessor executionProcessor = getExecutionProcessor(editor, executable, true);
                        executionProcessors.add(executionProcessor);
                    }
                }
            }
            child = child.getNextSibling();
        }
        return executionProcessors;
    }

    @Nullable
    public StatementExecutionProcessor getExecutionProcessor(Editor editor, ExecutablePsiElement executablePsiElement, boolean create) {
        DBLanguagePsiFile psiFile = executablePsiElement.getFile();

        List<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(psiFile);
        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (executablePsiElement == executionProcessor.getCachedExecutable()) {
                return executionProcessor;
            }
        }

        return create ? createExecutionProcessor(editor, executionProcessors, executablePsiElement) : null;
    }

    private StatementExecutionProcessor createExecutionProcessor(Editor editor, List<StatementExecutionProcessor> executionProcessors, ExecutablePsiElement executablePsiElement) {
        StatementExecutionBasicProcessor executionProcessor =
                executablePsiElement.isQuery() ?
                        new StatementExecutionCursorProcessor(editor, executablePsiElement, getNextSequence()) :
                        new StatementExecutionBasicProcessor(editor, executablePsiElement, getNextSequence());
        executionProcessors.add(executionProcessor);
        executablePsiElement.setExecutionProcessor(executionProcessor);
        return executionProcessor;
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.StatementExecutionManager";
    }
}
