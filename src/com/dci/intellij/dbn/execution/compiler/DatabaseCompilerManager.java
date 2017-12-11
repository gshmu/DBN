package com.dci.intellij.dbn.execution.compiler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.RunnableTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerAdapter;
import com.dci.intellij.dbn.editor.code.SourceCodeManagerListener;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.compiler.ui.CompilerTypeSelectionDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class DatabaseCompilerManager extends AbstractProjectComponent {
    private DatabaseCompilerManager(Project project) {
        super(project);
        EventUtil.subscribe(project, project, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
    }

    public static DatabaseCompilerManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseCompilerManager.class);
    }

    private SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            Project project = getProject();
            DBSchemaObject object = sourceCodeFile.getObject();
            DBContentType contentType = sourceCodeFile.getContentType();

            if (DatabaseFeature.OBJECT_INVALIDATION.isSupported(object)) {
                boolean isCompilable = object.is(DBObjectProperty.COMPILABLE);

                if (isCompilable) {
                    CompileType compileType = getCompileType(object, contentType);

                    CompilerAction compilerAction = new CompilerAction(CompilerActionSource.SAVE, contentType, sourceCodeFile, fileEditor);
                    if (compileType == CompileType.DEBUG) {
                        compileObject(object, compileType, compilerAction);
                    }
                    ConnectionHandler connectionHandler = object.getConnectionHandler();
                    EventUtil.notify(project, CompileManagerListener.TOPIC).compileFinished(connectionHandler, object);

                    createCompilerResult(object, compilerAction);
                }
            }
        }
    };

    public void createCompilerResult(DBSchemaObject object, CompilerAction compilerAction) {
        Project project = object.getProject();
        CompilerResult compilerResult = new CompilerResult(compilerAction, object);
        ExecutionManager.getInstance(project).addExecutionResult(compilerResult);
    }

    public void createErrorCompilerResult(CompilerAction compilerAction, DBSchemaObject object, DBContentType contentType, Exception e) {
        Project project = object.getProject();
        CompilerResult compilerResult = new CompilerResult(compilerAction, object, contentType,"Could not perform compile operation. \nCause: " + e.getMessage());
        ExecutionManager.getInstance(project).addExecutionResult(compilerResult);
    }

    public CompileType getCompileType(@Nullable DBSchemaObject object, DBContentType contentType) {
        OperationSettings operationSettings = OperationSettings.getInstance(getProject());
        CompileType compileType = operationSettings.getCompilerSettings().getCompileType();
        switch (compileType) {
            case KEEP: return object != null && object.getStatus().is(contentType, DBObjectStatus.DEBUG) ? CompileType.DEBUG : CompileType.NORMAL;
            case DEBUG: return CompileType.DEBUG;
        }
        return CompileType.NORMAL;
    }

    public void compileObject(final DBSchemaObject object, CompileType compileType, final CompilerAction compilerAction) {
        assert compileType != CompileType.KEEP;
        Project project = object.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        boolean allowed = debuggerManager.checkForbiddenOperation(object.getConnectionHandler());
        if (allowed) {
            doCompileObject(object, compileType, compilerAction);
            DBContentType contentType = compilerAction.getContentType();
            updateFilesContentState(object, contentType);
        }
    }

    void updateFilesContentState(final DBSchemaObject object, final DBContentType contentType) {
        new BackgroundTask(getProject(), "Refreshing local content state", true) {
            @Override
            protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                DBEditableObjectVirtualFile databaseFile = object.getCachedVirtualFile();
                if (databaseFile != null && databaseFile.isContentLoaded()) {
                    if (contentType.isBundle()) {
                        for (DBContentType subContentType : contentType.getSubContentTypes()) {
                            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(subContentType);
                            if (sourceCodeFile != null) {
                                sourceCodeFile.refreshContentState();
                            }
                        }
                    } else {
                        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                        if (sourceCodeFile != null) {
                            sourceCodeFile.refreshContentState();
                        }
                    }
                }
            }
        }.start();
    }

    public void compileInBackground(final DBSchemaObject object, final CompileType compileType, final CompilerAction compilerAction) {
        new ConnectionAction("compiling the object", object) {
            @Override
            protected boolean canExecute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
                return debuggerManager.checkForbiddenOperation(connectionHandler);
            }

            @Override
            protected void execute() {
                final Project project = getProject();
                BackgroundTask<CompileType> compileTask = new BackgroundTask<CompileType>(project, "Compiling " + object.getObjectType().getName(), true) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) {
                        CompileType compileType = getData();
                        doCompileObject(object, compileType, compilerAction);
                        ConnectionHandler connectionHandler = object.getConnectionHandler();
                        EventUtil.notify(project, CompileManagerListener.TOPIC).compileFinished(connectionHandler, object);

                        DBContentType contentType = compilerAction.getContentType();
                        updateFilesContentState(object, contentType);
                    }
                };

                promptCompileTypeSelection(compileType, object, compileTask);
            }
        }.start();
    }

    private void doCompileObject(DBSchemaObject object, CompileType compileType, CompilerAction compilerAction) {
        DBContentType contentType = compilerAction.getContentType();
        DBObjectStatusHolder objectStatus = object.getStatus();
        if (objectStatus.isNot(contentType, DBObjectStatus.COMPILING)) {
            objectStatus.set(contentType, DBObjectStatus.COMPILING, true);
            DBNConnection connection = null;
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            boolean verbose = compilerAction.getSource() != CompilerActionSource.BULK_COMPILE;
            try {
                connection = connectionHandler.getPoolConnection(true);
                DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();

                boolean isDebug = compileType == CompileType.DEBUG;

                if (compileType == CompileType.KEEP) {
                    isDebug = objectStatus.is(DBObjectStatus.DEBUG);
                }

                if (contentType == DBContentType.CODE_SPEC || contentType == DBContentType.CODE) {
                    metadataInterface.compileObject(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getTypeName().toUpperCase(),
                            isDebug,
                            connection);
                }
                else if (contentType == DBContentType.CODE_BODY){
                    metadataInterface.compileObjectBody(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getTypeName().toUpperCase(),
                            isDebug,
                            connection);

                } else if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
                    metadataInterface.compileObject(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getTypeName().toUpperCase(),
                            isDebug,
                            connection);
                    metadataInterface.compileObjectBody(
                            object.getSchema().getName(),
                            object.getName(),
                            object.getTypeName().toUpperCase(),
                            isDebug,
                            connection);
                }

                if (verbose) createCompilerResult(object, compilerAction);
            } catch (SQLException e) {
                if (verbose) createErrorCompilerResult(compilerAction, object, contentType, e);
            }  finally{
                connectionHandler.freePoolConnection(connection);
                objectStatus.set(contentType, DBObjectStatus.COMPILING, false);
            }
        }
    }

    public void compileInvalidObjects(final DBSchema schema, final CompileType compileType) {
        new ConnectionAction("compiling the invalid objects", schema) {
            @Override
            protected boolean canExecute() {
                ConnectionHandler connectionHandler = getConnectionHandler();
                DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
                return debuggerManager.checkForbiddenOperation(connectionHandler);
            }

            @Override
            protected void execute() {
                final Project project = getProject();
                final ConnectionHandler connectionHandler = getConnectionHandler();
                promptCompileTypeSelection(compileType, null, new BackgroundTask<CompileType>(project, "Compiling invalid objects", false, true) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        CompileType compileType = getData();
                        doCompileInvalidObjects(schema.getPackages(), "packages", progressIndicator, compileType);
                        doCompileInvalidObjects(schema.getFunctions(), "functions", progressIndicator, compileType);
                        doCompileInvalidObjects(schema.getProcedures(), "procedures", progressIndicator, compileType);
                        doCompileInvalidObjects(schema.getDatasetTriggers(), "dataset triggers", progressIndicator, compileType);
                        doCompileInvalidObjects(schema.getDatabaseTriggers(), "database triggers", progressIndicator, compileType);
                        EventUtil.notify(project, CompileManagerListener.TOPIC).compileFinished(connectionHandler, null);

                        if (!progressIndicator.isCanceled()) {
                            List<CompilerResult> compilerErrors = new ArrayList<CompilerResult>();
                            buildCompilationErrors(schema.getPackages(), compilerErrors);
                            buildCompilationErrors(schema.getFunctions(), compilerErrors);
                            buildCompilationErrors(schema.getProcedures(), compilerErrors);
                            buildCompilationErrors(schema.getDatasetTriggers(), compilerErrors);
                            buildCompilationErrors(schema.getDatabaseTriggers(), compilerErrors);
                            if (compilerErrors.size() > 0) {
                                ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
                                executionManager.addExecutionResults(compilerErrors);
                            }
                        }

                    }
                });
            }
        }.start();
    }

    private void doCompileInvalidObjects(List<? extends DBSchemaObject> objects, String description, ProgressIndicator progressIndicator, CompileType compileType) {
        if (progressIndicator.isCanceled()) return;

        progressIndicator.setText("Compiling invalid " + description + "...");
        int count = objects.size();
        for (int i=0; i< count; i++) {
            if (progressIndicator.isCanceled() || objects.size() == 0 /* may be disposed meanwhile*/) {
                break;
            } else {
                DBSchemaObject object = objects.get(i);
                progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, count));
                DBObjectStatusHolder objectStatus = object.getStatus();
                if (object.getContentType().isBundle()) {
                    for (DBContentType contentType : object.getContentType().getSubContentTypes()) {
                        if (objectStatus.isNot(contentType, DBObjectStatus.VALID)) {
                            CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, contentType);
                            doCompileObject(object, compileType, compilerAction);
                            progressIndicator.setText2("Compiling " + object.getQualifiedNameWithType());
                        }
                    }
                } else {
                    if (objectStatus.isNot(DBObjectStatus.VALID)) {
                        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, object.getContentType());
                        doCompileObject(object, compileType, compilerAction);
                        progressIndicator.setText2("Compiling " + object.getQualifiedNameWithType());
                    }
                }
            }
        }
    }

    private void buildCompilationErrors(List<? extends DBSchemaObject> objects, List<CompilerResult> compilerErrors) {
        for (DBSchemaObject object : objects) {
            if (!object.getStatus().is(DBObjectStatus.VALID)) {
                CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, object.getContentType());
                CompilerResult compilerResult = new CompilerResult(compilerAction, object);
                if (compilerResult.isError()) {
                    compilerErrors.add(compilerResult);
                }
            }
        }
    }

    private void promptCompileTypeSelection(CompileType compileType, @Nullable DBSchemaObject program, RunnableTask<CompileType> callback) {
        if (compileType == CompileType.ASK) {
            CompilerTypeSelectionDialog dialog = new CompilerTypeSelectionDialog(getProject(), program);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                compileType = dialog.getSelection();
                if (dialog.isRememberSelection()) {
                    OperationSettings operationSettings = OperationSettings.getInstance(getProject());
                    operationSettings.getCompilerSettings().setCompileType(compileType);
                }
                callback.setData(compileType);
                callback.start();
            }
        } else {
            callback.setData(compileType);
            callback.start();
        }
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.CompilerManager";
    }
}
