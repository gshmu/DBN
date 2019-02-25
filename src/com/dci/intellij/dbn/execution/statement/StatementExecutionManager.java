package com.dci.intellij.dbn.execution.statement;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
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
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.language.common.psi.RootPsiElement;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentTransactionListener;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dci.intellij.dbn.execution.ExecutionStatus.*;

@State(
    name = StatementExecutionManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class StatementExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.StatementExecutionManager";

    private static final String[] OPTIONS_MULTIPLE_STATEMENT_EXEC = new String[]{"Execute All", "Execute All from Caret", "Cancel"};

    private final Map<FileEditor, List<StatementExecutionProcessor>> fileExecutionProcessors = new HashMap<>();

    private final StatementExecutionVariablesCache variablesCache;

    private static final AtomicInteger RESULT_SEQUENCE = new AtomicInteger(0);

    private StatementExecutionManager(Project project) {
        super(project);
        variablesCache = new StatementExecutionVariablesCache(project);
        EventUtil.subscribe(project, this, PsiDocumentTransactionListener.TOPIC, psiDocumentTransactionListener);
        EventUtil.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    public StatementExecutionQueue getExecutionQueue(ConnectionId connectionId, SessionId sessionId) {
        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        ConnectionHandler connectionHandler = connectionManager.getConnectionHandler(connectionId);
        connectionHandler = Failsafe.get(connectionHandler);
        return connectionHandler.getExecutionQueue(sessionId);
    }

    public static StatementExecutionManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, StatementExecutionManager.class);
    }

    public void cacheVariable(VirtualFile virtualFile, StatementExecutionVariable variable) {
        variablesCache.cacheVariable(virtualFile, variable);
    }

    public StatementExecutionVariablesCache getVariablesCache() {
        return variablesCache;
    }

    private PsiDocumentTransactionListener psiDocumentTransactionListener = new PsiDocumentTransactionListener() {
        @Override
        public void transactionStarted(@NotNull Document document, @NotNull PsiFile file) {}

        @Override
        public void transactionCompleted(@NotNull Document document, @NotNull PsiFile file) {
            Project project = file.getProject();
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile.isInLocalFileSystem()) {
                List<FileEditor> scriptFileEditors = EditorUtil.getScriptFileEditors(project, virtualFile);
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
        }
    };

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {

        }
    };

    private void refreshEditorExecutionProcessors(FileEditor textEditor) {
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
    private List<StatementExecutionProcessor> getExecutionProcessors(FileEditor textEditor) {
        return fileExecutionProcessors.computeIfAbsent(textEditor, k -> CollectionUtil.createConcurrentList());
    }

    private void bindExecutionProcessors(FileEditor fileEditor, MatchType matchType) {
        Editor editor = EditorUtil.getEditor(fileEditor);
        PsiFile psiFile = DocumentUtil.getFile(editor);
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
    public void debugExecute(@NotNull StatementExecutionProcessor executionProcessor, @NotNull DBNConnection connection) throws SQLException {
        try {
            executionProcessor.execute(connection, true);
        } finally {
            DBLanguagePsiFile file = executionProcessor.getPsiFile();
            DocumentUtil.refreshEditorAnnotations(file);
        }
    }

    public void executeStatement(@NotNull StatementExecutionProcessor executionProcessor) {
        executeStatements(executionProcessor.asList(), executionProcessor.getVirtualFile());
    }

    private void executeStatements(List<StatementExecutionProcessor> executionProcessors, VirtualFile virtualFile) {
        if (executionProcessors.size() > 0) {
            Project project = getProject();
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);

            DBLanguagePsiFile file =  executionProcessors.get(0).getPsiFile();
            connectionMappingManager.selectConnectionAndSchema(file,
                    () -> ConnectionAction.invoke(
                            "the statement execution", false,
                            () -> connectionMappingManager.getConnectionHandler(virtualFile),
                            (action) -> promptExecutionDialogs(executionProcessors, DBDebuggerType.NONE,
                                    () -> {
                                        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
                                            ExecutionContext context = executionProcessor.getExecutionContext();
                                            StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
                                            SessionId sessionId = executionInput.getTargetSessionId();
                                            ConnectionId connectionId = executionInput.getConnectionHandlerId();
                                            if (context.isNot(EXECUTING) && context.isNot(QUEUED)) {
                                                if (sessionId == SessionId.POOL) {
                                                    Progress.background(project, "Executing statement", true,
                                                            (progress) -> process(executionProcessor));
                                                } else {
                                                    StatementExecutionQueue executionQueue = getExecutionQueue(connectionId, sessionId);
                                                    if (!executionQueue.contains(executionProcessor)) {
                                                        executionQueue.queue(executionProcessor);
                                                    }
                                                }
                                            }
                                        }
                                    })));
        }
    }

    public void process(StatementExecutionProcessor executionProcessor) {
        try {
            DBNConnection connection = null;
            try {
                StatementExecutionInput executionInput = executionProcessor.getExecutionInput();
                SchemaId schema = executionInput.getTargetSchemaId();
                ConnectionHandler connectionHandler = Failsafe.get(executionProcessor.getConnectionHandler());
                connection = connectionHandler.getConnection(executionInput.getTargetSessionId(), schema);
            } catch (SQLException e) {
                sendErrorNotification("Error executing " + executionProcessor.getStatementName() + ". Failed to ensure connectivity.", e.getMessage());
            }

            if (connection != null) {
                executionProcessor.execute(connection, false);
            }
        } catch (SQLException e) {
            sendErrorNotification("Error executing " + executionProcessor.getStatementName(), e.getMessage());
        } finally {
            DocumentUtil.refreshEditorAnnotations(executionProcessor.getPsiFile());
        }
    }

    public void executeStatementAtCursor(FileEditor fileEditor) {
        Editor editor = EditorUtil.getEditor(fileEditor);
        if (editor != null) {
            StatementExecutionProcessor executionProcessor = getExecutionProcessorAtCursor(fileEditor);
            if (executionProcessor != null) {
                executeStatement(executionProcessor);
            } else {
                MessageUtil.showQuestionDialog(
                        getProject(),
                        "Multiple statement execution",
                        "No statement found under the caret. \nExecute all statements in the file or just the ones after the cursor?",
                        OPTIONS_MULTIPLE_STATEMENT_EXEC, 0, MessageCallback.create(null, option -> {
                            if (option == 0 || option == 1) {
                                int offset = option == 0 ? 0 : editor.getCaretModel().getOffset();
                                List<StatementExecutionProcessor> executionProcessors = getExecutionProcessorsFromOffset(fileEditor, offset);
                                VirtualFile virtualFile = DocumentUtil.getVirtualFile(editor);
                                executeStatements(executionProcessors, virtualFile);
                            }
                        }));
            }
        }

    }

    public void promptExecutionDialog(@NotNull StatementExecutionProcessor executionProcessor, DBDebuggerType debuggerType, @NotNull Runnable callback) {
        promptExecutionDialogs(executionProcessor.asList(), debuggerType, callback);

    }

    private void promptExecutionDialogs(@NotNull List<StatementExecutionProcessor> processors, DBDebuggerType debuggerType, @NotNull Runnable callback) {
        Dispatch.invokeNonModal(() -> {
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
            Set<ExecVariablePsiElement> bucket = new THashSet<>();
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            if (executablePsiElement != null) {
                executablePsiElement.collectExecVariablePsiElements(bucket);
            }

            StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
            if (bucket.isEmpty()) {
                executionVariables = null;
                executionInput.setExecutionVariables(null);
            } else {
                if (executionVariables == null){
                    executionVariables = new StatementExecutionVariablesBundle(bucket);
                    executionInput.setExecutionVariables(executionVariables);
                }
                executionVariables.initialize(bucket);
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
                            Set<StatementExecutionVariable> variables = executionVariables.getVariables();
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
        Dispatch.invokeNonModal(() -> {
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
    private StatementExecutionProcessor getExecutionProcessorAtCursor(FileEditor fileEditor) {
        Editor editor = EditorUtil.getEditor(fileEditor);
        if (editor != null) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
            String selection = editor.getSelectionModel().getSelectedText();
            if (selection != null) {
                return new StatementExecutionCursorProcessor(fileEditor, file, selection, RESULT_SEQUENCE.incrementAndGet());
            }

            ExecutablePsiElement executablePsiElement = PsiUtil.lookupExecutableAtCaret(editor, true);
            if (executablePsiElement != null) {
                return getExecutionProcessor(fileEditor, executablePsiElement, true);
            }
        }
        return null;
    }

    private List<StatementExecutionProcessor> getExecutionProcessorsFromOffset(FileEditor fileEditor, int offset) {
        List<StatementExecutionProcessor> executionProcessors = new ArrayList<>();
        Editor editor = EditorUtil.getEditor(fileEditor);

        if (editor != null) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) DocumentUtil.getFile(editor);
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
    public StatementExecutionProcessor getExecutionProcessor(FileEditor fileEditor, ExecutablePsiElement executablePsiElement, boolean create) {
        List<StatementExecutionProcessor> executionProcessors = getExecutionProcessors(fileEditor);
        for (StatementExecutionProcessor executionProcessor : executionProcessors) {
            if (executablePsiElement == executionProcessor.getCachedExecutable()) {
                return executionProcessor;
            }
        }

        return create ? createExecutionProcessor(fileEditor, executionProcessors, executablePsiElement) : null;
    }

    private StatementExecutionProcessor createExecutionProcessor(FileEditor fileEditor, List<StatementExecutionProcessor> executionProcessors, ExecutablePsiElement executablePsiElement) {
        StatementExecutionBasicProcessor executionProcessor =
                executablePsiElement.isQuery() ?
                        new StatementExecutionCursorProcessor(fileEditor, executablePsiElement, RESULT_SEQUENCE.incrementAndGet()) :
                        new StatementExecutionBasicProcessor(fileEditor, executablePsiElement, RESULT_SEQUENCE.incrementAndGet());
        executionProcessors.add(executionProcessor);
        executablePsiElement.setExecutionProcessor(executionProcessor);
        return executionProcessor;
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @Override
    @NotNull
    @NonNls
    public String getComponentName() {
        return COMPONENT_NAME;
    }


    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        variablesCache.writeState(element);
        return element;
    }

    @Override
    public void loadState(@NotNull Element element) {
        variablesCache.readState(element);
    }
}
