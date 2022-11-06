package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.notification.NotificationGroup;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Messages;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.context.ConnectionProvider;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.common.debug.DebuggerVersionInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUpdaterFileEditorListener;
import com.dci.intellij.dbn.debugger.common.config.*;
import com.dci.intellij.dbn.debugger.common.process.DBProgramRunner;
import com.dci.intellij.dbn.debugger.jdbc.process.DBMethodJdbcRunner;
import com.dci.intellij.dbn.debugger.jdbc.process.DBStatementJdbcRunner;
import com.dci.intellij.dbn.debugger.jdwp.process.DBMethodJdwpRunner;
import com.dci.intellij.dbn.debugger.jdwp.process.DBStatementJdwpRunner;
import com.dci.intellij.dbn.debugger.options.DebuggerSettings;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.file.DBConsoleVirtualFile;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.*;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.common.message.MessageCallback.when;
import static com.dci.intellij.dbn.common.util.Commons.list;

@State(
    name = DatabaseDebuggerManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DatabaseDebuggerManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DebuggerManager";

    public static final String GENERIC_METHOD_RUNNER_HINT =
            "This is the generic Database Method debug runner. " +
                    "This is used when debugging is invoked on a given method. " +
                    "No specific method information can be specified here.";

    public static final String GENERIC_STATEMENT_RUNNER_HINT =
            "This is the generic Database Statement debug runner. " +
                    "This is used when debugging is invoked on a given SQL statement. " +
                    "No specific statement information can be specified here.";

    private final Set<ConnectionRef> activeDebugSessions = new HashSet<>();

    private DatabaseDebuggerManager(Project project) {
        super(project, COMPONENT_NAME);

        ProjectEvents.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, new DBBreakpointUpdaterFileEditorListener());
    }

    public static DatabaseDebuggerManager getInstance(@NotNull Project project) {
        return projectService(project, DatabaseDebuggerManager.class);
    }

    public void registerDebugSession(ConnectionHandler connection) {
        activeDebugSessions.add(connection.ref());
    }

    public void unregisterDebugSession(ConnectionHandler connection) {
        activeDebugSessions.remove(connection.ref());
    }

    public boolean checkForbiddenOperation(ConnectionHandler connection) {
        return checkForbiddenOperation(connection, null);
    }

    public boolean checkForbiddenOperation(ConnectionProvider connection) {
        return checkForbiddenOperation(connection.getConnection());
    }


    public boolean checkForbiddenOperation(ConnectionHandler connection, String message) {
        // TODO add flag on connection handler instead of this
        if (activeDebugSessions.contains(connection.ref())) {
            Messages.showErrorDialog(getProject(), message == null ? "Operation not supported during active debug session." : message);
            return false;
        }
        return true;
    }

    private static DBMethodRunConfigType getMethodConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBMethodRunConfigType.class);
    }

    private static DBStatementRunConfigType getStatementConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBStatementRunConfigType.class);
    }

    public static String createMethodConfigurationName(DBMethod method) {
        DBMethodRunConfigType configurationType = getMethodConfigurationType();
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(method.getProject());
        List<RunnerAndConfigurationSettings> configurationSettings = runManager.getConfigurationSettingsList(configurationType);

        String name = method.getName();
        while (nameExists(configurationSettings, name)) {
            name = Naming.nextNumberedIdentifier(name, true);
        }
        return name;
    }

    private static boolean nameExists(List<RunnerAndConfigurationSettings> configurationSettings, String name) {
        return Lists.anyMatch(configurationSettings, configurationSetting -> Objects.equals(configurationSetting.getName(), name));
    }

    public static boolean isDebugConsole(VirtualFile virtualFile) {
        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            return consoleVirtualFile.getType() == DBConsoleType.DEBUG;
        }
        return false;
    }

    public static void checkJdwpConfiguration() throws RuntimeConfigurationError {
        if (!DBDebuggerType.JDWP.isSupported()) {
            ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
            throw new RuntimeConfigurationError("JDWP debugging is not supported in \"" + applicationInfo.getVersionName() + " " + applicationInfo.getFullVersion()+ "\". Please use Classic debugger over JDBC instead.");
        }
    }

    @NotNull
    private RunnerAndConfigurationSettings getDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType){
        return Failsafe.nn(getDefaultConfig(configurationType, debuggerType, true));
    }

    @Nullable
    private RunnerAndConfigurationSettings getDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType, boolean create){
        Project project = getProject();
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(project);
        List<RunnerAndConfigurationSettings> configurationSettings = runManager.getConfigurationSettingsList(configurationType);
        for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            RunConfiguration configuration = configurationSetting.getConfiguration();
            if (configuration instanceof DBRunConfig) {
                DBRunConfig dbRunConfiguration = (DBRunConfig) configuration;
                if (dbRunConfiguration.getCategory() == DBRunConfigCategory.GENERIC && dbRunConfiguration.getDebuggerType() == debuggerType) {
                    return configurationSetting;
                }
            }
        }
        if (create) {
            return createDefaultConfig(configurationType, debuggerType);
        }
        return null;
    }

    private RunnerAndConfigurationSettings createDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType) {
        RunnerAndConfigurationSettings defaultRunnerConfig = getDefaultConfig(configurationType, debuggerType, false);
        if (defaultRunnerConfig == null) {
            Project project = getProject();
            RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(project);
            DBRunConfigFactory configurationFactory = configurationType.getConfigurationFactory(debuggerType);
            String defaultRunnerName = configurationType.getDefaultRunnerName();
            if (debuggerType == DBDebuggerType.JDWP) {
                defaultRunnerName = defaultRunnerName + " (JDWP)";
            }

            DBRunConfig runConfiguration = configurationFactory.createConfiguration(project, defaultRunnerName, DBRunConfigCategory.GENERIC);
            RunnerAndConfigurationSettings configuration = runManager.createConfiguration(runConfiguration, configurationFactory);
            runManager.addConfiguration(configuration, false);
            //runManager.setTemporaryConfiguration(configuration);
            return configuration;
        }
        return defaultRunnerConfig;
    }

    public void startMethodDebugger(@NotNull DBMethod method) {
        startDebugger((debuggerType) -> {
            Project project = getProject();
            DBMethodRunConfigType configurationType = getMethodConfigurationType();
            RunnerAndConfigurationSettings runConfigurationSetting = null;
            if (getDebuggerSettings().isUseGenericRunners()) {

                runConfigurationSetting = getDefaultConfig(configurationType, debuggerType);
                MethodExecutionManager executionManager = MethodExecutionManager.getInstance(project);
                DBMethodRunConfig runConfiguration = (DBMethodRunConfig) runConfigurationSetting.getConfiguration();

                MethodExecutionInput executionInput = executionManager.getExecutionInput(method);
                runConfiguration.setExecutionInput(executionInput);

            } else {
                RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(project);
                List<RunnerAndConfigurationSettings> configurationSettings = runManager.getConfigurationSettingsList(configurationType);
                for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
                    DBMethodRunConfig availableRunConfiguration = (DBMethodRunConfig) configurationSetting.getConfiguration();
                    if (availableRunConfiguration.getCategory() == DBRunConfigCategory.CUSTOM && method.equals(availableRunConfiguration.getMethod())) {
                        runConfigurationSetting = configurationSetting;
                        break;
                    }
                }

                // check whether a configuration already exists for the given method
                if (runConfigurationSetting == null) {
                    DBMethodRunConfigFactory configurationFactory = configurationType.getConfigurationFactory(debuggerType);
                    DBMethodRunConfig runConfiguration = configurationFactory.createConfiguration(method);
                    runConfigurationSetting = runManager.createConfiguration(runConfiguration, configurationFactory);
                    runManager.addConfiguration(runConfigurationSetting, false);
                    runManager.setTemporaryConfiguration(runConfigurationSetting);

                }
                runManager.setSelectedConfiguration(runConfigurationSetting);
            }

            String runnerId =
                    debuggerType == DBDebuggerType.JDBC ? DBMethodJdbcRunner.RUNNER_ID :
                    debuggerType == DBDebuggerType.JDWP ? DBMethodJdwpRunner.RUNNER_ID : null;
            if (runnerId != null) {
                ProgramRunner programRunner = ProgramRunner.findRunnerById(runnerId);
                if (programRunner != null) {
                    try {
                        Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
                        if (executorInstance == null) {
                            throw new ExecutionException("Could not resolve debug executor");
                        }

                        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executorInstance, programRunner, runConfigurationSetting, project);
                        programRunner.execute(executionEnvironment);
                    } catch (ExecutionException e) {
                        Messages.showErrorDialog(
                                project, "Could not start debugger for " + method.getQualifiedName() + ". \n" +
                                        "Cause: " + e.getMessage());
                    }
                }
            }
        });
    }

    public void startStatementDebugger(@NotNull StatementExecutionProcessor executionProcessor) {
        startDebugger(debuggerType -> {
            Project project = getProject();

            DBStatementRunConfigType configurationType = getStatementConfigurationType();
            RunnerAndConfigurationSettings runConfigurationSetting;
            runConfigurationSetting = getDefaultConfig(configurationType, debuggerType);
            DBStatementRunConfig runConfiguration = (DBStatementRunConfig) runConfigurationSetting.getConfiguration();

            runConfiguration.setExecutionInput(executionProcessor.getExecutionInput());

            String runnerId =
                    debuggerType == DBDebuggerType.JDBC ? DBStatementJdbcRunner.RUNNER_ID :
                            debuggerType == DBDebuggerType.JDWP ? DBStatementJdwpRunner.RUNNER_ID :
                                    DBProgramRunner.INVALID_RUNNER_ID;

            ProgramRunner programRunner = ProgramRunner.findRunnerById(runnerId);
            if (programRunner != null) {
                try {
                    Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
                    if (executorInstance == null) {
                        throw new ExecutionException("Could not resolve debug executor");
                    }

                    ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executorInstance, programRunner, runConfigurationSetting, project);
                    programRunner.execute(executionEnvironment);
                } catch (ExecutionException e) {
                    Messages.showErrorDialog(
                            project, "Could not start statement debugger. \n" +
                                    "Cause: " + e.getMessage());
                }
            }
        });
    }


    private void startDebugger(@NotNull ParametricRunnable.Basic<DBDebuggerType> debuggerStarter) {

        getDebuggerSettings().getDebuggerType().resolve(list(),
                debuggerTypeOption -> {
                    DBDebuggerType debuggerType = debuggerTypeOption.getDebuggerType();
                    if (debuggerType != null) {
                        if (debuggerType.isSupported()) {
                            debuggerStarter.run(debuggerType);
                        } else {
                            ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
                            Messages.showErrorDialog(
                                    getProject(), "Unsupported debugger",
                                    debuggerType.name() + " debugging is not supported in \"" +
                                            applicationInfo.getVersionName() + " " +
                                            applicationInfo.getFullVersion() + "\".\n" +
                                            "Do you want to use classic debugger over JDBC instead?",
                                    new String[]{"Use " + DBDebuggerType.JDBC.getName(), "Cancel"}, 0,
                                    option -> when(option == 0, () -> debuggerStarter.run(debuggerType)));
                        }
                    }
                });
    }

    private DebuggerSettings getDebuggerSettings() {
        return OperationSettings.getInstance(getProject()).getDebuggerSettings();
    }



    public List<DBSchemaObject> loadCompileDependencies(List<DBMethod> methods, ProgressIndicator progressIndicator) {
        // TODO improve this logic (currently only drilling one level down in the dependencies)
        List<DBSchemaObject> compileList = new ArrayList<>();
        for (DBMethod method : methods) {
            DBProgram program = method.getProgram();
            DBSchemaObject executable = program == null ? method : program;
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(getProject());
            sourceCodeManager.ensureSourcesLoaded(executable, true);

            addToCompileList(compileList, executable);

            for (DBObject object : executable.getReferencedObjects()) {
                if (object instanceof DBSchemaObject && object != executable) {
                    if (!progressIndicator.isCanceled()) {
                        DBSchemaObject schemaObject = (DBSchemaObject) object;
                        boolean added = addToCompileList(compileList, schemaObject);
                        if (added) {
                            progressIndicator.setText("Loading dependencies of " + schemaObject.getQualifiedNameWithType());
                            schemaObject.getReferencedObjects();
                        }
                    }
                }
            }
        }

        compileList.sort(DEPENDENCY_COMPARATOR);
        return compileList;
    }

    private boolean addToCompileList(List<DBSchemaObject> compileList, DBSchemaObject schemaObject) {
        DBSchema schema = schemaObject.getSchema();
        DBObjectStatusHolder objectStatus = schemaObject.getStatus();
        if (!schema.isPublicSchema() && !schema.isSystemSchema() && schemaObject.is(DBObjectProperty.DEBUGABLE) && !objectStatus.is(DBObjectStatus.DEBUG)) {
            if (!compileList.contains(schemaObject)) {
                compileList.add(schemaObject);
            }

            return true;
        }
        return false;
    }

    public List<String> getMissingDebugPrivileges(@NotNull ConnectionHandler connection) {
        List<String> missingPrivileges = new ArrayList<>();
        String userName = connection.getUserName();
        DBObjectBundle objectBundle = connection.getObjectBundle();
        DBUser user = objectBundle.getUser(userName);

        if (user != null) {
            String[] privilegeNames = connection.getInterfaceProvider().getDebuggerInterface().getRequiredPrivilegeNames();

            for (String privilegeName : privilegeNames) {
                DBSystemPrivilege systemPrivilege = objectBundle.getSystemPrivilege(privilegeName);
                if (systemPrivilege == null || !user.hasSystemPrivilege(systemPrivilege))  {
                    missingPrivileges.add(privilegeName);
                }
            }
        }
        return missingPrivileges;

    }

    private static final Comparator<DBSchemaObject> DEPENDENCY_COMPARATOR = (schemaObject1, schemaObject2) -> {
        if (schemaObject1.getReferencedObjects().contains(schemaObject2)) return 1;
        if (schemaObject2.getReferencedObjects().contains(schemaObject1)) return -1;
        return 0;
    };

    public String getDebuggerVersion(@NotNull ConnectionHandler connection) {
        if (DatabaseFeature.DEBUGGING.isSupported(connection)) {
            try {
                return DatabaseInterface.call(true,
                        connection,
                        (provider, conn) -> {
                            DatabaseDebuggerInterface debuggerInterface = provider.getDebuggerInterface();
                            DebuggerVersionInfo debuggerVersion = debuggerInterface.getDebuggerVersion(conn);
                            return debuggerVersion.getVersion();
                        });
            } catch (SQLException e) {
                sendErrorNotification(
                        NotificationGroup.DEBUGGER,
                        "Failed to load debugger version: {0}", e);
            }
        }
        return "Unknown";
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {

    }
}