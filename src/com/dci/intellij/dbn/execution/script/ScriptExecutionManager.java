package com.dci.intellij.dbn.execution.script;

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
import com.dci.intellij.dbn.execution.script.ui.ScriptExecutionInputDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScriptExecutionManager extends AbstractProjectComponent {
    private Map<VirtualFile, Process> activeProcesses = new HashMap<VirtualFile, Process>();

    private ScriptExecutionManager(Project project) {
        super(project);
    }

    public static ScriptExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ScriptExecutionManager.class);
    }

    public void killProcess(VirtualFile virtualFile) {
        synchronized (activeProcesses) {
            Process process = activeProcesses.remove(virtualFile);
            if (process != null) {
                process.destroy();
            }
        }
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
                        new SimpleTimeoutCall<Object>(10, TimeUnit.SECONDS, null) {
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
        Process process = null;
        BufferedReader outputReader = null;
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

            if (true) {
                ProcessBuilder processBuilder = new ProcessBuilder(executionInput.getCommand());
                processBuilder.environment().putAll(executionInput.getEnvironmentVars());
                processBuilder.redirectErrorStream(true);
                process = processBuilder.start();
            } else {
                Runtime runtime = Runtime.getRuntime();
                process = runtime.exec(executionInput.getLineCommand());
            }
            activeProcesses.put(virtualFile, process);

            outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
            boolean addHeadline = true;
            while ((line = outputReader.readLine()) != null) {
                synchronized (activeProcesses) {
                    if (activeProcesses.containsKey(virtualFile)) {
                        executionManager.writeLogOutput(connectionHandler, virtualFile, line, addHeadline, true);
                        addHeadline = false;
                    } else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            if (process != null) {
                process.destroy();
            }
            throw e;
        } finally {
            if (outputReader != null) outputReader.close();
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