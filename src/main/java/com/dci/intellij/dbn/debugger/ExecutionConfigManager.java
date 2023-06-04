package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.debugger.common.config.*;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static com.dci.intellij.dbn.common.component.Components.projectService;
import static com.dci.intellij.dbn.debugger.ExecutionConfigManager.COMPONENT_NAME;

@State(
        name = COMPONENT_NAME,
        storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class ExecutionConfigManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.ExecutionConfigManager";


    public static final String GENERIC_METHOD_RUNNER_HINT =
            "This is the generic Database Method debug runner. " +
                    "This is used when debugging is invoked on a given method. " +
                    "No specific method information can be specified here.";

    public static final String GENERIC_STATEMENT_RUNNER_HINT =
            "This is the generic Database Statement debug runner. " +
                    "This is used when debugging is invoked on a given SQL statement. " +
                    "No specific statement information can be specified here.";


    private ExecutionConfigManager(Project project) {
        super(project, COMPONENT_NAME);
    }


    public static ExecutionConfigManager getInstance(@NotNull Project project) {
        return projectService(project, ExecutionConfigManager.class);
    }

    public DBMethodRunConfigType getMethodConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBMethodRunConfigType.class);
    }

    public DBStatementRunConfigType getStatementConfigurationType() {
        ConfigurationType[] configurationTypes = Extensions.getExtensions(ConfigurationType.CONFIGURATION_TYPE_EP);
        return ContainerUtil.findInstance(configurationTypes, DBStatementRunConfigType.class);
    }

    public String createMethodConfigurationName(DBMethod method) {
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

    @NotNull
    public RunnerAndConfigurationSettings getDefaultConfig(DBRunConfigType configurationType, DBDebuggerType debuggerType){
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

    public void removeRunConfigurations() {
        RunManager runManager = RunManagerEx.getInstance(getProject());
        List<RunnerAndConfigurationSettings> runConfigurations = runManager.getAllSettings();
        for (RunnerAndConfigurationSettings runConfiguration : runConfigurations) {
            RunConfiguration configuration = runConfiguration.getConfiguration();
            if (configuration instanceof DBRunConfig) {
                runManager.removeConfiguration(runConfiguration);
            }
        }
    }

    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element state) {

    }
}
