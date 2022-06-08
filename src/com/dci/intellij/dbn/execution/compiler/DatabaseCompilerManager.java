package com.dci.intellij.dbn.execution.compiler;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterface;
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
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public class DatabaseCompilerManager extends AbstractProjectComponent {
    private DatabaseCompilerManager(@NotNull Project project) {
        super(project);

        ProjectEvents.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
    }

    public static DatabaseCompilerManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DatabaseCompilerManager.class);
    }

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeSaved(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
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
                    ConnectionHandler connection = object.getConnection();
                    ProjectEvents.notify(project,
                            CompileManagerListener.TOPIC,
                            (listener) -> listener.compileFinished(connection, object));

                    createCompilerResult(object, compilerAction);
                }
            }
        }
    };

    private void createCompilerResult(DBSchemaObject object, CompilerAction compilerAction) {
        Project project = object.getProject();
        CompilerResult compilerResult = new CompilerResult(compilerAction, object);
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.addCompilerResult(compilerResult);
    }

    private void createErrorCompilerResult(CompilerAction compilerAction, DBSchemaObject object, DBContentType contentType, Exception e) {
        Project project = object.getProject();
        CompilerResult compilerResult = new CompilerResult(compilerAction, object, contentType, "Could not perform compile operation. \nCause: " + e.getMessage());
        ExecutionManager executionManager = ExecutionManager.getInstance(project);
        executionManager.addCompilerResult(compilerResult);
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

    public void compileObject(DBSchemaObject object, CompileType compileType, CompilerAction compilerAction) {
        assert compileType != CompileType.KEEP;
        Project project = object.getProject();
        DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
        boolean allowed = debuggerManager.checkForbiddenOperation(object.getConnection());
        if (allowed) {
            doCompileObject(object, compileType, compilerAction);
            DBContentType contentType = compilerAction.getContentType();
            updateFilesContentState(object, contentType);
        }
    }

    private void updateFilesContentState(DBSchemaObject object, DBContentType contentType) {
        Progress.background(
                getProject(),
                "Refreshing local content state", false,
                progress -> {
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
                });
    }

    public void compileInBackground(DBSchemaObject object, CompileType compileType, CompilerAction compilerAction) {
        Project project = getProject();
        ConnectionAction.invoke("compiling the object", false, object,
                action -> promptCompileTypeSelection(compileType, object,
                        selectedCompileType -> Progress.background(project, "Compiling " + object.getObjectType().getName(), false,
                                progress -> {
                                    doCompileObject(object, selectedCompileType, compilerAction);
                                    ConnectionHandler connection = object.getConnection();
                                    ProjectEvents.notify(project,
                                            CompileManagerListener.TOPIC,
                                            (listener) -> listener.compileFinished(connection, object));

                                    DBContentType contentType = compilerAction.getContentType();
                                    updateFilesContentState(object, contentType);
                                })),
                null,
                action -> {
                    ConnectionHandler connection = action.getConnection();
                    DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(project);
                    return debuggerManager.checkForbiddenOperation(connection);
                });
    }

    private void doCompileObject(DBSchemaObject object, CompileType compileType, CompilerAction compilerAction) {
        DBContentType contentType = compilerAction.getContentType();
        DBObjectStatusHolder objectStatus = object.getStatus();
        if (objectStatus.isNot(contentType, DBObjectStatus.COMPILING)) {
            objectStatus.set(contentType, DBObjectStatus.COMPILING, true);
            try {
                DatabaseInterface.run(true,
                        object.getConnection(),
                        (provider, connection) -> {
                            DatabaseMetadataInterface metadataInterface = provider.getMetadataInterface();

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
                        });

                createCompilerResult(object, compilerAction);
            } catch (SQLException e) {
                createErrorCompilerResult(compilerAction, object, contentType, e);
            }  finally{
                objectStatus.set(contentType, DBObjectStatus.COMPILING, false);
            }
        }
    }

    public void compileInvalidObjects(@NotNull DBSchema schema, CompileType compileType) {
        ConnectionAction.invoke("compiling the invalid objects", false, schema,
                action -> promptCompileTypeSelection(compileType, null,
                        selectedCompileType -> {
                            Progress.prompt(getProject(), "Compiling invalid objects", true,
                                    progress -> {
                                        Project project = getProject();
                                        progress.setIndeterminate(false);
                                        doCompileInvalidObjects(schema.getPackages(), "packages", progress, selectedCompileType);
                                        doCompileInvalidObjects(schema.getFunctions(), "functions", progress, selectedCompileType);
                                        doCompileInvalidObjects(schema.getProcedures(), "procedures", progress, selectedCompileType);
                                        doCompileInvalidObjects(schema.getDatasetTriggers(), "dataset triggers", progress, selectedCompileType);
                                        doCompileInvalidObjects(schema.getDatabaseTriggers(), "database triggers", progress, selectedCompileType);
                                        ConnectionHandler connection = schema.getConnection();
                                        ProjectEvents.notify(project,
                                                CompileManagerListener.TOPIC,
                                                (listener) -> listener.compileFinished(connection, null));

/*
                                    if (!progress.isCanceled()) {
                                        List<CompilerResult> compilerErrors = new ArrayList<>();
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
*/
                                    });
                        }),
                null,
                action -> {
                    DatabaseDebuggerManager debuggerManager = DatabaseDebuggerManager.getInstance(getProject());
                    ConnectionHandler connection = schema.getConnection();
                    return debuggerManager.checkForbiddenOperation(connection);
                });
    }

    private void doCompileInvalidObjects(List<? extends DBSchemaObject> objects, String description, ProgressIndicator progress, CompileType compileType) {
        if (progress.isCanceled()) return;

        progress.setText("Compiling invalid " + description + "...");
        int count = objects.size();
        for (int i=0; i< count; i++) {
            if (progress.isCanceled() || objects.size() == 0 /* may be disposed meanwhile*/) {
                break;
            } else {
                DBSchemaObject object = objects.get(i);
                progress.setFraction(Progress.progressOf(i, count));
                DBObjectStatusHolder objectStatus = object.getStatus();
                if (object.getContentType().isBundle()) {
                    for (DBContentType contentType : object.getContentType().getSubContentTypes()) {
                        if (objectStatus.isNot(contentType, DBObjectStatus.VALID)) {
                            CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, contentType);
                            doCompileObject(object, compileType, compilerAction);
                            progress.setText("Compiling " + object.getQualifiedNameWithType());
                        }
                    }
                } else {
                    if (objectStatus.isNot(DBObjectStatus.VALID)) {
                        CompilerAction compilerAction = new CompilerAction(CompilerActionSource.BULK_COMPILE, object.getContentType());
                        doCompileObject(object, compileType, compilerAction);
                        progress.setText("Compiling " + object.getQualifiedNameWithType());
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

    private void promptCompileTypeSelection(
            CompileType compileType,
            @Nullable DBSchemaObject program,
            @NotNull ParametricRunnable.Basic<CompileType> callback) {

        if (compileType == CompileType.ASK) {
            CompilerTypeSelectionDialog dialog = new CompilerTypeSelectionDialog(getProject(), program);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                compileType = dialog.getSelection();
                if (dialog.isRememberSelection()) {
                    OperationSettings operationSettings = OperationSettings.getInstance(getProject());
                    operationSettings.getCompilerSettings().setCompileType(compileType);
                }
                callback.run(compileType);
            }
        } else {
            callback.run(compileType);
        }
    }

    /***************************************
     *            ProjectComponent         *
     ***************************************/
    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.CompilerManager";
    }
}
