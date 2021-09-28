package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

public class MethodExecutionInput extends LocalExecutionInput implements Comparable<MethodExecutionInput>, Cloneable<MethodExecutionInput> {
    private DBObjectRef<DBMethod> method;

    private transient MethodExecutionResult executionResult;
    private final List<ArgumentValue> argumentValues = new ArrayList<>();
    private Map<String, MethodExecutionArgumentValue> argumentValueHistory = new HashMap<>();

    public MethodExecutionInput(Project project) {
        super(project, ExecutionTarget.METHOD);
        method = new DBObjectRef<>();

        ExecutionOptions options = getOptions();
        options.set(ExecutionOption.COMMIT_AFTER_EXECUTION, true);
        //setSessionId(SessionId.POOL);
    }

    public MethodExecutionInput(Project project, DBObjectRef<DBMethod> method) {
        super(project, ExecutionTarget.METHOD);
        this.method = method;
        DBObjectRef<?> schema = method.getParentRef(DBObjectType.SCHEMA);

        if (schema != null) {
            this.targetSchemaId = SchemaId.get(schema.getObjectName());
        }


        if (DatabaseFeature.DATABASE_LOGGING.isSupported(method)) {
            ConnectionHandler connectionHandler = Failsafe.nn(method.getConnectionHandler());
            getOptions().set(ExecutionOption.ENABLE_LOGGING, connectionHandler.isLoggingEnabled());
        }
    }

    public ExecutionContext initExecution(DBDebuggerType debuggerType) {
        MethodExecutionResult executionResult = new MethodExecutionResult(this, debuggerType);
        executionResult.setPrevious(this.executionResult);
        this.executionResult = executionResult;
        return initExecutionContext();
    }

    @Override
    protected ExecutionContext createExecutionContext() {
        return new ExecutionContext() {
            @NotNull
            @Override
            public String getTargetName() {
                return method.getObjectType().getName() + " " + method.getObjectName();
            }

            @Nullable
            @Override
            public ConnectionHandler getTargetConnection() {
                return getConnectionHandler();
            }

            @Nullable
            @Override
            public SchemaId getTargetSchema() {
                return MethodExecutionInput.this.getTargetSchemaId();
            }
        };
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        DBMethod method = getMethod();
        return method == null ? this.method == null ? null : this.method.getConnectionHandler() : method.getConnectionHandler();
    }

    @Override
    public ConnectionId getConnectionHandlerId() {
        return method.getConnectionId();
    }

    @Override
    public boolean hasExecutionVariables() {
        return false;
    }

    @Override
    public boolean isSchemaSelectionAllowed() {
        return DatabaseFeature.AUTHID_METHOD_EXECUTION.isSupported(getConnectionHandler());
    }

    @Override
    public boolean isSessionSelectionAllowed() {
        return true;
    }

    @Override
    public boolean isDatabaseLogProducer() {
        return true;
    }

    @Nullable
    public DBMethod getMethod() {
        return DBObjectRef.get(method);
    }

    public DBObjectRef<DBMethod> getMethodRef() {
        return method;
    }

    public ConnectionId getConnectionId() {
        return method.getConnectionId();
    }

    public boolean isObsolete() {
        ConnectionHandler connectionHandler = method.resolveConnectionHandler();
        return connectionHandler == null/* || getMethod() == null*/;
    }

