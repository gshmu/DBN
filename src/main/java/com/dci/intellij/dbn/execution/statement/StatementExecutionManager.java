package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.action.UserDataKeys;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.consumer.ListCollector;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.*;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionContextManager;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.editor.console.SQLConsoleEditor;
import com.dci.intellij.dbn.editor.ddl.DDLFileEditor;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionBasicProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionCursorProcessor;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.execution.statement.result.ui.PendingTransactionDialog;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariable;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesCache;
import com.dci.intellij.dbn.execution.statement.variables.ui.StatementExecutionInputsDialog;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement.MatchType;
import com.dci.intellij.dbn.language.common.psi.*;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import lombok.Getter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.common.dispose.Checks.isNotValid;
import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;
import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

@State(
    name = StatementExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Getter
public class StatementExecutionManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.StatementExecutionManager";

    private static final String[] OPTIONS_MULTIPLE_STATEMENT_EXEC = new String[]{"Execute All", "Execute All from Caret", "Cancel"};

    private final StatementExecutionVariablesCache variablesCache;

    private static final AtomicInteger RESULT_SEQUENCE = new AtomicInteger(0);

    private StatementExecutionManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
        variablesCache = new StatementExecutionVariablesCache(project);

        ProjectEvents.subscribe(project, this, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener());
    }

    @NotNull
    private PsiDocumentTransactionListener psiDocumentTransactionListener() {
        return new PsiDocumentTransactionListener() {
            @Override
            public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {
            }

            @Override
            public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
                guarded(() -> {
                    Project project = file.getProject();
                    VirtualFile virtualFile = file.getVirtualFile();
                    if (virtualFile.isInLocalFileSystem()) {
                        List<FileEditor> scriptFileEditors = Editors.getScriptFileEditors(project, virtualFile);
                        for (FileEditor scriptFileEditor : scriptFileEditors) {
                            refreshEditorExecutionProcessors(scriptFileEditor);
                        }
                    } else {
                        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        FileEditor[] fileEditors = fileEditorManager.getAllEditors(virtualFile);
                        for (FileEditor fileEditor : fileEditors) {
                            if (fileEditor instanceof DDLFileEditor || fileEditor instanceof SQLConsoleEditor) {
                                refreshEditorExecutionProcessors(fileEditor);
                            }
                        }
                    }
                });
            }
        };
    }


    @Nullable
    public StatementExecutionQueue getExecutionQueue(ConnectionId connectionId, SessionId sessionId) {
        ConnectionHandler connection = nd(ConnectionHandler.get(connectionId, getProject()));
        return connection.isVirtual() ? null : connection.getExecutionQueue(sessionId);
    }

    public static StatementExecutionManager getInstance(@NotNull Project project) {
        return projectService(project, StatementExecutionManager.class);
    }

    public void cacheVariable(VirtualFile virtualFile, StatementExecutionVariable variable) {
        variablesCache.cacheVariable(virtualFile, variable);
    }

    private void refreshEditorExecutionProcessors(@NotNull FileEditor textEditor) {
        Collection<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(textEditor);
        if (!executionProcessors.isEmpty()) {
            for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                executionProcessor.unbind();
            }

            bindExecutionProcessors(textEditor, MatchType.STRONG);
            bindExecutionProcessors(textEditor, MatchType.CACHED);
            bindExecutionProcessors(textEditor, MatchType.SOFT);

            List<StatementExecutionProcessor> removeList = null;
            for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                if (executionProcessor.getCachedExecutable() == null) {
                    if (removeList == null) removeList = new ArrayList<>();
                    removeList.add(executionProcessor);
                }
            }

            if (removeList != null) {
                executionProcessors.removeAll(removeList);
            }
        }
    }

    @NotNull
    private List<StatementExecutionProcessor> getExecutionProcessors(@NotNull FileEditor textEditor) {
        return UserDataUtil.ensure(
                textEditor,
                UserDataKeys.STATEMENT_EXECUTION_PROCESSORS,
                () -> CollectionUtil.createConcurrentList());
    }

    private void bindExecutionProcessors(FileEditor fileEditor, MatchType matchType) {
        Editor editor = Editors.getEditor(fileEditor);
        PsiFile psiFile = Documents.getFile(editor);
        if (psiFile != null) {
            PsiElement child = psiFile.getFirstChild();
            while (child != null) {
                if (child instanceof RootPsiElement) {
                    RootPsiElement root = (RootPsiElement) child;
                    for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                        if (matchType == MatchType.CACHED) {
                            StatementExecutionProcessor executionProcessor = executable.getExecutionProcessor();
                            if (executionProcessor != null && !executionProcessor.isBound() && executionProcessor.isQuery() == executable.isQuery()) {
                                executionProcessor.bind(executable);
                            }
                        } else {
                            StatementExecutionProcessor executionProcessor = findExecutionProcessor(executable, fileEditor, matchType);
                            if (executionProcessor != null) {
                                executionProcessor.bind(executable);
                            }
                        }
                    }
                }
                child = child.getNextSibling();
            }
        }
    }

    private StatementExecutionProcessor findExecutionProcessor(ExecutablePsiElement executablePsiElement, FileEditor fileEditor, MatchType matchType) {
        Collection<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(fileEditor);

        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (executionProcessor.isBound()) continue;

            ExecutablePsiElement execPsiElement = executionProcessor.getExecutionInput().getExecutablePsiElement();
            if (execPsiElement != null && execPsiElement.matches(executablePsiElement, matchType)) {
                return executionProcessor;
            }
        }
        return null;
    }

    /*********************************************************
     *                       Execution                       *
     *********************************************************/
    public void debugExecute(@NotNull StatementExecutionProcessor executionProcessor, @NotNull DBNConnection connection) throws SQLException {
        try {
            executionProcessor.execute(connection, true);
        } finally {
            DBLanguagePsiFile file = executionProcessor.getPsiFile();
            Documents.refreshEditorAnnotations(file);
        }
    }

    public void executeStatement(@NotNull StatementExecutionProcessor executionProcessor, DataContext dataContext) {
        executeStatements(
                executionProcessor.getVirtualFile(),
                Collections.singletonList(executionProcessor),
                dataContext);
    }

    private void executeStatements(@Nullable VirtualFile virtualFile, List<StatementExecutionProcessor> executionProcessors, DataContext dataContext) {
        if (isNotValid(virtualFile) || executionProcessors.isEmpty()) return;

        guarded(() -> {
            FileConnectionContextManager contextManager = FileConnectionContextManager.getInstance(getProject());
            contextManager.selectConnectionAndSchema(
                    virtualFile,
                    dataContext,
                    () -> ConnectionAction.invoke(
                            "the statement execution", false,
                            contextManager.getConnection(virtualFile),
                            action -> promptExecutionDialogs(executionProcessors, DBDebuggerType.NONE,
                                    () -> executeStatements(executionProcessors))));

        });
    }

    private void executeStatements(List<StatementExecutionProcessor> executionProcessors) {
        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            ExecutionContext context = executionProcessor.getExecutionContext();
            StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
            SessionId sessionId = executionInput.getTargetSessionId();
            ConnectionId connectionId = executionInput.getConnectionId();
            if (context.isNot(EXECUTING) && context.isNot(QUEUED)) {
                if (sessionId == SessionId.POOL) {
                    Progress.background(getProject(), executionInput, true,
                            "Executing statement",
                            "Executing " + executionInput.getStatementDescription(),
                            progress -> process(executionProcessor));
                } else {
                    StatementExecutionQueue queue = getExecutionQueue(connectionId, sessionId);
                    if (queue != null && !queue.contains(executionProcessor)) {
                        queue.queue(executionProcessor);
                    }
                }
            }
        }
    }

    public void process(StatementExecutionProcessor executionProcessor) {
        String statementName = executionProcessor.getStatementName();
        try {
            DBNConnection conn = null;
            try {
                StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
                SchemaId schema = executionInput.getTargetSchemaId();
                ConnectionHandler connection = Failsafe.nn(executionProcessor.getConnection());
                conn = connection.getConnection(executionInput.getTargetSessionId(), schema);
            } catch (SQLException e) {
                sendErrorNotification(
                        NotificationGroup.EXECUTION,
                        "Error executing {0}. Failed to ensure connectivity: {1}", statementName, e);

                ExecutionContext context = executionProcessor.getExecutionContext();
                context.reset();
            }

            if (conn != null) {
                executionProcessor.execute(conn, false);
            }
        } catch (ProcessCanceledException ignore) {
        } catch (SQLException e) {
            sendErrorNotification(
                    NotificationGroup.EXECUTION,
                    "Error executing {0}: {1}", statementName, e);
        } finally {
            Documents.refreshEditorAnnotations(executionProcessor.getPsiFile());
        }
    }

    public void executeStatementAtCursor(@NotNull FileEditor fileEditor) {
        Editor editor = Editors.getEditor(fileEditor);
        if (editor != null) {
            DataContext dataContext = Context.getDataContext(editor);
            StatementExecutionProcessor executionProcessor = getExecutionProcessorAtCursor(fileEditor);
            if (executionProcessor != null) {
                executeStatement(executionProcessor, dataContext);
            } else {
                Messages.showQuestionDialog(
                        getProject(),
                        "Multiple statement execution",
                        "No statement found under the caret. \nExecute all statements in the file or just the ones after the cursor?",
                        OPTIONS_MULTIPLE_STATEMENT_EXEC, 0,
                        (option) -> {
                            if (option == 0 || option == 1) {
                                int offset = option == 0 ? 0 : editor.getCaretModel().getOffset();
                                List<StatementExecutionProcessor> executionProcessors = getExecutionProcessorsFromOffset(fileEditor, offset);
                                VirtualFile virtualFile = Documents.getVirtualFile(editor);
                                executeStatements(virtualFile, executionProcessors, dataContext);
                            }
                        });
            }
        }

    }

    public void promptExecutionDialog(
            @NotNull StatementExecutionProcessor executionProcessor,
            @NotNull DBDebuggerType debuggerType,
            @NotNull Runnable callback) {
        promptExecutionDialogs(
                Collections.singletonList(executionProcessor),
                debuggerType,
                callback);

    }

    private void promptExecutionDialogs(
            @NotNull List<StatementExecutionProcessor> processors,
            @NotNull DBDebuggerType debuggerType,
            @NotNull Runnable callback) {

        Dispatch.run(() -> {
            if (promptExecutionDialogs(processors, debuggerType)) {
                callback.run();
            }
        });
    }

    private boolean promptExecutionDialogs(@NotNull List<StatementExecutionProcessor> executionProcessors, DBDebuggerType debuggerType) {
        Map<String, StatementExecutionVariable> variableCache = new HashMap<>();
        boolean reuseVariables = false;
        boolean bulkExecution = executionProcessors.size() > 1;

        StatementExecutionSettings executionSettings = ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();

        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            executionProcessor.initExecutionInput(bulkExecution);
            StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
            ListCollector<ExecVariablePsiElement> psiElements = ListCollector.basic();
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            if (executablePsiElement != null) {
                executablePsiElement.collectExecVariablePsiElements(psiElements);
            }

            StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
            if (psiElements.isEmpty()) {
                executionVariables = null;
                executionInput.setExecutionVariables(null);
            } else {
                List<ExecVariablePsiElement> varPsiElements = psiElements.elements();
                if (executionVariables == null){
                    executionVariables = new StatementExecutionVariablesBundle(varPsiElements);
                    executionInput.setExecutionVariables(executionVariables);
                }
                executionVariables.initialize(varPsiElements);
            }

            if (executionVariables != null) {
                if (reuseVariables) {
                    executionVariables.populate(variableCache, true);
                }

                if (!(reuseVariables && executionVariables.isProvided())) {
                    StatementExecutionInputsDialog dialog = new StatementExecutionInputsDialog(executionProcessor, debuggerType, bulkExecution);
                    dialog.show();
                    if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
                        return false;
                    } else {
                        reuseVariables = dialog.isReuseVariables();
                        if (reuseVariables) {
                            List<StatementExecutionVariable> variables = executionVariables.getVariables();
                            for (StatementExecutionVariable variable : variables) {
                                variableCache.put(variable.getName().toUpperCase(), variable);
                            }
                        } else {
                            variableCache.clear();
                        }
                    }
                }
            } else if (executionSettings.isPromptExecution() || debuggerType.isDebug()) {
                StatementExecutionInputsDialog dialog = new StatementExecutionInputsDialog(executionProcessor, debuggerType, bulkExecution);
                dialog.show();
                if (dialog.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
                    return false;
                }
            }
        }

        return true;
    }

    public void promptPendingTransactionDialog(StatementExecutionProcessor executionProcessor) {
        ExecutionContext context = executionProcessor.getExecutionContext();
        context.set(PROMPTED, true);
        Dispatch.run(() -> {
            try {
                PendingTransactionDialog dialog = new PendingTransactionDialog(executionProcessor);
                dialog.show();
            } finally {
                executionProcessor.postExecute();
                context.set(PROMPTED, false);
            }
        });
    }

    @Nullable
    private StatementExecutionProcessor getExecutionProcessorAtCursor(@NotNull FileEditor fileEditor) {
        Editor editor = Editors.getEditor(fileEditor);
        if (editor != null) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) Documents.getFile(editor);
            String selection = editor.getSelectionModel().getSelectedText();
            if (selection != null && file != null) {
                return new StatementExecutionCursorProcessor(getProject(), fileEditor, file, selection, RESULT_SEQUENCE.incrementAndGet());
            }

            ExecutablePsiElement executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
            if (executablePsiElement != null) {
                return getExecutionProcessor(fileEditor, executablePsiElement, true);
            }
        }
        return null;
    }

    private List<StatementExecutionProcessor> getExecutionProcessorsFromOffset(@NotNull FileEditor fileEditor, int offset) {
        List<StatementExecutionProcessor> executionProcessors = new ArrayList<>();
        Editor editor = Editors.getEditor(fileEditor);

        if (editor != null) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) Documents.getFile(editor);
            if (file != null) {
                PsiElement child = file.getFirstChild();
                while (child != null) {
                    if (child instanceof ChameleonPsiElement) {
                        ChameleonPsiElement chameleonPsiElement = (ChameleonPsiElement) child;
                        for (ExecutablePsiElement executable : chameleonPsiElement.getExecutablePsiElements()) {
                            StatementExecutionProcessor executionProcessor = getExecutionProcessor(fileEditor, executable, true);
                            executionProcessors.add(executionProcessor);
                        }

                    }
                    if (child instanceof RootPsiElement) {
                        RootPsiElement root = (RootPsiElement) child;

                        for (ExecutablePsiElement executable: root.getExecutablePsiElements()) {
                            if (executable.getTextOffset() > offset) {
                                StatementExecutionProcessor executionProcessor = getExecutionProcessor(fileEditor, executable, true);
                                executionProcessors.add(executionProcessor);
                            }
                        }
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return executionProcessors;
    }

    @Nullable
    public StatementExecutionProcessor getExecutionProcessor(@NotNull FileEditor fileEditor, @NotNull ExecutablePsiElement executablePsiElement, boolean create) {
        List<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(fileEditor);
        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (executablePsiElement == executionProcessor.getCachedExecutable()) {
                return executionProcessor;
            }
        }

        return create ? createExecutionProcessor(fileEditor, executionProcessors, executablePsiElement) : null;
    }

    private StatementExecutionProcessor createExecutionProcessor(@NotNull FileEditor fileEditor, List<StatementExecutionProcessor> executionProcessors, @NotNull ExecutablePsiElement executablePsiElement) {
        Project project = getProject();
        int index = RESULT_SEQUENCE.incrementAndGet();
        StatementExecutionBasicProcessor executionProcessor =
                executablePsiElement.isQuery() ?
                        new StatementExecutionCursorProcessor(project, fileEditor, executablePsiElement, index) :
                        new StatementExecutionBasicProcessor(project, fileEditor, executablePsiElement, index);
        executionProcessors.add(executionProcessor);
        executablePsiElement.setExecutionProcessor(executionProcessor);
        return executionProcessor;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        Element element = new Element("state");
        variablesCache.writeState(element);
        return element;
    }

    @Override

    public void loadComponentState(@NotNull Element element) {
        variablesCache.readState(element);
    }
}
