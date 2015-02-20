package com.dci.intellij.dbn.execution.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

public class MethodExecutionInput implements Disposable, PersistentConfiguration, Comparable<MethodExecutionInput>, ConnectionProvider {
    private DBMethodRef<DBMethod> methodRef;
    private DBObjectRef<DBSchema> executionSchema;
    private Map<String, String> valuesMap = new HashMap<String, String>();
    private boolean usePoolConnection = true;
    private boolean commitAfterExecution = true;
    private boolean enableLogging = false;
    private boolean isExecuting = false;


    private transient MethodExecutionResult executionResult;
    private transient List<ArgumentValue> argumentValues = new ArrayList<ArgumentValue>();

    private transient boolean executionCancelled;

    public MethodExecutionInput() {
        methodRef = new DBMethodRef<DBMethod>();
        executionSchema = new DBObjectRef<DBSchema>();
    }

    public MethodExecutionInput(DBMethod method) {
        this.methodRef = new DBMethodRef<DBMethod>(method);
        this.executionSchema = method.getSchema().getRef();

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(method)) {
            enableLogging = method.getConnectionHandler().isLoggingEnabled();
        }
    }

    public void initExecutionResult(boolean debug) {
        MethodExecutionResultForm resultForm = executionResult == null ? null : executionResult.getForm(false);
        executionResult = new MethodExecutionResult(this, resultForm, debug);
    }

    @Nullable
    public DBMethod getMethod() {
        return methodRef.get();
    }

    public DBMethodRef getMethodRef() {
        return methodRef;
    }

    public ConnectionHandler getConnectionHandler() {
        return methodRef.lookupConnectionHandler();
    }

    public DBSchema getExecutionSchema() {
        return executionSchema.get();
    }

    public boolean isObsolete() {
        return getConnectionHandler() == null || getMethod() == null;
    }

    public boolean isExecutionCancelled() {
        return executionCancelled;
    }

    public void setExecutionCancelled(boolean executionCancelled) {
        this.executionCancelled = executionCancelled;
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

    public String getInputValue(DBArgument argument, DBTypeAttribute typeAttribute) {
        ArgumentValue argumentValue = getArgumentValue(argument, typeAttribute);
        return (String) argumentValue.getValue();
    }

    public List<ArgumentValue> getArgumentValues() {
        return argumentValues;
    }

    private ArgumentValue getArgumentValue(@NotNull DBArgument argument) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (argument.equals(argumentValue.getArgument())) {
                return argumentValue;
            }
        }
        ArgumentValue argumentValue = new ArgumentValue(argument, null);
        argumentValue.setValue(valuesMap.get(argumentValue.getName()));
        argumentValues.add(argumentValue);
        return argumentValue;
    }

    private ArgumentValue getArgumentValue(DBArgument argument, DBTypeAttribute attribute) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (argumentValue.getArgument().equals(argument) &&
                    argumentValue.getAttribute().equals(attribute)) {
                return argumentValue;
            }
        }

        ArgumentValue argumentValue = new ArgumentValue(argument, attribute, null);
        argumentValue.setValue(valuesMap.get(argumentValue.getName()));
        argumentValues.add(argumentValue);
        return argumentValue;
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

    public Project getProject() {
        return getMethod().getProject();
    }

    public void setExecuting(boolean executing) {
        isExecuting = executing;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    /*********************************************************
     *                 PersistentConfiguration               *
     *********************************************************/
    public void readConfiguration(Element element) {
        methodRef.readState(element);
        String schemaName = element.getAttributeValue("execution-schema");
        executionSchema = new DBObjectRef<DBSchema>(methodRef.getConnectionId(), DBObjectType.SCHEMA, schemaName);
        usePoolConnection = SettingsUtil.getBooleanAttribute(element, "use-pool-connection", true);
        commitAfterExecution = SettingsUtil.getBooleanAttribute(element, "commit-after-execution", true);
        enableLogging = SettingsUtil.getBooleanAttribute(element, "enable-logging", true);
        Element argumentsElement = element.getChild("argument-list");
        for (Object object : argumentsElement.getChildren()) {
            Element argumentElement = (Element) object;
            String name = argumentElement.getAttributeValue("name");
            String value = CommonUtil.nullIfEmpty(argumentElement.getAttributeValue("value"));
            valuesMap.put(name, value);
        }
    }

    public void writeConfiguration(Element element) {
        methodRef.writeState(element);
        element.setAttribute("execution-schema", CommonUtil.nvl(executionSchema.getPath(), ""));
        SettingsUtil.setBooleanAttribute(element, "use-pool-connection", usePoolConnection);
        SettingsUtil.setBooleanAttribute(element, "commit-after-execution", commitAfterExecution);
        SettingsUtil.setBooleanAttribute(element, "enable-logging", enableLogging);

        Element argumentsElement = new Element("argument-list");
        element.addContent(argumentsElement);

        if (argumentValues.size() > 0) {
            for (ArgumentValue argumentValue : argumentValues) {
                Element argumentElement = new Element("argument");
                argumentElement.setAttribute("name", argumentValue.getName());
                argumentElement.setAttribute("value", (String) CommonUtil.nvl(argumentValue.getValue(), ""));
                argumentsElement.addContent(argumentElement);
            }
        } else {
            for (String name : valuesMap.keySet()) {
                Element argumentElement = new Element("argument");
                argumentElement.setAttribute("name", name);
                argumentElement.setAttribute("value", CommonUtil.nvl(valuesMap.get(name), ""));
                argumentsElement.addContent(argumentElement);
            }
        }
    }

    public int compareTo(@NotNull MethodExecutionInput executionInput) {
        DBMethodRef localMethod = getMethodRef();
        DBMethodRef remoteMethod = executionInput.getMethodRef();
        return localMethod.compareTo(remoteMethod);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MethodExecutionInput) {
            MethodExecutionInput executionInput = (MethodExecutionInput) obj;
            return methodRef.equals(executionInput.getMethodRef());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return methodRef.hashCode();
    }

    public MethodExecutionInput clone() {
        MethodExecutionInput executionInput = new MethodExecutionInput();
        executionInput.methodRef = methodRef;
        executionInput.executionSchema = executionSchema;
        executionInput.usePoolConnection = usePoolConnection;
        executionInput.commitAfterExecution = commitAfterExecution;
        executionInput.enableLogging = enableLogging;
        executionInput.valuesMap = new HashMap<String, String>(valuesMap);
        return executionInput;
    }

    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        executionResult = null;
        valuesMap.clear();
        argumentValues.clear();
    }

}
