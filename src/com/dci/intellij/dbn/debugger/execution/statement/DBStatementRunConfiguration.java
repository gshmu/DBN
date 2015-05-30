package com.dci.intellij.dbn.debugger.execution.statement;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.debugger.execution.DBProgramRunConfiguration;
import com.dci.intellij.dbn.execution.statement.StatementExecutionInput;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.object.DBMethod;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DBStatementRunConfiguration extends DBProgramRunConfiguration<DBLanguagePsiFile, StatementExecutionInput> {
    private StatementExecutionInput executionInput;
    private DBStatementRunConfigurationEditor configurationEditor;

    public DBStatementRunConfiguration(Project project, ConfigurationFactory factory, String name, boolean generic) {
        super(project, factory, name, generic);
    }

    @NotNull
    public DBStatementRunConfigurationEditor getConfigurationEditor() {
        if (configurationEditor == null )
            configurationEditor = new DBStatementRunConfigurationEditor(this);
        return configurationEditor;
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBStatementRunProfileState();
    }

    @Nullable
    @Override
    public DBLanguagePsiFile getSource() {
        if (executionInput != null) {
            return executionInput.getExecutionProcessor().getPsiFile();
        }
        return null;
    }

    @Override
    public List<DBMethod> getMethods() {
        return null;
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
/*        if (executionInput == null) {
            throw new RuntimeConfigurationError("No or invalid method selected. The database connection is down, obsolete or method has been dropped.");
        }

        if (executionInput.isObsolete()) {
            throw new RuntimeConfigurationError(
                    "Method " + executionInput.getMethodRef().getQualifiedName() + " could not be resolved. " +
                    "The database connection is down or method has been dropped.");
        }

        ConnectionHandler connectionHandler = getMethod().getConnectionHandler();
        if (!DatabaseFeature.DEBUGGING.isSupported(connectionHandler)){
            throw new RuntimeConfigurationError(
                    "Debugging is not supported for " + connectionHandler.getDatabaseType().getDisplayName() +" databases.");
        }*/
    }

    public boolean isGeneratedName() {
        return false;
    }

    public String suggestedName() {
        return "DBN - Statement Runner";
    }

    public StatementExecutionInput getExecutionInput() {
        return executionInput;
    }

    public void setExecutionInput(StatementExecutionInput executionInput) {
        this.executionInput = executionInput;
    }


    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        SettingsUtil.setBoolean(element, "compile-dependencies", isCompileDependencies());
    }

    @Override
    public boolean excludeCompileBeforeLaunchOption() {
        return true;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        setCompileDependencies(SettingsUtil.getBoolean(element, "compile-dependencies", true));
    }

    @Override
    public RunConfiguration clone() {
        DBStatementRunConfiguration runConfiguration = (DBStatementRunConfiguration) super.clone();
        runConfiguration.configurationEditor = null;
        return runConfiguration;
    }
}
