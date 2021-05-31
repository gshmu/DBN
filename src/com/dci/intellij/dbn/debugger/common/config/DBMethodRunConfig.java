package com.dci.intellij.dbn.debugger.common.config;

import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.execution.method.MethodExecutionManager;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class DBMethodRunConfig extends DBRunConfig<MethodExecutionInput> implements Cloneable<DBMethodRunConfig> {
    private Set<MethodExecutionInput> methodSelectionHistory = new THashSet<>();

    public DBMethodRunConfig(Project project, DBMethodRunConfigFactory factory, String name,DBRunConfigCategory category) {
        super(project, factory, name, category);
    }

    public Set<MethodExecutionInput> getMethodSelectionHistory() {
        return methodSelectionHistory;
    }

    @Override
    public void setExecutionInput(MethodExecutionInput executionInput) {
        MethodExecutionInput currentExecutionInput = getExecutionInput();
        if (currentExecutionInput != null && !currentExecutionInput.equals(executionInput)) {
            methodSelectionHistory.add(currentExecutionInput);
        }
        super.setExecutionInput(executionInput);
    }

    @Override
    public MethodExecutionInput getExecutionInput() {
        return super.getExecutionInput();
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (getCategory() == DBRunConfigCategory.CUSTOM) {
            MethodExecutionInput executionInput = getExecutionInput();
            if (executionInput == null) {
                throw new RuntimeConfigurationError("No or invalid method selected. The database connection is down, obsolete or method has been dropped.");
            }

            if (executionInput.isObsolete()) {
                throw new RuntimeConfigurationError(
                        "Method " + executionInput.getMethodRef().getQualifiedName() + " could not be resolved. " +
                                "The database connection is down or method has been dropped.");
            }

            DBMethod method = getMethod();
            if (method != null) {
                ConnectionHandler connectionHandler = method.getConnectionHandler();
                if (!DatabaseFeature.DEBUGGING.isSupported(connectionHandler)){
                    throw new RuntimeConfigurationError(
                            "Debugging is not supported for " + connectionHandler.getDatabaseType().getName() +" databases.");
                }
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
        ArrayList<DBMethod> methods = new ArrayList<>();
        DBMethod method = getMethod();
        if (method != null) {
            methods.add(method);
        }
        return methods;
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        MethodExecutionInput executionInput = getExecutionInput();
        if (executionInput != null && getCategory() == DBRunConfigCategory.CUSTOM) {
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
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        MethodExecutionManager executionManager = MethodExecutionManager.getInstance(getProject());
        if (getCategory() == DBRunConfigCategory.CUSTOM) {
            Element methodIdentifierElement = element.getChild("method-identifier");
            if (methodIdentifierElement != null) {
                DBObjectRef<DBMethod> methodRef = new DBObjectRef<>();
                methodRef.readState(methodIdentifierElement);

                MethodExecutionInput executionInput = executionManager.getExecutionInput(methodRef);
                setExecutionInput(executionInput);
            }

            Element methodIdentifierHistoryElement = element.getChild("method-identifier-history");
            if (methodIdentifierHistoryElement != null) {
                for (Object o : methodIdentifierHistoryElement.getChildren()) {
                    methodIdentifierElement = (Element) o;
                    DBObjectRef<DBMethod> methodRef = new DBObjectRef<>();
                    methodRef.readState(methodIdentifierElement);

                    MethodExecutionInput executionInput = executionManager.getExecutionInput(methodRef);
                    methodSelectionHistory.add(executionInput);
                }
            }
        }
    }

    @Override
    public DBMethodRunConfig clone() {
        DBMethodRunConfig runConfiguration = (DBMethodRunConfig) super.clone();
        MethodExecutionInput executionInput = getExecutionInput();
        runConfiguration.setExecutionInput(executionInput == null ? null : executionInput.clone());
        runConfiguration.methodSelectionHistory = new HashSet<>(getMethodSelectionHistory());
        return runConfiguration;
    }

    @Override
    public String suggestedName() {
        if (getCategory() == DBRunConfigCategory.CUSTOM) {
            MethodExecutionInput executionInput = getExecutionInput();
            if (executionInput != null) {
                setGeneratedName(true);
                String runnerName = executionInput.getMethodRef().objectName;
                if (getDebuggerType() == DBDebuggerType.JDWP) {
                    runnerName = runnerName + " (JDWP)";
                }
                return runnerName;
            }
        } else {
            String defaultRunnerName = getType().getDefaultRunnerName();
            if (getDebuggerType() == DBDebuggerType.JDWP) {
                defaultRunnerName = defaultRunnerName + " (JDWP)";
            }
            return defaultRunnerName;
        }
        return null;
    }

    @Override
    public Icon getIcon() {
        if (getCategory() == DBRunConfigCategory.CUSTOM) {
            MethodExecutionInput executionInput = getExecutionInput();
            if (executionInput != null) {
                DBMethod method = executionInput.getMethod();
                if (method != null) {
                    return method.getIcon();
                }
            }

        }
        return super.getIcon();
    }
}
