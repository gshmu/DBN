package com.dci.intellij.dbn.execution.compiler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionUtil;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.debugger.DatabaseDebuggerManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.compiler.ui.CompilerTypeSelectionDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

public class DatabaseCompilerManager extends AbstractProjectComponent {
    private DatabaseCompilerManager(Project project) {
        super(project);
    }

    public static DatabaseCompilerManager getInstance(Project project) {
        return project.getComponent(DatabaseCompilerManager.class);
    }

    public void createCompilerResult(DBSchemaObject object) {
        CompilerResult compilerResult = new CompilerResult(object);
        ExecutionManager.getInstance(object.getProject()).showExecutionConsole(compilerResult);
    }

    public void createErrorCompilerResult(DBSchemaObject object, Exception e) {
        CompilerResult compilerResult = new CompilerResult(object, "Could not perform compile operation. \nCause: " + e.getMessage());
        ExecutionManager.getInstance(object.getProject()).showExecutionConsole(compilerResult);
    }

    public void compileObject(DBSchemaObject object, CompileType compileType, boolean silently) {
        Project project = object.getProject();
        boolean allowed = DatabaseDebuggerManager.getInstance(project).checkForbiddenOperation(object.getConnectionHandler());
        if (allowed) {
            CompileType selectedCompileType = getCompileTypeSelection(compileType, object);
            if (selectedCompileType != null) {
                doCompileObject(object, object.getContentType(), selectedCompileType, silently);
                if (DatabaseFileSystem.getInstance().isFileOpened(object)) {
                    DBEditableObjectVirtualFile databaseFile = object.getVirtualFile();
                    if (object.getContentType().isBundle()) {
                        for (DBContentType contentType : object.getContentType().getSubContentTypes()) {
                            DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                            sourceCodeFile.updateChangeTimestamp();
                        }
                    } else {
                        DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(object.getContentType());
                        sourceCodeFile.updateChangeTimestamp();
                    }
                }
            }
        }
    }

