package com.dci.intellij.dbn.debugger.jdbc.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.config.DBProgramRunConfiguration;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import gnu.trove.THashSet;

public class DBMethodRunConfig extends DBProgramRunConfiguration<MethodExecutionInput> {
    private Set<MethodExecutionInput> methodSelectionHistory = new THashSet<MethodExecutionInput>();
    private DBMethodRunConfigEditor configurationEditor;
    private boolean isGeneratedName = true;

    public DBMethodRunConfig(Project project, ConfigurationFactory factory, String name, boolean generic) {
        super(project, factory, name, generic);
    }

    @NotNull
    public DBMethodRunConfigEditor getConfigurationEditor() {
        if (configurationEditor == null )
            configurationEditor = new DBMethodRunConfigEditor(this);
        return configurationEditor;
    }

    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new DBMethodRunProfileState(env);
    }

    public Set<MethodExecutionInput> getMethodSelectionHistory() {
        return methodSelectionHistory;
    }

    public void checkConfiguration() throws RuntimeConfigurationException {
        if (!isGeneric()) {
            MethodExecutionInput executionInput = getExecutionInput();
            if (executionInput == null) {
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
            }
        }
    }

    @Nullable
    @Override
    public DBMethod getSource() {
        return getMethod();
    }

    @Nullable
    public DBMethod getMethod() {
        MethodExecutionInput executionInput = getExecutionInput();
        return executionInput == null ? null : executionInput.getMethod();
    }

    @Override
    public List<DBMethod> getMethods() {
        ArrayList<DBMethod> methods = new ArrayList<DBMethod>();
        DBMethod method = getMethod();
        if (method != null) {
            methods.add(method);
        }
        return methods;
    }

    public boolean isGeneratedName() {
        return isGeneratedName;
    }

    public String suggestedName() {
        return createSuggestedName();
    }

    public String createSuggestedName() {
        MethodExecutionInput executionInput = getExecutionInput();
        if (executionInput == null) {
            return "<unnamed>";
        } else {
            return executionInput.getMethodRef().getObjectName();
        }
    }

    public void setExecutionInput(MethodExecutionInput executionInput) {
        MethodExecutionInput currentExecutionInput = getExecutionInput();
        if (currentExecutionInput != null && !currentExecutionInput.equals(executionInput)) {
            methodSelectionHistory.add(currentExecutionInput);
        }
        super.setExecutionInput(executionInput);
        getConfigurationEditor().setExecutionInput(executionInput);
    }


    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        SettingsUtil.setBoolean(element, "compile-dependencies", isCompileDependencies());
        MethodExecutionInput executionInput = getExecutionInput();
        if (executionInput != null) {
            Element methodIdentifierElement = new Element("method-identifier");
            executionInput.getMethodRef().writeState(methodIdentifierElement);
            element.addContent(methodIdentifierElement);

            Element methodIdentifierHistoryElement = new Element("method-identifier-history");
            for (MethodExecutionInput histExecutionInput : methodSelectionHistory) {
                methodIdentifierElement = new Element("method-identifier");
                histExecutionInput.getMethodRef().writeState(methodIdentifierElement);
                methodIdentifierHistoryElement.addContent(methodIdentifierElement);
            }
            element.addContent(methodIdentifierHistoryElement);
        }
    }

    @Override
    public boolean excludeCompileBeforeLaunchOption() {
        return true;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        setCompileDependencies(SettingsUtil.getBoolean(element, "compile-dependencies", true));
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(getProject());
        Element methodIdentifierElement = element.getChild("method-identifier");
        if (methodIdentifierElement != null) {
            DBObjectRef<DBMethod> methodRef = new DBObjectRef<DBMethod>();
            methodRef.readState(methodIdentifierElement);

            MethodExecutionInput executionInput = executionManager.getExecutionInput(methodRef);
            setExecutionInput(executionInput);
        }

        Element methodIdentifierHistoryElement = element.getChild("method-identifier-history");
        if (methodIdentifierHistoryElement != null) {
            for (Object o : methodIdentifierHistoryElement.getChildren()) {
                methodIdentifierElement = (Element) o;
                DBObjectRef<DBMethod> methodRef = new DBObjectRef<DBMethod>();
                methodRef.readState(methodIdentifierElement);

                MethodExecutionInput executionInput = executionManager.getExecutionInput(methodRef);
                methodSelectionHistory.add(executionInput);
            }
        }
    }

    @Override
    public RunConfiguration clone() {
        DBMethodRunConfig runConfiguration = (DBMethodRunConfig) super.clone();
        runConfiguration.configurationEditor = null;
        MethodExecutionInput executionInput = getExecutionInput();
        runConfiguration.setExecutionInput(executionInput == null ? null : executionInput.clone());
        runConfiguration.methodSelectionHistory = new HashSet<MethodExecutionInput>(methodSelectionHistory);
        return runConfiguration;
    }
}
