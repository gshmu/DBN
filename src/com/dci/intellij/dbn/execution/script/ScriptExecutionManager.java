package com.dci.intellij.dbn.execution.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jdesktop.swingx.util.OS;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.message.MessageCallback;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.CancellableDatabaseCall;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleCallback;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingManager;
import com.dci.intellij.dbn.database.CmdLineExecutionInput;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.logging.LogOutput;
import com.dci.intellij.dbn.execution.logging.LogOutputContext;
import com.dci.intellij.dbn.execution.script.options.ScriptExecutionSettings;
import com.dci.intellij.dbn.execution.script.ui.CmdLineInterfaceInputDialog;
import com.dci.intellij.dbn.execution.script.ui.ScriptExecutionInputDialog;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.application.PathManagerEx;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

@State(
        name = "DBNavigator.Project.ScriptExecutionManager",
        storages = {
                @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
                @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class ScriptExecutionManager extends AbstractProjectComponent implements PersistentStateComponent<Element>{
    private static final SecureRandom TMP_FILE_RANDOMIZER = new SecureRandom();
    private final Map<VirtualFile, Process> activeProcesses = new HashMap<VirtualFile, Process>();
    private Map<DatabaseType, String> recentlyUsedInterfaces = new HashMap<DatabaseType, String>();
    private boolean clearOutputOption = true;

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
            DBSchema currentSchema = connectionMappingManager.getCurrentSchema(virtualFile);

            final ScriptExecutionInput executionInput = new ScriptExecutionInput(getProject(), virtualFile, activeConnection, currentSchema, clearOutputOption);
            ScriptExecutionSettings scriptExecutionSettings = ExecutionEngineSettings.getInstance(project).getScriptExecutionSettings();
            int timeout = scriptExecutionSettings.getExecutionTimeout();
            executionInput.setExecutionTimeout(timeout);

            ScriptExecutionInputDialog inputDialog = new ScriptExecutionInputDialog(project,executionInput);

            inputDialog.show();
            if (inputDialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                final ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
                final DBSchema schema = executionInput.getSchema();
                final CmdLineInterface cmdLineExecutable = executionInput.getCmdLineInterface();
                connectionMappingManager.setActiveConnection(virtualFile, connectionHandler);
                connectionMappingManager.setCurrentSchema(virtualFile, schema);
                if (connectionHandler != null) {
                    recentlyUsedInterfaces.put(connectionHandler.getDatabaseType(), cmdLineExecutable.getId());
                }
                clearOutputOption = executionInput.isClearOutput();

                new BackgroundTask(project, "Executing database script", true, true) {
                    @Override
                    protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                        try {
                            doExecuteScript(executionInput);
                        } catch (Exception e) {
                            MessageUtil.showErrorDialog(getProject(), "Error", "Error executing SQL Script \"" + virtualFile.getPath() + "\". " + e.getMessage());
                        }
                    }
                }.start();
            }
        }
    }

    private void doExecuteScript(final ScriptExecutionInput input) throws Exception {
        ExecutionContext context = input.getExecutionContext();
        context.setExecuting(true);
        ConnectionHandler connectionHandler = FailsafeUtil.get(input.getConnectionHandler());
        final VirtualFile sourceFile = input.getSourceFile();
        activeProcesses.put(sourceFile, null);

        final Project project = getProject();
        final AtomicReference<File> tempScriptFile = new AtomicReference<File>();
        final AtomicReference<BufferedReader> logReader = new AtomicReference<BufferedReader>();
        final LogOutputContext outputContext = new LogOutputContext(connectionHandler, sourceFile, null);
        final ExecutionManager executionManager = ExecutionManager.getInstance(project);
        int timeout = input.getExecutionTimeout();

        try {
            new CancellableDatabaseCall<Object>(connectionHandler, null, timeout, TimeUnit.SECONDS) {
                @Override
                public Object execute() throws Exception {
                    ConnectionHandler connectionHandler = FailsafeUtil.get(input.getConnectionHandler());
                    DBSchema schema = input.getSchema();

                    String content = new String(sourceFile.contentsToByteArray());
                    File temporaryScriptFile = createTempScriptFile();
                    tempScriptFile.set(temporaryScriptFile);

                    DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
                    CmdLineInterface cmdLineInterface = input.getCmdLineInterface();
                    CmdLineExecutionInput executionInput = executionInterface.createScriptExecutionInput(cmdLineInterface,
                            temporaryScriptFile.getPath(),
                            content,
                            schema == null ? null : schema.getName(),
                            connectionHandler.getDatabaseInfo(),
                            connectionHandler.getAuthenticationInfo()
                    );

                    FileUtil.writeToFile(temporaryScriptFile, executionInput.getTextContent());
                    if (!temporaryScriptFile.isFile()) {
                        throw new IllegalStateException("Failed to create temporary script file " + temporaryScriptFile + ". Check access rights at location.");
                    }

                    ProcessBuilder processBuilder = new ProcessBuilder(executionInput.getCommand());
                    processBuilder.environment().putAll(executionInput.getEnvironmentVars());
                    processBuilder.redirectErrorStream(true);
                    executionManager.writeLogOutput(outputContext, LogOutput.createSysOutput(outputContext, " - Executing command: " + executionInput.getLineCommand(), input.isClearOutput()));
                    Process process = processBuilder.start();

                    outputContext.setProcess(process);
                    activeProcesses.put(sourceFile, process);

                    outputContext.setHideEmptyLines(false);
                    outputContext.start();
                    String line;
                    executionManager.writeLogOutput(outputContext, LogOutput.createSysOutput(outputContext, " - Script execution started", input.isClearOutput()));

                    BufferedReader consoleReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    logReader.set(consoleReader);
                    while ((line = consoleReader.readLine()) != null) {
                        if (outputContext.isActive()) {
                            LogOutput stdOutput = LogOutput.createStdOutput(line);
                            executionManager.writeLogOutput(outputContext, stdOutput);
                        } else {
                            break;
                        }
                    }

                    LogOutput logOutput = LogOutput.createSysOutput(outputContext,
                            outputContext.isStopped() ?
                                    " - Script execution interrupted by user" :
                                    " - Script execution finished", false);
                    executionManager.writeLogOutput(outputContext, logOutput);
                    EventUtil.notify(project, ScriptExecutionManagerListener.TOPIC).scriptExecuted(sourceFile);
                    return null;
                }

                @Override
                public void cancel() throws Exception {
                    outputContext.stop();
                }

                @Override
                public void handleTimeout() throws SQLTimeoutException {
                    new SimpleBackgroundTask("handling script execution timeout") {
                        @Override
                        protected void execute() {
                            MessageUtil.showErrorDialog(project,
                                    "Script execution timeout",
                                    "The script execution has timed out",
                                    new String[]{"Retry", "Cancel"}, 0,
                                    new MessageCallback(0) {
                                        @Override
                                        protected void execute() {
                                            executeScript(sourceFile);
                                        }
                                    });
                        }
                    }.start();
                }

                @Override
                public void handleException(final Throwable e) throws SQLException {
                    new SimpleBackgroundTask("handling script execution exception") {
                        @Override
                        protected void execute() {
                            MessageUtil.showErrorDialog(project,
                                    "Script execution error",
                                    "Error executing SQL script \"" + sourceFile.getPath() + "\". \nDetails: " + e.getMessage(),
                                    new String[]{"Retry", "Cancel"}, 0,
                                    new MessageCallback(0) {
                                        @Override
                                        protected void execute() {
                                            executeScript(sourceFile);
                                        }
                                    });
                        }
                    }.start();
                }
            }.start();
        } catch (Exception e) {
            if (e instanceof ProcessCanceledException) {
                //executionManager.writeLogOutput(outputContext, LogOutput.createSysOutput(outputContext, " - Script execution cancelled by user", false));
            } else {
                executionManager.writeLogOutput(outputContext, LogOutput.createErrOutput(e.getMessage()));
                executionManager.writeLogOutput(outputContext, LogOutput.createSysOutput(outputContext, " - Script execution finished with errors", false));
                throw e;
            }
        } finally {
            context.setExecuting(false);
            outputContext.finish();
            BufferedReader consoleReader = logReader.get();
            if (consoleReader != null) consoleReader.close();
            activeProcesses.remove(sourceFile);
            File temporaryScriptFile = tempScriptFile.get();
            if (temporaryScriptFile != null && temporaryScriptFile.exists()) {
                FileUtil.delete(temporaryScriptFile);
            }
        }
    }

    public void createCmdLineInterface(@NotNull DatabaseType databaseType, @Nullable Set<String> bannedNames, SimpleCallback<CmdLineInterface> callback) {
        boolean updateSettings = false;
        VirtualFile virtualFile = selectCmdLineExecutable(databaseType, null);
        if (virtualFile != null) {
            Project project = getProject();
            ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(project);
            if (bannedNames == null) {
                bannedNames = executionEngineSettings.getScriptExecutionSettings().getCommandLineInterfaces().getInterfaceNames();
                updateSettings = true;
            }

            CmdLineInterface cmdLineInterface = new CmdLineInterface(databaseType, virtualFile.getPath(), CmdLineInterface.getDefault(databaseType).getName(), null);
            CmdLineInterfaceInputDialog dialog = new CmdLineInterfaceInputDialog(project, cmdLineInterface, bannedNames);
            dialog.show();
            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                callback.start(cmdLineInterface);
                if (updateSettings) {
                    CmdLineInterfaceBundle commandLineInterfaces = executionEngineSettings.getScriptExecutionSettings().getCommandLineInterfaces();
                    commandLineInterfaces.add(cmdLineInterface);
                }
            }
        }
    }

    @Nullable
    public VirtualFile selectCmdLineExecutable(@NotNull DatabaseType databaseType, @Nullable String selectedExecutable) {
        FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        CmdLineInterface defaultCli = CmdLineInterface.getDefault(databaseType);
        String extension = OS.isWindows() ? ".exe" : "";
        fileChooserDescriptor.
                withTitle("Select Command-Line Client").
                withDescription("Select Command-Line Interface executable (" + defaultCli.getExecutablePath() + extension + ")").
                withShowHiddenFiles(true);
        VirtualFile selectedFile = StringUtil.isEmpty(selectedExecutable) ? null : LocalFileSystem.getInstance().findFileByPath(selectedExecutable);
        VirtualFile[] virtualFiles = FileChooser.chooseFiles(fileChooserDescriptor, getProject(), selectedFile);
        return virtualFiles.length == 1 ? virtualFiles[0] : null;
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

    public boolean getClearOutputOption() {
        return clearOutputOption;
    }

    public void setClearOutputOption(boolean clearOutputOption) {
        this.clearOutputOption = clearOutputOption;
    }

    private File createTempScriptFile() throws IOException {
        File tempFile = File.createTempFile("DBN-", ".sql");
        if (!tempFile.isFile()) {
            long n = TMP_FILE_RANDOMIZER.nextLong();
            n = n == Long.MIN_VALUE ? 0 : Math.abs(n);
            String tempFileName = "DBN-" + n;

            tempFile = FileUtil.createTempFile(tempFileName, ".sql");
            if (!tempFile.isFile()) {
                Path systemDir = PathManagerEx.getAppSystemDir();
                File systemTempDir = new File(systemDir.toFile(), "tmp");
                tempFile = new File(systemTempDir, tempFileName);
                FileUtil.createParentDirs(tempFile);
                FileUtil.delete(tempFile);
                FileUtil.createIfDoesntExist(tempFile);
            }
        }
        return tempFile;
    }

    /****************************************
     *       PersistentStateComponent       *
     *****************************************/
    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("state");
        SettingsUtil.setBooleanAttribute(element, "clear-outputs", clearOutputOption);
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
        clearOutputOption = SettingsUtil.getBooleanAttribute(element, "clear-outputs", clearOutputOption);
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