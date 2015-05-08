package com.dci.intellij.dbn.execution.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.notification.NotificationUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutCall;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.database.ScriptExecutionInput;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.script.ui.ScriptExecutionInputDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

@State(
        name = "DBNavigator.Project.ScriptExecutionManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ScriptExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element>{
    private final Map<VirtualFile, Process> activeProcesses = new HashMap<VirtualFile, Process>();
    private Map<DatabaseType, String> recentlyUsedInterfaces = new HashMap<DatabaseType, String>();

    private ScriptExecutionManager(Project project) {
        super(project);
    }

    public static ScriptExecutionManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, ScriptExecutionManager.class);
    }

    public List<CmdLineInterface> getAvailableInterfaces(DatabaseType databaseType) {
        ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(getProject());
        CmdLineInterfaceBundle commandLineInterfaces = executionEngineSettings.getScriptExecutionSettings().getCommandLineInterfaces();
        List<CmdLineInterface> interfaces = commandLineInterfaces.getInterfaces(databaseType);
        CmdLineInterface defaultInterface = CmdLineInterface.getDefault(databaseType);
        if (defaultInterface != null) {
            interfaces.add(0, defaultInterface);
        }
        return interfaces;
    }


    public void executeScript(final VirtualFile virtualFile) {
        final Project project = getProject();
        if (activeProcesses.containsKey(virtualFile)) {
            MessageUtil.showInfoDialog(project, "Information", "SQL Script \"" + virtualFile.getPath() + "\" is already running. \nWait for the execution to finish before running again.");
        } else {
            FileConnectionMappingManager connectionMappingManager = FileConnectionMappingManager.getInstance(project);

            ConnectionHandler activeConnection = connectionMappingManager.getActiveConnection(virtualFile);

            ScriptExecutionInputDialog inputDialog =
                    new ScriptExecutionInputDialog(project, virtualFile,
                            activeConnection);
            inputDialog.show();
            if (inputDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                final ConnectionHandler connectionHandler = inputDialog.getConnection();
                final DBSchema schema = inputDialog.getSchema();
                final CmdLineInterface cmdLineExecutable = inputDialog.getCmdLineInterface();
                connectionMappingManager.setActiveConnection(virtualFile, connectionHandler);
                connectionMappingManager.setCurrentSchema(virtualFile, schema);
                recentlyUsedInterfaces.put(connectionHandler.getDatabaseType(), cmdLineExecutable.getId());

                new BackgroundTask(project, "Executing database script", true, false) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        new SimpleTimeoutCall<Object>(100, TimeUnit.SECONDS, null) {
                            @Override
                            public Object call() throws Exception {
                                doExecuteScript(cmdLineExecutable, virtualFile, connectionHandler, schema);
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

    @Nullable
    public CmdLineInterface getRecentInterface(DatabaseType databaseType) {
        String id = recentlyUsedInterfaces.get(databaseType);
        if (id != null) {
            if (id.equals(CmdLineInterface.DEFAULT_ID)) {
                return CmdLineInterface.getDefault(databaseType);
            }

            ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(getProject());
            CmdLineInterfaceBundle commandLineInterfaces = executionEngineSettings.getScriptExecutionSettings().getCommandLineInterfaces();
            return commandLineInterfaces.getInterface(id);

        }
        return null;
    }

    private void doExecuteScript(CmdLineInterface cmdLineInterface, VirtualFile virtualFile, ConnectionHandler connectionHandler, DBSchema schema) throws Exception{
        activeProcesses.put(virtualFile, null);
        File tempScriptFile = null;
        BufferedReader logReader = null;
        LogOutputContext context = new LogOutputContext(connectionHandler, virtualFile, null);
        ExecutionManager executionManager = ExecutionManager.getInstance(getProject());
        try {
            String content = new String(virtualFile.contentsToByteArray());
            tempScriptFile = createTempScriptFile();

            DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
            ScriptExecutionInput executionInput = executionInterface.createScriptExecutionInput(cmdLineInterface,
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

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        Element interfacesElement = new Element("recently-used-interfaces");
        element.addContent(interfacesElement);
        for (DatabaseType databaseType : recentlyUsedInterfaces.keySet()) {
            Element interfaceElement = new Element("mapping");
            interfaceElement.setAttribute("database-type", databaseType.name());
            interfaceElement.setAttribute("interface-id", recentlyUsedInterfaces.get(databaseType));
            interfacesElement.addContent(interfaceElement);
        }
        return element;
    }

    @Override
    public void loadState(final Element element) {
        recentlyUsedInterfaces.clear();
        Element interfacesElement = element.getChild("recently-used-interfaces");
        if (interfacesElement != null) {
            for (Element interfaceElement : interfacesElement.getChildren()) {
                DatabaseType databaseType = DatabaseType.get(interfaceElement.getAttributeValue("database-type"));
                String interfaceId = interfaceElement.getAttributeValue("interface-id");
                recentlyUsedInterfaces.put(databaseType, interfaceId);
            }

        }
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