    public boolean isInactive() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler != null && !connectionHandler.getSettings().isActive();
    }

    public void setInputValue(@NotNull DBArgument argument, DBTypeAttribute typeAttribute, String value) {
        ArgumentValue argumentValue = getArgumentValue(argument, typeAttribute);
        argumentValue.setValue(value);
    }

    public void setInputValue(@NotNull DBArgument argument, String value) {
        ArgumentValue argumentValue = getArgumentValue(argument);
        argumentValue.setValue(value);
    }

    public String getInputValue(@NotNull DBArgument argument) {
        ArgumentValue argumentValue = getArgumentValue(argument);
        return (String) argumentValue.getValue();
    }

    public List<String> getInputValueHistory(@NotNull DBArgument argument, @Nullable DBTypeAttribute typeAttribute) {
        ArgumentValue argumentValue =
                typeAttribute == null ?
                        getArgumentValue(argument) :
                        getArgumentValue(argument, typeAttribute);

        ArgumentValueHolder valueStore = argumentValue.getValueHolder();
        if (valueStore instanceof MethodExecutionArgumentValue) {
            MethodExecutionArgumentValue executionVariable = (MethodExecutionArgumentValue) valueStore;
            return executionVariable.getValueHistory();
        }
        return Collections.emptyList();
    }

    public String getInputValue(DBArgument argument, DBTypeAttribute typeAttribute) {
        ArgumentValue argumentValue = getArgumentValue(argument, typeAttribute);
        return (String) argumentValue.getValue();
    }

    public List<ArgumentValue> getArgumentValues() {
        return argumentValues;
    }

    private ArgumentValue getArgumentValue(@NotNull DBArgument argument) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (Safe.equal(argument, argumentValue.getArgument())) {
                return argumentValue;
            }
        }
        ArgumentValue argumentValue = new ArgumentValue(argument, null);
        argumentValue.setValueHolder(getExecutionVariable(argumentValue.getName()));
        argumentValues.add(argumentValue);
        return argumentValue;
    }

    private ArgumentValue getArgumentValue(DBArgument argument, DBTypeAttribute attribute) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (Safe.equal(argumentValue.getArgument(), argument) &&
                    Safe.equal(argumentValue.getAttribute(), attribute)) {
                return argumentValue;
            }
        }

        ArgumentValue argumentValue = new ArgumentValue(argument, attribute, null);
        argumentValue.setValueHolder(getExecutionVariable(argumentValue.getName()));
        argumentValues.add(argumentValue);
        return argumentValue;
    }

    private MethodExecutionArgumentValue getExecutionVariable(String name) {
        for (MethodExecutionArgumentValue executionVariable : argumentValueHistory.values()) {
            if (StringUtil.equalsIgnoreCase(executionVariable.getName(), name)) {
                return executionVariable;
            }
        }
        MethodExecutionArgumentValue executionVariable = new MethodExecutionArgumentValue(name);
        argumentValueHistory.put(executionVariable.getName(), executionVariable);
        return executionVariable;
    }

    public Map<String, MethodExecutionArgumentValue> getArgumentValueHistory() {
        return argumentValueHistory;
    }

    @Nullable
    public MethodExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(MethodExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    @Override
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        method.readState(element);
        targetSchemaId = SchemaId.get(stringAttribute(element, "execution-schema"));;
        Element argumentsElement = element.getChild("argument-actions");
        if (argumentsElement != null) {
            for (Element valueElement : argumentsElement.getChildren()) {
                MethodExecutionArgumentValue argumentValue = new MethodExecutionArgumentValue(valueElement);
                argumentValueHistory.put(argumentValue.getName(), argumentValue);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        method.writeState(element);
        element.setAttribute("execution-schema", targetSchemaId == null ? "" : targetSchemaId.id());

        Element argumentsElement = new Element("argument-actions");
        element.addContent(argumentsElement);

        for (MethodExecutionArgumentValue executionVariable : argumentValueHistory.values()) {
            Element argumentElement = new Element("argument");
            executionVariable.writeState(argumentElement);
            argumentsElement.addContent(argumentElement);
        }
    }

    @Override
    public int compareTo(@NotNull MethodExecutionInput executionInput) {
        DBObjectRef<DBMethod> localMethod = method;
        DBObjectRef<DBMethod> remoteMethod = executionInput.method;
        return localMethod.compareTo(remoteMethod);
    }

    @Override
    public MethodExecutionInput clone() {
        MethodExecutionInput clone = new MethodExecutionInput(getProject());
        clone.method = method;
        clone.targetSchemaId = targetSchemaId;
        clone.setOptions(ExecutionOptions.clone(getOptions()));
        clone.argumentValueHistory = new HashMap<>();
        for (MethodExecutionArgumentValue executionVariable : argumentValueHistory.values()) {
            clone.argumentValueHistory.put(
                    executionVariable.getName(),
                    executionVariable.clone());
        }
        return clone;
    }
}
