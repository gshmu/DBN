package com.dci.intellij.dbn.execution.statement.processor;

import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Counter;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.editor.BasicTextEditor;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.thread.CancellableDatabaseCall;
import com.dci.intellij.dbn.common.thread.ReadActionRunner;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EditorUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.NavigationInstruction;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
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
import com.dci.intellij.dbn.execution.statement.options.StatementExecutionSettings;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionBasicResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionStatus;
import com.dci.intellij.dbn.execution.statement.variables.StatementExecutionVariablesBundle;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ChameleonPsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.QualifiedIdentifierPsiElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

public class StatementExecutionBasicProcessor extends DisposableBase implements StatementExecutionProcessor {
    protected DBLanguagePsiFile psiFile;
    private WeakReference<FileEditor> fileEditorRef;
    private ExecutablePsiElement cachedExecutable;
    private EditorProviderId editorProviderId;

    private LazyValue<String> resultName = new SimpleLazyValue<String>() {
        @Override
        protected String load() {
            String resultName = null;
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            if (executablePsiElement!= null) {
                resultName = executablePsiElement.createSubjectList();
            }
            if (StringUtil.isEmptyOrSpaces(resultName)) {
                resultName = "Result " + index;
            }
            return resultName;
        }
    };

    protected int index;

    private StatementExecutionInput executionInput;
    private StatementExecutionResult executionResult;

    public StatementExecutionBasicProcessor(FileEditor fileEditor, ExecutablePsiElement psiElement, int index) {
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
        this.psiFile = psiElement.getFile();

        this.cachedExecutable = psiElement;
        this.index = index;
        executionInput = new StatementExecutionInput(psiElement.getText(), psiElement.prepareStatementText(), this);
        initEditorProviderId(fileEditor);
    }

    public StatementExecutionBasicProcessor(FileEditor fileEditor, DBLanguagePsiFile psiFile, String sqlStatement, int index) {
        this.fileEditorRef = new WeakReference<FileEditor>(fileEditor);
        this.psiFile = psiFile;
        this.index = index;
        sqlStatement = sqlStatement.trim();
        executionInput = new StatementExecutionInput(sqlStatement, sqlStatement, this);

        initEditorProviderId(fileEditor);
    }

    private void initEditorProviderId(FileEditor fileEditor) {
        if (fileEditor instanceof BasicTextEditor) {
            BasicTextEditor basicTextEditor = (BasicTextEditor) fileEditor;
            editorProviderId = basicTextEditor.getEditorProviderId();
        }
    }

    public boolean isDirty(){
        if (getConnectionHandler() != executionInput.getConnectionHandler() || // connection changed since execution
            getTargetSchema() != executionInput.getCurrentSchema()) { // current schema changed since execution)
            return true;

        } else {
            ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
            return
                this.cachedExecutable == null ||
                executablePsiElement == null ||
                !this.cachedExecutable.matches(executablePsiElement, BasePsiElement.MatchType.STRONG);
        }
    }

    @Override
    public void bind(ExecutablePsiElement executablePsiElement) {
        this.cachedExecutable = executablePsiElement;
        executablePsiElement.setExecutionProcessor(this);
    }

    @Override
    public void unbind() {
        cachedExecutable = null;
    }

    @Override
    public boolean isBound() {
        return cachedExecutable != null;
    }

    @NotNull
    public DBLanguagePsiFile getPsiFile() {
        return FailsafeUtil.get(psiFile);
    }

    @Override
    public VirtualFile getVirtualFile() {
        return getPsiFile().getVirtualFile();
    }

    @Override
    public FileEditor getFileEditor() {
        FileEditor fileEditor = this.fileEditorRef == null ? null : this.fileEditorRef.get();
        if (fileEditor != null) {
            Editor editor = EditorUtil.getEditor(fileEditor);
            if (editor != null && editor.isDisposed()) {
                this.fileEditorRef = null;
            }
        }
        return fileEditor;
    }

    @Override
    @Nullable
    public EditorProviderId getEditorProviderId() {
        return editorProviderId;
    }

