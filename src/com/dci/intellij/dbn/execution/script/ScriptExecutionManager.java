package com.dci.intellij.dbn.execution.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.script.ui.ScriptExecutionInputDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionManager extends AbstractProjectComponent {
    private final Map<VirtualFile, Process> activeProcesses = new HashMap<VirtualFile, Process>();

    private ScriptExecutionManager(Project project) {
        super(project);
    }

    public static ScriptExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ScriptExecutionManager.class);
    }

    public void executeScript(final VirtualFile virtualFile) {
        final Project project = getProject();
        if (activeProcesses.containsKey(virtualFile)) {
            MessageUtil.showInfoDialog(project, "Information", "SQL Script \"" + virtualFile.getPath() + "\" is already running. \nWait for the execution to finish before running again.");
        } else {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);

            ScriptExecutionInputDialog inputDialog =
                    new ScriptExecutionInputDialog(project, virtualFile,
                            connectionMappingManager.getActiveConnection(virtualFile), connectionMappingManager.getCurrentSchema(virtualFile));
            inputDialog.show();
            if (inputDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                final ConnectionHandler connectionHandler = inputDialog.getConnection();
                final DBSchema schema = inputDialog.getSchema();
                connectionMappingManager.setActiveConnection(virtualFile, connectionHandler);
                connectionMappingManager.setCurrentSchema(virtualFile, schema);

                new BackgroundTask(project, "Executing database script", true, false) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        new SimpleTimeoutCall<Object>(100, TimeUnit.SECONDS, null) {
                            @Override
                            public Object call() throws Exception {
                                doExecuteScript(virtualFile, connectionHandler, schema);
                                return null;
                            }

                            @Override
                            protected Object handleException(Exception e) {
                                String causeMessage = e instanceof TimeoutException ? "Operation has timed out" : e.getMessage();
                                NotificationUtil.sendErrorNotification(project, "Script execution", "Error executing SQL script \"" + virtualFile.getPath() + "\". Details: " + causeMessage);
                                return super.handleException(e);
                            }
                        }.start();
                    }
                }.start();
            }
        }
    }

    private void doExecuteScript(VirtualFile virtualFile, ConnectionHandler connectionHandler, DBSchema schema) throws Exception{
        activeProcesses.put(virtualFile, null);
        File tempScriptFile = null;
        BufferedReader logReader = null;
        LogOutputContext context = new LogOutputContext(connectionHandler, virtualFile, null);
        ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
        try {
            String content = new String(virtualFile.contentsToByteArray());
            tempScriptFile = createTempScriptFile();

            DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
            ScriptExecutionInput executionInput = executionInterface.createScriptExecutionInput(null,
                    tempScriptFile.getPath(),
                    content,
                    schema == null ? null : schema.getName(),
                    connectionHandler.getDatabaseInfo(),
                    connectionHandler.getAuthenticationInfo()
            );

            FileUtil.writeToFile(tempScriptFile, executionInput.getTextContent());

            ProcessBuilder processBuilder = new ProcessBuilder(executionInput.getCommand());
            processBuilder.environment().putAll(executionInput.getEnvironmentVars());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

/*
                Runtime runtime = Runtime.getRuntime();
                process = runtime.exec(executionInput.getLineCommand());
*/
            context.setProcess(process);
            activeProcesses.put(virtualFile, process);

            context.setHideEmptyLines(false);
            context.start();
            String line;
            executionManager.writeLogOutput(context, LogOutput.createSysOutput(context, " - Script execution started"));

            logReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = logReader.readLine()) != null) {
                if (context.isActive()) {
                    LogOutput stdOutput = LogOutput.createStdOutput(line);
                    executionManager.writeLogOutput(context, stdOutput);
                } else {
                    break;
                }
            }
            executionManager.writeLogOutput(context, LogOutput.createSysOutput(context, context.isStopped() ? " - Script execution interrupted by used" : " - Script execution finished"));

        } catch (Exception e) {
            executionManager.writeLogOutput(context, LogOutput.createErrOutput(e.getMessage()));
            executionManager.writeLogOutput(context, LogOutput.createSysOutput(context, " - Script execution finished with errors"));
            throw e;
        } finally {
            context.finish();
            if (logReader != null) logReader.close();
            activeProcesses.remove(virtualFile);
            if (tempScriptFile != null && tempScriptFile.exists()) {
                tempScriptFile.delete();
            }
        }
    }


    private File createTempScriptFile() throws IOException {
        return File.createTempFile(UUID.randomUUID().toString(), ".sql");
    }

    /*********************************************************
     *                    ProjectComponent                   *
     *********************************************************/
    @NotNull
    @NonNls
    public String getComponentName() {
        return "DBNavigator.Project.ScriptExecutionManager";
    }

    @Override
    public void dispose() {
        super.dispose();
        activeProcesses.clear();
    }

}