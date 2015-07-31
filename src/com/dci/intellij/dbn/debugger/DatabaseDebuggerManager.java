package com.dci.intellij.dbn.debugger;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.database.common.debug.DebuggerVersionInfo;
import com.dci.intellij.dbn.debugger.common.breakpoint.DBBreakpointUpdaterFileEditorListener;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBMethodRunConfigType;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigFactory;
import com.dci.intellij.dbn.debugger.common.config.DBRunConfigType;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfig;
import com.dci.intellij.dbn.debugger.common.config.DBStatementRunConfigType;
import com.dci.intellij.dbn.debugger.jdbc.process.DBMethodJdbcRunner;
import com.dci.intellij.dbn.debugger.jdbc.process.DBStatementJdbcRunner;
import com.dci.intellij.dbn.debugger.jdwp.process.DBMethodJdwpRunner;
import com.dci.intellij.dbn.debugger.jdwp.process.DBStatementJdwpRunner;
import com.dci.intellij.dbn.debugger.options.DebuggerSettings;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.execution.statement.processor.StatementExecutionProcessor;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.status.DBObjectStatus;
import com.dci.intellij.dbn.object.common.status.DBObjectStatusHolder;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.UnknownConfigurationType;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;

@State(
    name = "DBNavigator.Project.DebuggerManager",
    storages = {
        @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/dbnavigator.xml", scheme = StorageScheme.DIRECTORY_BASED),
        @Storage(file = StoragePathMacros.PROJECT_FILE)}
)
public class DatabaseDebuggerManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    private Set<ConnectionHandler> activeDebugSessions = new THashSet<ConnectionHandler>();

    private DatabaseDebuggerManager(Project project) {
        super(project);
        FileEditorManager.getInstance(project).addFileEditorManagerListener(new DBBreakpointUpdaterFileEditorListener());
    }

    public void registerDebugSession(ConnectionHandler connectionHandler) {
        activeDebugSessions.add(connectionHandler);
    }

    public void unregisterDebugSession(ConnectionHandler connectionHandler) {
        activeDebugSessions.remove(connectionHandler);
    }

    public boolean checkForbiddenOperation(ConnectionHandler connectionHandler) {
        return checkForbiddenOperation(connectionHandler, null);
    }
    public boolean checkForbiddenOperation(ConnectionHandler connectionHandler, String message) {
        if (activeDebugSessions.contains(connectionHandler)) {
            MessageUtil.showErrorDialog(getProject(), message == null ? "Operation not supported during active debug session." : message);
            return false;
        }
        return true;
    }

    public static DBMethodRunConfigType getMethodConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBMethodRunConfigType.class);
    }

    public static DBStatementRunConfigType getStatementConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBStatementRunConfigType.class);
    }

    public static String createMethodConfigurationName(DBMethod method) {
        DBMethodRunConfigType configurationType = getMethodConfigurationType();
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(method.getProject());
        List<RunnerAndConfigurationSettings> configurationSettings = runManager.getConfigurationSettingsList(configurationType);

        String name = method.getName();
        while (nameExists(configurationSettings, name)) {
            name = NamingUtil.getNextNumberedName(name, true);
        }
        return name;
    }

    private static boolean nameExists(List<RunnerAndConfigurationSettings> configurationSettings, String name) {
        for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            if (configurationSetting.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDebugConsole(VirtualFile virtualFile) {
        if (virtualFile instanceof DBConsoleVirtualFile) {
            DBConsoleVirtualFile consoleVirtualFile = (DBConsoleVirtualFile) virtualFile;
            return consoleVirtualFile.getType() == DBConsoleType.DEBUG;
        }
        return false;
    }

    @Override
    public void initComponent() {
        //createDefaultConfigs();

        // TODO remove this cleanup logic after statement debugger roll-out
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(getProject());
        List<RunnerAndConfigurationSettings> configurationSettingsList = runManager.getConfigurationSettingsList(UnknownConfigurationType.INSTANCE);
        for (RunnerAndConfigurationSettings configurationSettings : configurationSettingsList) {
            runManager.removeConfiguration(configurationSettings);
        }
        super.initComponent();
    }

    @NotNull
    public RunnerAndConfigurationSettings getDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType){
        return FailsafeUtil.get(getDefaultConfig(configurationType, debuggerType, true));
    }

    @Nullable
    public RunnerAndConfigurationSettings getDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType, boolean create){
        Project project = getProject();
        RunManagerEx runManager = (RunManagerEx) RunManagerEx.getInstance(project);
        List<RunnerAndConfigurationSettings> configurationSettings = runManager.getConfigurationSettingsList(configurationType);
        for (RunnerAndConfigurationSettings configurationSetting : configurationSettings) {
            RunConfiguration configuration = configurationSetting.getConfiguration();
            if (configuration instanceof DBRunConfig) {
                DBRunConfig dbRunConfiguration = (DBRunConfig) configuration;
                if (dbRunConfiguration.isGeneric()) {
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
            DBRunConfig runConfiguration = configurationFactory.createConfiguration(project, configurationType.getDefaultRunnerName(), true);
            RunnerAndConfigurationSettings configuration = runManager.createConfiguration(runConfiguration, configurationFactory);
            runManager.addConfiguration(configuration, false);
            //runManager.setTemporaryConfiguration(configuration);
            return configuration;
        }
        return defaultRunnerConfig;
    }

    public void startMethodDebugger(DBMethod method) {
        Project project = getProject();
        DebuggerSettings debuggerSettings = OperationSettings.getInstance(project).getDebuggerSettings();
        DBDebuggerType debuggerType = debuggerSettings.getDebuggerType();

        DBMethodRunConfigType configurationType = getMethodConfigurationType();

        RunnerAndConfigurationSettings runConfigurationSetting;
        if (true) {
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
                if (method.equals(availableRunConfiguration.getMethod())) {
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

        ProgramRunner programRunner = RunnerRegistry.getInstance().findRunnerById(runnerId);
        if (programRunner != null) {
            try {
                Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
                if (executorInstance == null) {
                    throw new ExecutionException("Could not resolve debug executor");
                }

                ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executorInstance, programRunner, runConfigurationSetting, project);
                programRunner.execute(executionEnvironment);
            } catch (ExecutionException e) {
                MessageUtil.showErrorDialog(
                        project, "Could not start debugger for " + method.getQualifiedName() + ". \n" +
                                "Reason: " + e.getMessage());
            }
        }
    }

    public void startStatementDebugger(@NotNull StatementExecutionProcessor executionProcessor) {
        Project project = getProject();
        DebuggerSettings debuggerSettings = OperationSettings.getInstance(project).getDebuggerSettings();
        DBDebuggerType debuggerType = debuggerSettings.getDebuggerType();

        DBStatementRunConfigType configurationType = getStatementConfigurationType();
        RunnerAndConfigurationSettings runConfigurationSetting;
        runConfigurationSetting = getDefaultConfig(configurationType, debuggerType);
        DBStatementRunConfig runConfiguration = (DBStatementRunConfig) runConfigurationSetting.getConfiguration();

        runConfiguration.setExecutionInput(executionProcessor.getExecutionInput());

        String runnerId =
                debuggerType == DBDebuggerType.JDBC ? DBStatementJdbcRunner.RUNNER_ID :
                debuggerType == DBDebuggerType.JDWP ? DBStatementJdwpRunner.RUNNER_ID : null;

        ProgramRunner programRunner = RunnerRegistry.getInstance().findRunnerById(runnerId);
        if (programRunner != null) {
            try {
                Executor executorInstance = DefaultDebugExecutor.getDebugExecutorInstance();
                if (executorInstance == null) {
                    throw new ExecutionException("Could not resolve debug executor");
                }

                ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(executorInstance, programRunner, runConfigurationSetting, project);
                programRunner.execute(executionEnvironment);
            } catch (ExecutionException e) {
                MessageUtil.showErrorDialog(
                        project, "Could not start statement debugger. \n" +
                                "Reason: " + e.getMessage());
            }
        }
    }



    public List<DBSchemaObject> loadCompileDependencies(List<DBMethod> methods, ProgressIndicator progressIndicator) {
        // TODO improve this logic (currently only drilling one level down in the dependencies)
        List<DBSchemaObject> compileList = new ArrayList<DBSchemaObject>();
        for (DBMethod method : methods) {
            DBSchemaObject executable = method.getProgram() == null ? method : method.getProgram();
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

        Collections.sort(compileList, DEPENDENCY_COMPARATOR);
        return compileList;
    }

    private boolean addToCompileList(List<DBSchemaObject> compileList, DBSchemaObject schemaObject) {
        DBSchema schema = schemaObject.getSchema();
        DBObjectStatusHolder status = schemaObject.getStatus();
        if (!schema.isPublicSchema() && !schema.isSystemSchema() && status.has(DBObjectStatus.DEBUG) && !status.is(DBObjectStatus.DEBUG)) {
            compileList.add(schemaObject);
            return true;
        }
        return false;
    }

    public List<String> getMissingDebugPrivileges(@NotNull ConnectionHandler connectionHandler) {
        List<String> missingPrivileges = new ArrayList<String>();
        String userName = connectionHandler.getUserName();
        DBUser user = connectionHandler.getObjectBundle().getUser(userName);

        if (user != null) {
            String[] privilegeNames = connectionHandler.getInterfaceProvider().getDebuggerInterface().getRequiredPrivilegeNames();

            for (String privilegeName : privilegeNames) {
                DBSystemPrivilege systemPrivilege = connectionHandler.getObjectBundle().getSystemPrivilege(privilegeName);
                if (systemPrivilege == null || !user.hasSystemPrivilege(systemPrivilege))  {
                    missingPrivileges.add(privilegeName);
                }
            }
        }
        return missingPrivileges;
    }

    private static final Comparator<DBSchemaObject> DEPENDENCY_COMPARATOR = new Comparator<DBSchemaObject>() {
        public int compare(DBSchemaObject schemaObject1, DBSchemaObject schemaObject2) {
            if (schemaObject1.getReferencedObjects().contains(schemaObject2)) return 1;
            if (schemaObject2.getReferencedObjects().contains(schemaObject1)) return -1;
            return 0;
        }
    };

    public String getDebuggerVersion(ConnectionHandler connectionHandler) {

        if (connectionHandler != null) {
            DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
            Connection connection = null;
            try {
                connection = connectionHandler.getPoolConnection();
                DebuggerVersionInfo debuggerVersion = debuggerInterface.getDebuggerVersion(connection);
                return debuggerVersion.getVersion();
            } catch (Exception e) {

            } finally {
                connectionHandler.freePoolConnection(connection);
            }
        }
        return "Unknown";
    }


    /***************************************
     *            ProjectComponent         *
     ***************************************/
    public static DatabaseDebuggerManager getInstance(@NotNull Project project) {
        return FailsafeUtil.getComponent(project, DatabaseDebuggerManager.class);
    }

    @NonNls
    @NotNull
    public String getComponentName() {
        return "DBNavigator.Project.DebuggerManager";
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
    public void loadState(Element element) {

    }
}