    @Override
    @Nullable
    public ExecutablePsiElement getCachedExecutable() {
        return cachedExecutable;
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

    public StatementExecutionResult getExecutionResult() {
        if (executionResult != null && executionResult.isDisposed()) {
            executionResult = null;
        }
        return executionResult;
    }

    @Override
    public void initExecutionInput(boolean bulkExecution) {
        // overwrite the input if it was leniently bound
        if (cachedExecutable != null) {
            executionInput.setOriginalStatementText(cachedExecutable.getText());
            executionInput.setExecutableStatementText(cachedExecutable.prepareStatementText());
            executionInput.setConnectionHandler(getConnectionHandler());
            executionInput.setCurrentSchema(getTargetSchema());
            executionInput.setBulkExecution(bulkExecution);
        }

    }

    public void execute() throws SQLException {
        execute(null, false);
    }

    public void execute(@Nullable DBNConnection connection, boolean debug) throws SQLException {
        ProgressMonitor.setTaskDescription("Executing " + getStatementName());
        ExecutionContext context = executionInput.getExecutionContext(true);
        resultName.reset();
        executionInput.initExecution();

        ConnectionHandler connectionHandler = getTargetConnection();


        try {
            String statementText = initStatementText();
            SQLException executionException = null;
            if (statementText != null) {
                Counter runningStatements = connectionHandler.getLoadMonitor().getRunningStatements();

                try {
                    runningStatements.increment();
                    initConnection(context, connection);
                    initTimeout(context, debug);
                    initLogging(context, debug);

                    executionResult = executeStatement(context, statementText);

                    // post execution activities
                    if (executionResult != null) {
                        executionResult.calculateExecDuration();
                        consumeLoggerOutput(context);
                        notifyDataDefinitionChanges(context);
                        notifyDataManipulationChanges(context);
                    }
                } catch (SQLException e) {
                    ConnectionUtil.cancelStatement(context.getStatement());
                    executionException = e;
                    executionResult = createErrorExecutionResult(e.getMessage());
                    executionResult.calculateExecDuration();
                } finally {
                    runningStatements.decrement();
                    disableLogging(context);
                }
            }

            if (executionResult != null) {
                Project project = getProject();
                ExecutionManager executionManager = ExecutionManager.getInstance(project);
                executionManager.addExecutionResult(executionResult);
            }

            if (executionException != null && debug) {
                throw executionException;
            }

        } finally {
            postExecute();
        }
    }

    @Override
    public void postExecute() {
        ExecutionContext context = getExecutionInput().getExecutionContext();
        if (!context.isBusy()) {
            DBNConnection connection = context.getConnection();
            if (connection != null && connection.isPoolConnection()) {
                ConnectionUtil.cancelStatement(context.getStatement());
                ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
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

    private void initTimeout(ExecutionContext context, boolean debug) throws SQLException {
        int timeout = debug ?
                executionInput.getDebugExecutionTimeout() :
                executionInput.getExecutionTimeout();
        context.setTimeout(timeout);
    }

    private void initConnection(ExecutionContext context, DBNConnection connection) throws SQLException {
        ConnectionHandler connectionHandler = getTargetConnection();
        if (connection == null) {
            DBSchema schema = getTargetSchema();
            connection = connectionHandler.getMainConnection(schema);
        }
        context.setConnection(connection);
    }

    private void initLogging(ExecutionContext context, boolean debug) {
        boolean logging = false;
        if (!debug && executionInput.getOptions().isEnableLogging() && executionInput.isDatabaseLogProducer()) {
            ConnectionHandler connectionHandler = getTargetConnection();
            DBNConnection connection = context.getConnection();

            DatabaseLoggingManager loggingManager = DatabaseLoggingManager.getInstance(getProject());
            logging = loggingManager.enableLogger(connectionHandler, connection);
        }
        context.setLogging(logging);

    }

    @Nullable
    private StatementExecutionResult executeStatement(final ExecutionContext context, final String statementText) throws SQLException {
        DBNConnection connection = context.getConnection();
        ConnectionHandler connectionHandler = getTargetConnection();
        final Statement statement = connection.createStatement();
        context.setStatement(statement);

        int timeout = context.getTimeout();
        statement.setQueryTimeout(timeout);
        return new CancellableDatabaseCall<StatementExecutionResult>(connectionHandler, connection, timeout, TimeUnit.SECONDS) {
            @Override
            public StatementExecutionResult execute() throws Exception{
                statement.execute(statementText);
                return createExecutionResult(statement, executionInput);
            }

            @Override
            public void cancel() throws Exception {
                context.setCancelled(true);
                ConnectionUtil.cancelStatement(statement);
            }
        }.start();
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
        final DBNConnection connection = context.getConnection();
        ConnectionHandler connectionHandler = getTargetConnection();
        ExecutablePsiElement psiElement = executionInput.getExecutablePsiElement();
        boolean notifyChanges = false;
        boolean resetChanges = false;

        if (psiElement != null) {
            if (psiElement.isTransactional()) {
                notifyChanges = true;
            }
            else if (psiElement.isPotentiallyTransactional()) {
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
            if (connection.isMainConnection()) {
                VirtualFile virtualFile = getPsiFile().getVirtualFile();
                connectionHandler.notifyDataChanges(virtualFile);
            } else if (connection.isPoolConnection()) {
                StatementExecutionManager executionManager = StatementExecutionManager.getInstance(getProject());
                executionManager.promptPendingTransactionDialog(this);
/*
                MessageUtil.showQuestionDialog(
                        getProject(),
                        "Commit or rollback",
                        "You executed this statement in a pool connection. \nThe transactional status of this connection cannot be left inconsistent. Please choose whether to commit or rollback the changes.",
                        new String[]{"Commit", "Rollback"},
                        0,
                        new MessageCallback() {
                            @Override
                            protected void execute() {
                                int option = getData();
                                if (option == 0) ConnectionUtil.commit(connection); else
                                if (option == 1) ConnectionUtil.rollback(connection);
                            }
                        });
*/
            }
        } else if (resetChanges) {
            if (connection.isMainConnection()) {
                connectionHandler.resetDataChanges();
            }
        }
    }

    private void notifyDataDefinitionChanges(ExecutionContext context) {
        Project project = getProject();
        if (isDataDefinitionStatement()) {
            DBSchemaObject affectedObject = getAffectedObject();
            if (affectedObject != null) {
                DataDefinitionChangeListener listener = EventUtil.notify(project, DataDefinitionChangeListener.TOPIC);
                listener.dataDefinitionChanged(affectedObject);
            } else {
                DBSchema affectedSchema = getAffectedSchema();
                IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
                if (affectedSchema != null && subjectPsiElement != null) {
                    DataDefinitionChangeListener listener = EventUtil.notify(project, DataDefinitionChangeListener.TOPIC);
                    listener.dataDefinitionChanged(affectedSchema, subjectPsiElement.getObjectType());
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

    @Nullable
    public StatementExecutionVariablesBundle getExecutionVariables() {
        return executionInput.getExecutionVariables();
    }

    @NotNull
    protected StatementExecutionResult createExecutionResult(Statement statement, final StatementExecutionInput executionInput) throws SQLException {
        final StatementExecutionBasicResult executionResult = new StatementExecutionBasicResult(this, getResultName(), statement.getUpdateCount());
        boolean isDdlStatement = isDataDefinitionStatement();
        boolean hasCompilerErrors = false;
        final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
        if (isDdlStatement && DatabaseFeature.OBJECT_INVALIDATION.isSupported(connectionHandler)) {
            final BasePsiElement compilablePsiElement = getCompilableBlockPsiElement();
            if (compilablePsiElement != null) {
                hasCompilerErrors = new ReadActionRunner<Boolean>() {
                    @Override
                    protected Boolean run() {
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
                                boolean isCompilable = object.getProperties().is(DBObjectProperty.COMPILABLE);
                                if (isCompilable) {
                                    CompileType compileType = compilerManager.getCompileType(object, contentType);
                                    if (compileType == CompileType.DEBUG) {
                                        compilerManager.compileObject(object, compileType, compilerAction);
                                    }
                                    EventUtil.notify(project, CompileManagerListener.TOPIC).compileFinished(connectionHandler, object);
                                }
                                object.refresh();
                            }

                            executionResult.setCompilerResult(compilerResult);
                            return compilerResult.hasErrors();
                        }
                        return false;
                    }
                }.start();
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

        return executionResult;
    }



    private StatementExecutionResult createErrorExecutionResult(String cause) {
        StatementExecutionResult executionResult = new StatementExecutionBasicResult(this, getResultName(), 0);
        executionResult.updateExecutionMessage(MessageType.ERROR, "Error executing " + getStatementName() + '.', cause);
        executionResult.setExecutionStatus(StatementExecutionStatus.ERROR);
        return executionResult;
    }

    public StatementExecutionSettings getStatementExecutionSettings() {
        return ExecutionEngineSettings.getInstance(getProject()).getStatementExecutionSettings();
    }

    @Nullable
    public ConnectionHandler getConnectionHandler() {
        return getPsiFile().getActiveConnection();
    }

    @NotNull
    public ConnectionHandler getTargetConnection() {
        return FailsafeUtil.get(getConnectionHandler());
    }

    @Nullable
    public DBSchema getTargetSchema() {
        return getPsiFile().getCurrentSchema();
    }

    @NotNull
    public Project getProject() {
        return getPsiFile().getProject();
    }

    @NotNull
    public String getResultName() {
        return resultName.get();
    }

    public String getStatementName() {
        ExecutablePsiElement executablePsiElement = executionInput.getExecutablePsiElement();
        return executablePsiElement == null ? "SQL statement" : executablePsiElement.getElementType().getDescription();
    }

    public int getIndex() {
        return index;
    }

    public boolean canExecute() {
        return !isDisposed();
    }

    public void navigateToResult() {
        StatementExecutionResult executionResult = getExecutionResult();
        if (executionResult != null) {
            ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
            executionManager.selectExecutionResult(executionResult);
        }
    }

    public void navigateToEditor(NavigationInstruction instruction) {
        FileEditor fileEditor = getFileEditor();
        if (cachedExecutable != null) {
            if (fileEditor != null) {
                cachedExecutable.navigateInEditor(fileEditor, instruction);
            } else {
                cachedExecutable.navigate(instruction.isFocus());
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean isDataDefinitionStatement() {
        return cachedExecutable != null && cachedExecutable.is(ElementTypeAttribute.DATA_DEFINITION);
    }

    @Nullable
    private DBSchemaObject getAffectedObject() {
        if (isDataDefinitionStatement()) {
            IdentifierPsiElement subjectPsiElement = getSubjectPsiElement();
            if (subjectPsiElement != null) {
                DBSchema currentSchema = getTargetSchema();
                if (currentSchema != null) {
                    DBObjectListContainer childObjects = currentSchema.getChildObjects();
                    if (childObjects != null) {
                        DBObjectList objectList = childObjects.getObjectList(subjectPsiElement.getObjectType());
                        if (objectList != null) {
                            return (DBSchemaObject) objectList.getObject(subjectPsiElement.getText());
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
        return getTargetSchema();
    }

    @Nullable
    private IdentifierPsiElement getSubjectPsiElement() {
        return cachedExecutable == null ? null : (IdentifierPsiElement) cachedExecutable.findFirstPsiElement(ElementTypeAttribute.SUBJECT);
    }

    private BasePsiElement getCompilableBlockPsiElement() {
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

    public boolean isQuery() {
        return false;
    }

    @Override
    public List<StatementExecutionProcessor> asList() {
        List<StatementExecutionProcessor> list = new ArrayList<StatementExecutionProcessor>();
        list.add(this);
        return list;
    }

    @Override
    public int getExecutableLineNumber() {
        if (cachedExecutable != null) {
            Document document = DocumentUtil.getDocument(cachedExecutable.getFile());
            if (document != null) {
                int textOffset = cachedExecutable.getTextOffset();
                return document.getLineNumber(textOffset);
            }
        }

        return 0;
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    @Override
    public void dispose() {
        super.dispose();
        cachedExecutable = null;
        psiFile = null;
    }
}
