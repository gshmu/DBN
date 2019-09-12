package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.util.Cloneable;
import com.dci.intellij.dbn.common.util.Safe;
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
import com.intellij.openapi.project.Project;
import gnu.trove.THashSet;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Nullifiable
public class MethodExecutionInput extends LocalExecutionInput implements Comparable<MethodExecutionInput>, Cloneable<MethodExecutionInput> {
    private DBObjectRef<DBMethod> methodRef;
    private Set<MethodExecutionArgumentValue> argumentValues = new THashSet<>();

    private MethodExecutionResult executionResult;
    private List<ArgumentValue> inputArgumentValues = new ArrayList<>();

    public MethodExecutionInput(Project project) {
        super(project, ExecutionTarget.METHOD);
        methodRef = new DBObjectRef<>();

        ExecutionOptions options = getOptions();
        options.set(ExecutionOption.COMMIT_AFTER_EXECUTION, true);
        //setSessionId(SessionId.POOL);
    }

    public MethodExecutionInput(Project project, DBMethod method) {
        super(project, ExecutionTarget.METHOD);
        this.methodRef = DBObjectRef.from(method);
        this.targetSchemaId = method.getSchemaIdentifier();

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
                return methodRef.objectType.getName() + " " + methodRef.objectName;
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
        return method == null ? methodRef == null ? null : methodRef.getConnectionHandler() : method.getConnectionHandler();
    }

    @Override
    public ConnectionId getConnectionHandlerId() {
        return methodRef.getConnectionId();
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
        return DBObjectRef.get(methodRef);
    }

    public DBObjectRef<DBMethod> getMethodRef() {
        return methodRef;
    }

    public ConnectionId getConnectionId() {
        return methodRef.getConnectionId();
    }

    public boolean isObsolete() {
        ConnectionHandler connectionHandler = methodRef.resolveConnectionHandler();
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

    public List<ArgumentValue> getInputArgumentValues() {
        return inputArgumentValues;
    }

    private ArgumentValue getArgumentValue(@NotNull DBArgument argument) {
        for (ArgumentValue argumentValue : inputArgumentValues) {
            if (Safe.equal(argument, argumentValue.getArgument())) {
                return argumentValue;
            }
        }
        ArgumentValue argumentValue = new ArgumentValue(argument, null);
        argumentValue.setValueHolder(getExecutionVariable(argumentValue.getName()));
        inputArgumentValues.add(argumentValue);
        return argumentValue;
    }

    private ArgumentValue getArgumentValue(DBArgument argument, DBTypeAttribute attribute) {
        for (ArgumentValue argumentValue : inputArgumentValues) {
            if (Safe.equal(argumentValue.getArgument(), argument) &&
                    Safe.equal(argumentValue.getAttribute(), attribute)) {
                return argumentValue;
            }
        }

        ArgumentValue argumentValue = new ArgumentValue(argument, attribute, null);
        argumentValue.setValueHolder(getExecutionVariable(argumentValue.getName()));
        inputArgumentValues.add(argumentValue);
        return argumentValue;
    }

    private synchronized MethodExecutionArgumentValue getExecutionVariable(String name) {
        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            if (executionVariable.getName().equalsIgnoreCase(name)) {
                return executionVariable;
            }
        }
        MethodExecutionArgumentValue executionVariable = new MethodExecutionArgumentValue(name);
        argumentValues.add(executionVariable);
        return executionVariable;
    }

    public Set<MethodExecutionArgumentValue> getArgumentValues() {
        return argumentValues;
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
        methodRef.readState(element);
        targetSchemaId = SchemaId.get(element.getAttributeValue("execution-schema"));;
        Element argumentsElement = element.getChild("argument-actions");
        if (argumentsElement != null) {
            for (Object object : argumentsElement.getChildren()) {
                Element argumentElement = (Element) object;
                MethodExecutionArgumentValue variable = new MethodExecutionArgumentValue(argumentElement);
                argumentValues.add(variable);
            }
        }
    }

    @Override
    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        methodRef.writeState(element);
        element.setAttribute("execution-schema", targetSchemaId == null ? "" : targetSchemaId.id());

        Element argumentsElement = new Element("argument-actions");
        element.addContent(argumentsElement);

        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            Element argumentElement = new Element("argument");
            executionVariable.writeState(argumentElement);
            argumentsElement.addContent(argumentElement);
        }
    }

    @Override
    public int compareTo(@NotNull MethodExecutionInput executionInput) {
        DBObjectRef<DBMethod> localMethod = methodRef;
        DBObjectRef<DBMethod> remoteMethod = executionInput.methodRef;
        return localMethod.compareTo(remoteMethod);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodExecutionInput) {
            MethodExecutionInput executionInput = (MethodExecutionInput) obj;
            return methodRef.equals(executionInput.methodRef);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return methodRef.hashCode();
    }

    @Override
    public MethodExecutionInput clone() {
        MethodExecutionInput clone = new MethodExecutionInput(getProject());
        clone.methodRef = methodRef;
        clone.targetSchemaId = targetSchemaId;
        clone.setOptions(getOptions().clone());
        clone.argumentValues = new THashSet<>();
        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            clone.argumentValues.add(executionVariable.clone());
        }
        return clone;
    }
}
