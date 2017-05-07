package com.dci.intellij.dbn.execution.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.dci.intellij.dbn.common.util.SimpleLazyValue;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionContext;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.ExecutionTarget;
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

public class MethodExecutionInput extends ExecutionInput implements Comparable<MethodExecutionInput> {
    private DBObjectRef<DBMethod> methodRef;
    private DBObjectRef<DBSchema> executionSchema;
    private Set<MethodExecutionArgumentValue> argumentValues = new THashSet<MethodExecutionArgumentValue>();
    private boolean usePoolConnection = true;
    private boolean commitAfterExecution = true;
    private boolean enableLogging = false;

    private transient MethodExecutionResult executionResult;
    private transient List<ArgumentValue> inputArgumentValues = new ArrayList<ArgumentValue>();
    private LazyValue<ExecutionContext> executionContext = new SimpleLazyValue<ExecutionContext>() {
        @Override
        protected ExecutionContext load() {
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
                    return getExecutionSchema();
                }
            };
        }
    };

    public MethodExecutionInput(Project project) {
        super(project, ExecutionTarget.METHOD);
        methodRef = new DBObjectRef<DBMethod>();
        executionSchema = new DBObjectRef<DBSchema>();
    }

    public MethodExecutionInput(Project project, DBMethod method) {
        super(project, ExecutionTarget.METHOD);
        this.methodRef = new DBObjectRef<DBMethod>(method);
        this.executionSchema = method.getSchema().getRef();

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(method)) {
            enableLogging = FailsafeUtil.get(method.getConnectionHandler()).isLoggingEnabled();
        }
    }

    public void initExecution(DBDebuggerType debuggerType) {
        MethodExecutionResultForm resultForm = executionResult == null ? null : executionResult.getForm(false);
        executionResult = new MethodExecutionResult(this, resultForm, debuggerType);
        getExecutionContext().setExecutionTimestamp(System.currentTimeMillis());
    }

    @NotNull
    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext.get();
    }

    @Nullable
    @Override
    public ConnectionHandler getConnectionHandler() {
        DBMethod method = getMethod();
        return method == null ? null : method.getConnectionHandler();
    }


    @Nullable
    public DBMethod getMethod() {
        return methodRef.get();
    }

    public DBObjectRef<DBMethod> getMethodRef() {
        return methodRef;
    }

    public String getConnectionId() {
        return methodRef.getConnectionId();
    }

    public DBSchema getExecutionSchema() {
        return DBObjectRef.get(executionSchema);
    }

    public boolean isObsolete() {
        ConnectionHandler connectionHandler = methodRef.lookupConnectionHandler();
        return connectionHandler == null || getMethod() == null;
    }

    public void setExecutionSchema(DBSchema schema) {
        executionSchema = schema.getRef();
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

    public boolean isUsePoolConnection() {
        return usePoolConnection;
    }

    public void setUsePoolConnection(boolean usePoolConnection) {
        this.usePoolConnection = usePoolConnection;
    }

    public boolean isCommitAfterExecution() {
        return commitAfterExecution;
    }

    public void setCommitAfterExecution(boolean commitAfterExecution) {
        this.commitAfterExecution = commitAfterExecution;
    }

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        super.readConfiguration(element);
        methodRef.readState(element);
        String schemaName = element.getAttributeValue("execution-schema");
        executionSchema = new DBObjectRef<DBSchema>(methodRef.getConnectionId(), DBObjectType.SCHEMA, schemaName);
        usePoolConnection = SettingsUtil.getBooleanAttribute(element, "use-pool-connection", true);
        commitAfterExecution = SettingsUtil.getBooleanAttribute(element, "commit-after-execution", true);
        enableLogging = SettingsUtil.getBooleanAttribute(element, "enable-logging", true);
        Element argumentsElement = element.getChild("argument-list");
        for (Object object : argumentsElement.getChildren()) {
            Element argumentElement = (Element) object;
            MethodExecutionArgumentValue variable = new MethodExecutionArgumentValue(argumentElement);
            argumentValues.add(variable);
        }
    }

    public void writeConfiguration(Element element) {
        super.writeConfiguration(element);
        methodRef.writeState(element);
        element.setAttribute("execution-schema", CommonUtil.nvl(executionSchema.getPath(), ""));
        SettingsUtil.setBooleanAttribute(element, "use-pool-connection", usePoolConnection);
        SettingsUtil.setBooleanAttribute(element, "commit-after-execution", commitAfterExecution);
        SettingsUtil.setBooleanAttribute(element, "enable-logging", enableLogging);

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
        MethodExecutionInput executionInput = new MethodExecutionInput(getProject());
        executionInput.methodRef = methodRef;
        executionInput.executionSchema = executionSchema;
        executionInput.usePoolConnection = usePoolConnection;
        executionInput.commitAfterExecution = commitAfterExecution;
        executionInput.enableLogging = enableLogging;
        executionInput.argumentValues = new THashSet<MethodExecutionArgumentValue>();
        for (MethodExecutionArgumentValue executionVariable : argumentValues) {
            executionInput.argumentValues.add(executionVariable.clone());
        }
        return executionInput;
    }

    public void dispose() {
        super.dispose();
        executionResult = null;
        argumentValues.clear();
        inputArgumentValues.clear();
    }

}
