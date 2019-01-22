package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.execution.ExecutionTarget;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
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

public class MethodExecutionInput extends LocalExecutionInput implements Comparable<MethodExecutionInput> {
    private DBObjectRef<DBMethod> methodRef;
    private Set<MethodExecutionArgumentValue> argumentValues = new THashSet<MethodExecutionArgumentValue>();

    private transient MethodExecutionResult executionResult;
    private transient List<ArgumentValue> inputArgumentValues = new ArrayList<ArgumentValue>();

    public MethodExecutionInput(Project project) {
        super(project, ExecutionTarget.METHOD);
        methodRef = new DBObjectRef<DBMethod>();
        targetSchemaRef = new DBObjectRef<DBSchema>();

        ExecutionOptions options = getOptions();
        options.set(ExecutionOption.COMMIT_AFTER_EXECUTION, true);
        //setSessionId(SessionId.POOL);
    }

    public MethodExecutionInput(Project project, DBMethod method) {
        super(project, ExecutionTarget.METHOD);
        this.methodRef = DBObjectRef.from(method);
        this.targetSchemaRef = method.getSchema().getRef();

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(method)) {
            ConnectionHandler connectionHandler = FailsafeUtil.get(method.getConnectionHandler());
            getOptions().set(ExecutionOption.ENABLE_LOGGING, connectionHandler.isLoggingEnabled());
        }
    }

    public void initExecution(DBDebuggerType debuggerType) {
        MethodExecutionResultForm resultForm = executionResult == null ? null : executionResult.getForm(false);
        executionResult = new MethodExecutionResult(this, resultForm, debuggerType);
        getExecutionContext().setExecutionTimestamp(System.currentTimeMillis());
    }

    @Override
    protected ExecutionContext createExecutionContext() {
        return new ExecutionContext() {
            @NotNull
            @Override
            public String getTargetName() {
                return methodRef.getObjectType().getName() + " " + methodRef.getObjectName();
            }

            @Nullable
            @Override
            public ConnectionHandler getTargetConnection() {
                return getConnectionHandler();
            }

            @Nullable
            @Override
            public DBSchema getTargetSchema() {
                return MethodExecutionInput.this.getTargetSchema();
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
        ConnectionHandler connectionHandler = methodRef.lookupConnectionHandler();
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
            if (CommonUtil.safeEqual(argument, argumentValue.getArgument())) {
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
            if (CommonUtil.safeEqual(argumentValue.getArgument(), argument) &&
                    CommonUtil.safeEqual(argumentValue.getAttribute(), attribute)) {
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

    public MethodExecutionResult getExecutionResult() {
        return executionResult;
    }

    public void setExecutionResult(MethodExecutionResult executionResult) {
        this.executionResult = executionResult;
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        methodRef.readState(element);
        String schemaName = element.getAttributeValue("execution-schema");
        SessionId sessionId = CommonUtil.nvl(SessionId.get(element.getAttributeValue("session-id")), SessionId.MAIN);
        targetSchemaRef = new DBObjectRef<DBSchema>(methodRef.getConnectionId(), DBObjectType.SCHEMA, schemaName);
        Element argumentsElement = element.getChild("argument-list");
        if (argumentsElement != null) {
            for (Object object : argumentsElement.getChildren()) {
                Element argumentElement = (Element) object;
                MethodExecutionArgumentValue variable = new MethodExecutionArgumentValue(argumentElement);
                argumentValues.add(variable);
            }
        }
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        methodRef.writeState(element);
        element.setAttribute("execution-schema", CommonUtil.nvl(targetSchemaRef.getPath(), ""));

        Element argumentsElement = new Element("argument-list");
        element.addContent(argumentsElement);

        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            Element argumentElement = new Element("argument");
            executionVariable.writeState(argumentElement);
            argumentsElement.addContent(argumentElement);
        }
    }

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

    public MethodExecutionInput clone() {
        MethodExecutionInput clone = new MethodExecutionInput(getProject());
        clone.methodRef = methodRef;
        clone.targetSchemaRef = targetSchemaRef;
        clone.setOptions(getOptions().clone());
        clone.argumentValues = new THashSet<MethodExecutionArgumentValue>();
        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            clone.argumentValues.add(executionVariable.clone());
        }
        return clone;
    }

    public void dispose() {
        super.dispose();
        executionResult = null;
        argumentValues.clear();
        inputArgumentValues.clear();
    }

}
