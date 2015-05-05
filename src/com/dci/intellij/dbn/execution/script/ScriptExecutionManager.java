package com.dci.intellij.dbn.execution.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionManager extends AbstractProjectComponent {

    private ScriptExecutionManager(Project project) {
        super(project);
    }

    public static ScriptExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ScriptExecutionManager.class);
    }

    public void executeScript(final VirtualFile virtualFile) {
        FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(getProject());
        final ConnectionHandler connectionHandler = connectionMappingManager.getActiveConnection(virtualFile);
        if (connectionHandler != null && !connectionHandler.isVirtual()) {
            new BackgroundTask(connectionHandler.getProject(), "Executing database script", true, false) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    new SimpleTimeoutCall<Object>(60, TimeUnit.SECONDS, null) {
                        @Override
                        public Object call() throws Exception {
                            File tempScriptFile = null;
                            try {
                                String content = new String(virtualFile.contentsToByteArray());
                                tempScriptFile = createTempScriptFile();

                                DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
                                ScriptExecutionInput executionInput = executionInterface.createScriptExecutionInput(null,
                                        tempScriptFile.getPath(),
                                        content,
                                        null,
                                        connectionHandler.getDatabaseInfo(),
                                        connectionHandler.getAuthenticationInfo()
                                );

                                FileUtil.writeToFile(tempScriptFile, executionInput.getTextContent());

                                Process process;
                                if (true) {
                                    ProcessBuilder processBuilder = new ProcessBuilder(executionInput.getCommand());
                                    processBuilder.environment().putAll(executionInput.getEnvironmentVars());
                                    processBuilder.redirectErrorStream(true);
                                    process = processBuilder.start();
                                } else {
                                    Runtime runtime = Runtime.getRuntime();
                                    process = runtime.exec(executionInput.getLineCommand());
                                }

                                BufferedReader bri = new BufferedReader(new InputStreamReader(process.getInputStream()));

                                String line;
                                ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
                                boolean addHeadline = true;
                                while ((line = bri.readLine()) != null) {
                                    executionManager.writeLogOutput(connectionHandler, virtualFile, line, addHeadline, true);
                                    addHeadline = false;
                                }
                                bri.close();

                            } catch (Exception e) {
                                MessageUtil.showErrorDialog(getProject(), "Script Execution Error", "Error executing script " + virtualFile.getName() + ".\n" + e.getMessage());
                                e.printStackTrace();
                            } finally {
                                if (tempScriptFile != null && tempScriptFile.exists()) {
                                    tempScriptFile.delete();
                                }
                            }
                            return null;
                        }
                    }.start();
                }
            }.start();
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
}