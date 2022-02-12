package com.dci.intellij.dbn.execution.statement.processor;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.thread.CancellableDatabaseCall;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.thread.Read;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.jdbc.DBNStatement;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.compiler.CompileManagerListener;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.CompilerAction;
import com.dci.intellij.dbn.execution.compiler.CompilerActionSource;
import com.dci.intellij.dbn.execution.compiler.CompilerResult;
import com.dci.intellij.dbn.execution.compiler.DatabaseCompilerManager;
import com.dci.intellij.dbn.execution.logging.DatabaseLoggingManager;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.execution.statement.StatementExecutionManager;
import com.dci.intellij.dbn.execution.statement.StatementExecutionQueue;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.PsiElementRef;
import com.dci.intellij.dbn.language.common.PsiFileRef;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import static com.dci.intellij.dbn.common.navigation.NavigationInstruction.*;
import static com.dci.intellij.dbn.execution.ExecutionStatus.*;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.COMPILABLE;

public class StatementExecutionBasicProcessor extends StatefulDisposable.Base implements StatementExecutionProcessor {
    private final ProjectRef project;
    private final WeakRef<FileEditor> fileEditor;
    private PsiFileRef<DBLanguagePsiFile> psiFile;
    private PsiElementRef<ExecutablePsiElement> cachedExecutable;
    private EditorProviderId editorProviderId;
    private transient CancellableDatabaseCall<StatementExecutionResult> databaseCall;

    private StatementExecutionInput executionInput;
    private StatementExecutionResult executionResult;
    protected int index;

    private final String name;
    private final Icon icon;

    private String stickyResultName;

    private final Latent<String> resultName = Latent.basic(() -> {
        if (stickyResultName != null) {
            return stickyResultName;
        }
        String resultName = null;
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        if (executablePsiElement!= null) {
            resultName = executablePsiElement.createSubjectList();
        }
        if (Strings.isEmptyOrSpaces(resultName)) {
            resultName = "Result " + index;
        }
        return resultName;
    });

    public StatementExecutionBasicProcessor(@NotNull Project project, @NotNull FileEditor fileEditor, @NotNull ExecutablePsiElement psiElement, int index) {
        DBLanguagePsiFile psiFile = psiElement.getFile();

        this.project = ProjectRef.of(project);
        this.fileEditor = WeakRef.of(fileEditor);
        this.psiFile = PsiFileRef.of(psiFile);

        this.cachedExecutable = PsiElementRef.from(psiElement);
        this.name = psiFile.getName();
        this.icon = psiFile.getIcon();
        this.index = index;
        executionInput = new StatementExecutionInput(psiElement.getText(), psiElement.prepareStatementText(), this);
        initEditorProviderId(fileEditor);
    }

    StatementExecutionBasicProcessor(@NotNull Project project, @NotNull FileEditor fileEditor, @NotNull DBLanguagePsiFile psiFile, String sqlStatement, int index) {
        this.project = ProjectRef.of(project);
        this.fileEditor = WeakRef.of(fileEditor);
        this.psiFile = PsiFileRef.of(psiFile);
        this.name = psiFile.getName();
        this.icon = psiFile.getIcon();
        this.index = index;
        sqlStatement = sqlStatement.trim();
        executionInput = new StatementExecutionInput(sqlStatement, sqlStatement, this);

        initEditorProviderId(fileEditor);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return icon;
    }

    private void initEditorProviderId(FileEditor fileEditor) {
        if (fileEditor instanceof BasicTextEditor) {
            BasicTextEditor<?> basicTextEditor = (BasicTextEditor<?>) fileEditor;
            editorProviderId = basicTextEditor.getEditorProviderId();
        }
    }

    @Override
    public boolean isDirty(){
        return Read.conditional(() -> {
            if (getPsiFile() == null ||
                    getConnection() != executionInput.getConnection() || // connection changed since execution
                    getTargetSchema() != executionInput.getTargetSchemaId()) { // current schema changed since execution)
                return true;

            } else {
                ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
                ExecutablePsiElement cachedExecutable = getCachedExecutable();
                return
                        executablePsiElement == null ||
                                cachedExecutable == null ||
                                !cachedExecutable.isValid() ||
                                !cachedExecutable.matches(executablePsiElement, BasePsiElement.MatchType.STRONG);
            }
        }, true);
    }