    public void compileObject(final DBSchemaObject object, final DBContentType contentType, CompileType compileType, final boolean silently) {
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        boolean canConnect = ConnectionUtil.assertCanConnect(connectionHandler, "compiling object");
        if (canConnect) {
            Project project = object.getProject();
            boolean allowed = DatabaseDebuggerManager.getInstance(project).checkForbiddenOperation(connectionHandler);
            if (allowed) {
                final CompileType selectedCompileType = getCompileTypeSelection(compileType, object);
                if (selectedCompileType != null) {
                    new BackgroundTask(object.getProject(), "Compiling " + object.getQualifiedNameWithType(), true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            doCompileObject(object, contentType, selectedCompileType, silently);
                            if (DatabaseFileSystem.getInstance().isFileOpened(object)) {
                                DBEditableObjectVirtualFile databaseFile = object.getVirtualFile();
                                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) databaseFile.getContentFile(contentType);
                                sourceCodeFile.updateChangeTimestamp();
                            }
                        }
                    }.start();
                }
            }
        }
    }

    private void doCompileObject(DBSchemaObject object, DBContentType contentType, CompileType compileType, boolean silently) {
        object.getStatus().set(contentType, DBObjectStatus.COMPILING, true);
        Connection connection = null;
        DatabaseCompilerManager compilerManager = DatabaseCompilerManager.getInstance(getProject());
        ConnectionHandler connectionHandler = object.getConnectionHandler();
        try {
            connection = connectionHandler.getPoolConnection();
            DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();

            boolean isDebug = compileType == CompileType.DEBUG;

            if (compileType == CompileType.KEEP) {
                isDebug = object.getStatus().is(DBObjectStatus.DEBUG);
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

            if (!silently) compilerManager.createCompilerResult(object);
        } catch (SQLException e) {
            if (!silently) compilerManager.createErrorCompilerResult(object, e);
        }  finally{
            connectionHandler.freePoolConnection(connection);
            if (!silently) connectionHandler.getObjectBundle().refreshObjectsStatus(object);
            object.getStatus().set(contentType, DBObjectStatus.COMPILING, false);
        }
    }

    public void compileInvalidObjects(final DBSchema schema, final CompileType compileType) {
        final Project project = schema.getProject();
        final ConnectionHandler connectionHandler = schema.getConnectionHandler();
        boolean canConnect = ConnectionUtil.assertCanConnect(connectionHandler, "compiling invalid objects");
        if (canConnect) {
            boolean allowed = DatabaseDebuggerManager.getInstance(project).checkForbiddenOperation(connectionHandler);
            if (allowed) {
                final CompileType selectedCompileType = getCompileTypeSelection(compileType, null);
                if (selectedCompileType != null) {
                    new BackgroundTask(project, "Compiling invalid objects", false, true) {
                        public void execute(@NotNull ProgressIndicator progressIndicator) {
                            doCompileInvalidObjects(schema.getPackages(), "packages", progressIndicator, selectedCompileType);
                            doCompileInvalidObjects(schema.getFunctions(), "functions", progressIndicator, selectedCompileType);
                            doCompileInvalidObjects(schema.getProcedures(), "procedures", progressIndicator, selectedCompileType);
                            doCompileInvalidObjects(schema.getTriggers(), "triggers", progressIndicator, selectedCompileType);
                            connectionHandler.getObjectBundle().refreshObjectsStatus(null);

                            if (!progressIndicator.isCanceled()) {
                                List<CompilerResult> compilerErrors = new ArrayList<CompilerResult>();
                                buildCompilationErrors(schema.getPackages(), compilerErrors);
                                buildCompilationErrors(schema.getFunctions(), compilerErrors);
                                buildCompilationErrors(schema.getProcedures(), compilerErrors);
                                buildCompilationErrors(schema.getTriggers(), compilerErrors);
                                if (compilerErrors.size() > 0) {
                                    ExecutionManager.getInstance(project).showExecutionConsole(compilerErrors);
                                }
                            }
                        }
                    }.start();
                }
            }
        }

    }

    private void doCompileInvalidObjects(List<? extends DBSchemaObject> objects, String description, ProgressIndicator progressIndicator, CompileType compileType) {
        if (progressIndicator.isCanceled()) return;

        progressIndicator.setText("Compiling invalid " + description + "...");
        int count = objects.size();
        for (int i=0; i< count; i++) {
            if (progressIndicator.isCanceled()) {
                break;
            } else {
                progressIndicator.setFraction(CommonUtil.getProgressPercentage(i, count));
                DBSchemaObject object = objects.get(i);
                if (object.getContentType().isBundle()) {
                    for (DBContentType contentType : object.getContentType().getSubContentTypes()) {
                        if (!object.getStatus().is(contentType, DBObjectStatus.VALID)) {
                            doCompileObject(object, contentType, compileType, true);
                            progressIndicator.setText2("Compiling " + object.getQualifiedNameWithType());
                        }
                    }
                } else {
                    if (!object.getStatus().is(DBObjectStatus.VALID)) {
                        doCompileObject(object, object.getContentType(), compileType, true);
                        progressIndicator.setText2("Compiling " + object.getQualifiedNameWithType());
                    }
                }
            }
        }
    }

    private void buildCompilationErrors(List<? extends DBSchemaObject> objects, List<CompilerResult> compilerErrors) {
        for (DBSchemaObject object : objects) {
            if (!object.getStatus().is(DBObjectStatus.VALID)) {
                CompilerResult compilerResult = new CompilerResult(object);
                if (compilerResult.isError()) {
                    compilerErrors.add(compilerResult);
                }
            }
        }
    }

    private CompileType getCompileTypeSelection(CompileType compileType, @Nullable DBSchemaObject program) {
        if (compileType == CompileType.ASK) {
            CompilerTypeSelectionDialog dialog = new CompilerTypeSelectionDialog(getProject(), program);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                compileType = dialog.getSelection();
                if (dialog.rememberSelection()) {
                    ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(getProject());
                    executionEngineSettings.getCompilerSettings().setCompileType(compileType);
                }
            } else {
                compileType = null;
            }
        }
        return compileType;
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