    @Override
    public void bind(ExecutablePsiElement executablePsiElement) {
        cachedExecutable = PsiElementRef.from(executablePsiElement);
        executablePsiElement.setExecutionProcessor(this);
    }

    @Override
    public void unbind() {
        cachedExecutable = null;
    }

    @Override
    public boolean isBound() {
        return getCachedExecutable() != null;
    }

    @Nullable
    @Override
    public DBLanguagePsiFile getPsiFile() {
        DBLanguagePsiFile psiFile = this.psiFile.get();
        if (psiFile == null) {
            ExecutablePsiElement executablePsiElement = WeakRef.get(cachedExecutable);
            if (executablePsiElement != null && executablePsiElement.isValid()) {
                psiFile = executablePsiElement.getFile();
                this.psiFile = PsiFileRef.of(psiFile);
            }
        }
        return psiFile;
    }

    @Nullable
    @Override
    public VirtualFile getVirtualFile() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getVirtualFile();
    }

    @Nullable
    @Override
    public FileEditor getFileEditor() {
        return fileEditor.get();
    }

    @Override
    @Nullable
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Override
    @Nullable
    public ExecutablePsiElement getCachedExecutable() {
        return cachedExecutable == null ? null : cachedExecutable.get();
    }

    public static boolean contains(PsiElement parent, BasePsiElement childElement, BasePsiElement.MatchType matchType) {
        PsiElement child = parent.getFirstChild();
        while (child != null) {
            if (child == childElement) {
                return true;
            }
            if (child instanceof ChameleonPsiElement) {
                if (contains(child, childElement, matchType)) {
                    return true;
                }
            } else if(child instanceof BasePsiElement) {
                BasePsiElement basePsiElement = (BasePsiElement) child;
                if (basePsiElement.matches(childElement, matchType)) {
                    return true;
                }
            }
            child = child.getNextSibling();
        }
        return false;
    }

    @Override
    public String toString() {
        return executionInput.getOriginalStatementText();
    }

    @Override
    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return executionInput.getExecutionContext();
    }

    @Override
    public ExecutionContext initExecutionContext() {
        return executionInput.initExecutionContext();
    }

    @Nullable
    @Override
    public StatementExecutionResult getExecutionResult() {
        if (!Failsafe.check(executionResult)) {
            executionResult = null;
        }
        return executionResult;
    }

    @Override
    public void initExecutionInput(boolean bulkExecution) {
        // overwrite the input if it was leniently bound
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        if (cachedExecutable != null) {
            executionInput.setOriginalStatementText(cachedExecutable.getText());
            executionInput.setExecutableStatementText(cachedExecutable.prepareStatementText());
            executionInput.setConnectionHandler(getConnection());
            executionInput.setTargetSchemaId(getTargetSchema());
            executionInput.setTargetSession(getTargetSession());
            executionInput.setBulkExecution(bulkExecution);
        }

    }

    @Override
    public void execute() throws SQLException {
        execute(null, false);
    }

    @Override
    public void execute(@Nullable DBNConnection connection, boolean debug) throws SQLException {
        ProgressMonitor.setTaskDescription("Executing " + getStatementName());
        try {
            ExecutionContext context = initExecutionContext();
            context.set(EXECUTING, true);

            resultName.reset();
            Documents.refreshEditorAnnotations(getPsiFile());

            String statementText = initStatementText();
            SQLException executionException = null;
            if (statementText != null) {
                try {
                    assertNotCancelled();
                    initConnection(context, connection);
                    initTimeout(context, debug);
                    initLogging(context, debug);

                    executionResult = executeStatement(statementText);

                    // post execution activities
                    if (executionResult != null) {
                        executionResult.calculateExecDuration();
                        consumeLoggerOutput(context);
                        notifyDataDefinitionChanges(context);
                        notifyDataManipulationChanges(context);
                        notifySchemaSelectionChanges(context);
                    }
                } catch (SQLException e) {
                    ResourceUtil.cancel(context.getStatement());
                    if (context.isNot(CANCELLED)) {
                        executionException = e;
                        executionResult = createErrorExecutionResult(e.getMessage());
                        executionResult.calculateExecDuration();
                        consumeLoggerOutput(context);
                    }
                } finally {
                    disableLogging(context);
                }
            }

            assertNotCancelled();
            if (executionResult != null) {
                Project project = getProject();
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.addExecutionResult(executionResult,
                        NavigationInstructions.create(FOCUS, SCROLL, SELECT));
            }

            if (executionException != null && debug) {
                throw executionException;
            }

        } catch (ProcessCanceledException ignore){
        } finally {
            postExecute();
        }
    }

    private void assertNotCancelled() {
        ExecutionContext context = getExecutionContext();
        if (context.is(CANCELLED)) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    @Override
    public void postExecute() {
        ExecutionContext context = getExecutionContext();
        if (context.isNot(PROMPTED)) {
            DBNConnection connection = context.getConnection();
            if (connection != null && connection.isPoolConnection()) {
                ResourceUtil.cancel(context.getStatement());
                ConnectionHandler connectionHandler = Failsafe.nn(getConnection());
                connectionHandler.freePoolConnection(connection);
            }
            context.reset();
        }
    }

    private String initStatementText() {
        ConnectionHandler connectionHandler = getTargetConnection();
        String statementText = executionInput.getExecutableStatementText();
        StatementExecutionVariablesBundle executionVariables = executionInput.getExecutionVariables();
        if (executionVariables != null) {
            statementText = executionVariables.prepareStatementText(connectionHandler, statementText, false);
            executionInput.setExecutableStatementText(statementText);

            if (executionVariables.hasErrors()) {
                executionResult = createErrorExecutionResult("Could not bind all variables.");
                return null; // cancel execution
            }
        }
        return statementText;
    }

    private void initTimeout(ExecutionContext context, boolean debug) {
        int timeout = debug ?
                executionInput.getDebugExecutionTimeout() :
                executionInput.getExecutionTimeout();
        context.setTimeout(timeout);
    }

    private void initConnection(ExecutionContext context, DBNConnection connection) throws SQLException {
        ConnectionHandler connectionHandler = getTargetConnection();
        if (connection == null) {
            SchemaId schema = getTargetSchema();
            connection = connectionHandler.getMainConnection(schema);
        }
        context.setConnection(connection);
    }

    private void initLogging(ExecutionContext context, boolean debug) {
        boolean logging = false;
        if (!debug && executionInput.getOptions().is(ExecutionOption.ENABLE_LOGGING) && executionInput.isDatabaseLogProducer()) {
            ConnectionHandler connectionHandler = getTargetConnection();
            DBNConnection connection = context.getConnection();

            DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(getProject());
            logging = loggingManager.enableLogger(connectionHandler, connection);
        }
        context.setLogging(logging);

    }

    @Nullable
    private StatementExecutionResult executeStatement(String statementText) throws SQLException {
        ExecutionContext context = getExecutionContext();
        assertNotCancelled();

        ConnectionHandler connectionHandler = getTargetConnection();
        DBNConnection connection = context.getConnection();
        DBNStatement statement = connection.createStatement();

        statement.setFetchSize(executionInput.getResultSetFetchBlockSize());
        context.setStatement(statement);

        int timeout = context.getTimeout();
        statement.setQueryTimeout(timeout);
        assertNotCancelled();

        databaseCall = new CancellableDatabaseCall<StatementExecutionResult>(connectionHandler, connection, timeout, TimeUnit.SECONDS) {
            @Override
            public StatementExecutionResult execute() throws Exception {
                try {
                    statement.execute(statementText);
                    return createExecutionResult(statement, executionInput);
                } finally {
                    databaseCall = null;
                }
            }

            @Override
            public void cancel(){
                try {
                    context.set(CANCELLED, true);
                    ResourceUtil.cancel(statement);
                } finally {
                    databaseCall = null;
                }
            }
        };
        return databaseCall.start();
    }

    @Override
    public void cancelExecution() {
        ExecutionContext context = getExecutionContext();

        context.set(CANCELLED, true);
        StatementExecutionManager executionManager = getExecutionManager();
        StatementExecutionInput executionInput = getExecutionInput();
        SessionId sessionId = executionInput.getTargetSessionId();
        ConnectionId connectionId = executionInput.getConnectionHandlerId();
        StatementExecutionQueue executionQueue = executionManager.getExecutionQueue(connectionId, sessionId);
        executionQueue.cancelExecution(this);
        CancellableDatabaseCall<StatementExecutionResult> databaseCall = this.databaseCall;
        if (databaseCall != null) {
            Progress.background(
                    getProject(),
                    "Cancelling statement execution", false,
                    (progress) -> databaseCall.cancelSilently());
        }
    }

    private void consumeLoggerOutput(ExecutionContext context) {
        boolean logging = context.isLogging();
        executionResult.setLoggingActive(logging);
        if (logging) {
            Project project = getProject();
            DBNConnection connection = context.getConnection();
            ConnectionHandler connectionHandler = getTargetConnection();

            DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(project);
            String logOutput = loggingManager.readLoggerOutput(connectionHandler, connection);
            executionResult.setLoggingOutput(logOutput);
        }
    }

    private void notifyDataManipulationChanges(ExecutionContext context) {
        DBNConnection connection = context.getConnection();
        ConnectionHandler connectionHandler = getTargetConnection();
        ExecutablePsiElement psiElement = executionInput.getExecutablePsiElement();
        boolean notifyChanges = false;
        boolean resetChanges = false;

        if (psiElement != null) {
            if (psiElement.isTransactional()) {
                notifyChanges = true;
            }
            else if (psiElement.isTransactionalCandidate()) {
                if (connectionHandler.hasPendingTransactions(connection)) {
                    notifyChanges = true;
                }
            }
            else if (psiElement.isTransactionControl()) {
                resetChanges = true;
            }
        } else{
            if (executionResult.getUpdateCount() > 0) {
                notifyChanges = true;
            } else if (connectionHandler.hasPendingTransactions(connection)) {
                notifyChanges = true;
            }
        }

        if (notifyChanges) {
            if (connection.isPoolConnection()) {
                StatementExecutionManager executionManager = getExecutionManager();
                executionManager.promptPendingTransactionDialog(this);
            } else {
                VirtualFile virtualFile = getVirtualFile();
                if (virtualFile != null) {
                    connection.notifyDataChanges(virtualFile);
                }
            }
        } else if (resetChanges) {
            if (!connection.isPoolConnection()) {
                connection.resetDataChanges();
            }
        }
    }

    private void notifySchemaSelectionChanges(ExecutionContext context) {
        DatabaseSession targetSession = getTargetSession();
        if (targetSession != null && !targetSession.isPool()) {
            ExecutablePsiElement executablePsiElement = getCachedExecutable();
            if (executablePsiElement != null && executablePsiElement.isSchemaChange()) {
                SchemaId schemaId = executablePsiElement.getSchemaChangeTargetId();
                if (schemaId != null) {
                    FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
                    connectionMappingManager.setDatabaseSchema(getVirtualFile(), schemaId);
                }
            }
        }
    }

    @NotNull
    private StatementExecutionManager getExecutionManager() {
        return StatementExecutionManager.getInstance(getProject());
    }

    private void notifyDataDefinitionChanges(ExecutionContext context) {
        Project project = getProject();
        if (isDataDefinitionStatement()) {
            DBSchemaObject affectedObject = getAffectedObject();
            if (affectedObject != null) {
                ProjectEvents.notify(project,
                        DataDefinitionChangeListener.TOPIC,
                        (listener) -> listener.dataDefinitionChanged(affectedObject));
            } else {
                DBSchema affectedSchema = getAffectedSchema();
                IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
                if (affectedSchema != null && subjectPsiElement != null) {
                    DBObjectType objectType = subjectPsiElement.getObjectType();
                    ProjectEvents.notify(project,
                            DataDefinitionChangeListener.TOPIC,
                            (listener) -> listener.dataDefinitionChanged(affectedSchema, objectType));
                }
            }
        }
    }

    private void disableLogging(ExecutionContext context) {
        DBNConnection connection = context.getConnection();
        ConnectionHandler connectionHandler = getTargetConnection();
        if (context.isLogging()) {
            Project project = getProject();
            DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(project);
            loggingManager.disableLogger(connectionHandler, connection);
        }
    }

    @Override
    @Nullable
    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionInput.getExecutionVariables();
    }

    @NotNull
    protected StatementExecutionResult createExecutionResult(DBNStatement statement, final StatementExecutionInput executionInput) throws SQLException {
        StatementExecutionBasicResult executionResult = new StatementExecutionBasicResult(this, getResultName(), statement.getUpdateCount());
        ResourceUtil.close(statement);
        attachDdlExecutionInfo(executionInput, executionResult);
        return executionResult;
    }

    private void attachDdlExecutionInfo(StatementExecutionInput executionInput, final StatementExecutionBasicResult executionResult) {
        boolean isDdlStatement = isDataDefinitionStatement();
        boolean hasCompilerErrors = false;
        ConnectionHandler connectionHandler = executionInput.getConnection();
        if (isDdlStatement && connectionHandler != null && DatabaseFeature.OBJECT_INVALIDATION.isSupported(connectionHandler)) {
            BasePsiElement compilablePsiElement = getCompilableBlockPsiElement();
            if (compilablePsiElement != null) {
                hasCompilerErrors = Read.call(() -> {
                    DBContentType contentType = getCompilableContentType();
                    CompilerAction compilerAction = new CompilerAction(CompilerActionSource.DDL, contentType, getVirtualFile(), getFileEditor());
                    compilerAction.setSourceStartOffset(compilablePsiElement.getTextOffset());

                    DBSchemaObject object = getAffectedObject();
                    CompilerResult compilerResult = null;
                    if (object == null) {
                        DBSchema schema = getAffectedSchema();
                        IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
                        if (schema != null && subjectPsiElement != null) {
                            DBObjectType objectType = subjectPsiElement.getObjectType();
                            String objectName = subjectPsiElement.getUnquotedText().toString().toUpperCase();
                            compilerResult = new CompilerResult(compilerAction, connectionHandler, schema, objectType, objectName);
                        }
                    } else {
                        compilerResult = new CompilerResult(compilerAction, object);
                    }

                    if (compilerResult != null) {
                        if (object != null) {
                            Project project = getProject();
                            DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(project);
                            if (object.is(COMPILABLE)) {
                                CompileType compileType = compilerManager.getCompileType(object, contentType);
                                if (compileType == CompileType.DEBUG) {
                                    compilerManager.compileObject(object, compileType, compilerAction);
                                }
                                ProjectEvents.notify(project,
                                        CompileManagerListener.TOPIC,
                                        (listener) -> listener.compileFinished(connectionHandler, object));
                            }
                            object.refresh();
                        }

                        executionResult.setCompilerResult(compilerResult);
                        return compilerResult.hasErrors();
                    }
                    return false;
                }, false);
            }
        }

        if (hasCompilerErrors) {
            String message = executionInput.getStatementDescription() + " executed with warnings";
            executionResult.updateExecutionMessage(MessageType.WARNING, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.WARNING);

        } else {
            String message = executionInput.getStatementDescription() + " executed successfully";
            int updateCount = executionResult.getUpdateCount();
            if (!isDdlStatement && updateCount > -1) {
                message = message + ": " + updateCount + (updateCount != 1 ? " rows" : " row") + " affected";
            }
            executionResult.updateExecutionMessage(MessageType.INFO, message);
            executionResult.setExecutionStatus(StatementExecutionStatus.SUCCESS);
        }
    }


    private StatementExecutionResult createErrorExecutionResult(String cause) {
        StatementExecutionResult executionResult = new StatementExecutionBasicResult(this, getResultName(), 0);
        executionResult.updateExecutionMessage(MessageType.ERROR, "Error executing " + getStatementName() + '.', cause);
        executionResult.setExecutionStatus(StatementExecutionStatus.ERROR);
        return executionResult;
    }

    @Override
    @Nullable
    public ConnectionHandler getConnection() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getConnection();
    }

    @Override
    @NotNull
    public ConnectionHandler getTargetConnection() {
        return Failsafe.nn(getConnection());
    }

    @Override
    @Nullable
    public SchemaId getTargetSchema() {
        DatabaseSession targetSession = getTargetSession();
        if (targetSession != null && targetSession.isPool()) {
            ExecutablePsiElement cachedExecutable = getCachedExecutable();
            if (cachedExecutable != null) {
                SchemaId schemaId = cachedExecutable.getContextSchema();
                if (schemaId != null) {
                    return schemaId;
                }
            }
        }

        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getSchemaId();
    }

    @Override
    @Nullable
    public DatabaseSession getTargetSession() {
        DBLanguagePsiFile psiFile = getPsiFile();
        return psiFile == null ? null : psiFile.getSession();
    }

    @Override
    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    @NotNull
    public String getResultName() {
        return resultName.get();
    }

    public void setResultName(String resultName, boolean sticky) {
        this.resultName.set(resultName);
        stickyResultName = sticky ? resultName : null;
    }

    @Override
    public String getStatementName() {
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        return executablePsiElement == null ? "SQL statement" : executablePsiElement.getSpecificElementType().getDescription();
    }

    public int getIndex() {
        return index;
    }

    public boolean canExecute() {
        return !isDisposed();
    }

    @Override
    public void navigateToResult() {
        StatementExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
            executionManager.selectExecutionResult(executionResult);
        }
    }

    @Override
    public void navigateToEditor(NavigationInstructions instructions) {
        FileEditor fileEditor = getFileEditor();
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        if (cachedExecutable != null) {
            if (fileEditor != null) {
                cachedExecutable.navigateInEditor(fileEditor, instructions);
            } else {
                cachedExecutable.navigate(instructions.isFocus());
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean isDataDefinitionStatement() {
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        return cachedExecutable != null && cachedExecutable.is(ElementTypeAttribute.DATA_DEFINITION);
    }

    @Nullable
    private DBSchemaObject getAffectedObject() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                SchemaId targetSchema = getTargetSchema();
                ConnectionHandler connectionHandler = getConnection();
                if (targetSchema != null && connectionHandler != null) {
                    DBObject schemaObject = connectionHandler.getSchema(targetSchema);
                    if (schemaObject != null) {
                        DBObjectListContainer childObjects = schemaObject.getChildObjects();
                        if (childObjects != null) {
                            DBObjectList objectList = childObjects.getObjects(subjectPsiElement.getObjectType());
                            if (objectList != null) {
                                return (DBSchemaObject) objectList.getObject(subjectPsiElement.getText());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private DBSchema getAffectedSchema() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                PsiElement parent = subjectPsiElement.getParent();
                if (parent instanceof QualifiedIdentifierPsiElement) {
                    QualifiedIdentifierPsiElement qualifiedIdentifierPsiElement = (QualifiedIdentifierPsiElement) parent;
                    DBObject parentObject = qualifiedIdentifierPsiElement.lookupParentObjectFor(subjectPsiElement.getElementType());
                    if (parentObject instanceof DBSchema) {
                        return (DBSchema) parentObject;
                    }
                }
            }
        }
        ConnectionHandler connectionHandler = getConnection();
        SchemaId targetSchema = getTargetSchema();
        return connectionHandler == null || targetSchema == null ? null : connectionHandler.getSchema(targetSchema);
    }


    @Nullable
    private IdentifierPsiElement getSubjectPsiElement() {
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        return cachedExecutable == null ? null : (IdentifierPsiElement) cachedExecutable.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
    }

    private BasePsiElement getCompilableBlockPsiElement() {
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        return cachedExecutable == null ? null : cachedExecutable.findFirstPsiElement(ElementTypeAttribute.COMPILABLE_BLOCK);
    }

    private DBContentType getCompilableContentType() {
        BasePsiElement compilableBlockPsiElement = getCompilableBlockPsiElement();
        if (compilableBlockPsiElement != null) {
            //if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DEFINITION)) return DBContentType.CODE;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_SPECIFICATION)) return DBContentType.CODE_SPEC;
            if (compilableBlockPsiElement.is(ElementTypeAttribute.OBJECT_DECLARATION)) return DBContentType.CODE_BODY;
        }
        return DBContentType.CODE;
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public int getExecutableLineNumber() {
        ExecutablePsiElement cachedExecutable = getCachedExecutable();
        if (cachedExecutable != null) {
            Document document = Documents.getDocument(cachedExecutable.getFile());
            if (document != null) {
                int textOffset = cachedExecutable.getTextOffset();
                return document.getLineNumber(textOffset);
            }
        }

        return 0;
    }


    @Override
    protected void disposeInner() {
        nullify();
    }
}
