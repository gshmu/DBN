package com.dci.intellij.dbn.execution.method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.options.PersistentConfiguration;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.execution.method.result.MethodExecutionResult;
import com.dci.intellij.dbn.execution.method.result.ui.MethodExecutionResultForm;
import com.dci.intellij.dbn.object.DBArgument;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBTypeAttribute;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public class MethodExecutionInput implements Disposable, PersistentConfiguration, Comparable<MethodExecutionInput> {
    private DBMethodRef<DBMethod> method;
    private DBObjectRef<DBSchema> executionSchema;
    private Map<String, String> valuesMap = new HashMap<String, String>();
    private boolean usePoolConnection = true;
    private boolean commitAfterExecution = true;
    private boolean isExecuting = false;


    private transient MethodExecutionResult executionResult;
    private transient List<ArgumentValue> argumentValues = new ArrayList<ArgumentValue>();

    private transient boolean executionCancelled;

    public MethodExecutionInput() {
        method = new DBMethodRef<DBMethod>();
        executionSchema = new DBObjectRef<DBSchema>();
    }

    public MethodExecutionInput(DBMethod method) {
        this.method = new DBMethodRef<DBMethod>(method);
        this.executionSchema = method.getSchema().getRef();
    }

    public void initExecutionResult(boolean debug) {
        MethodExecutionResultForm resultPanel = executionResult == null || executionResult.isDisposed() ? null : executionResult.getResultPanel();
        executionResult = new MethodExecutionResult(this, resultPanel, debug);
    }

    @Nullable
    public DBMethod getMethod() {
        return method.get();
    }

    public DBMethodRef getMethodRef() {
        return method;
    }

    public ConnectionHandler getConnectionHandler() {
        return method.lookupConnectionHandler();
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

    public void setInputValue(DBArgument argument, DBTypeAttribute typeAttribute, String value) {
        ArgumentValue argumentValue = getArgumentValue(argument, typeAttribute);
        argumentValue.setValue(value);
    }

    public void setInputValue(DBArgument argument, String value) {
        ArgumentValue argumentValue = getArgumentValue(argument);
        argumentValue.setValue(value);
    }

    public String getInputValue(DBArgument argument) {
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

    private ArgumentValue getArgumentValue(DBArgument argument) {
        for (ArgumentValue argumentValue : argumentValues) {
            if (argumentValue.getArgument().equals(argument)) {
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

    public Project getProject() {
        return getMethod().getProject();
    }

    public void setExecuting(boolean executing) {
        isExecuting = executing;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void dispose() {
        executionResult = null;
        valuesMap.clear();
        argumentValues.clear();
    }

    /*********************************************************
     *                   JDOMExternalizable                  *
     *********************************************************/
    public void readConfiguration(Element element) throws InvalidDataException {
        method.readConfiguration(element);
        String schemaName = element.getAttributeValue("execution-schema");
        executionSchema = new DBObjectRef<DBSchema>(method.getConnectionId(), DBObjectType.SCHEMA, schemaName);
        usePoolConnection = SettingsUtil.getBooleanAttribute(element, "use-pool-connection", true);
        commitAfterExecution = SettingsUtil.getBooleanAttribute(element, "commit-after-execution", true);
        Element argumentsElement = element.getChild("argument-list");
        for (Object object : argumentsElement.getChildren()) {
            Element argumentElement = (Element) object;
            String name = argumentElement.getAttributeValue("name");
            String value = CommonUtil.nullIfEmpty(argumentElement.getAttributeValue("value"));
            valuesMap.put(name, value);
        }
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        method.writeConfiguration(element);
        element.setAttribute("execution-schema", CommonUtil.nvl(executionSchema.getPath(), ""));
        SettingsUtil.setBooleanAttribute(element, "use-pool-connection", usePoolConnection);
        SettingsUtil.setBooleanAttribute(element, "commit-after-execution", commitAfterExecution);

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
            return method.equals(executionInput.getMethodRef());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    public MethodExecutionInput clone() {
        MethodExecutionInput executionInput = new MethodExecutionInput();
        executionInput.method = method;
        executionInput.executionSchema = executionSchema;
        executionInput.usePoolConnection = usePoolConnection;
        executionInput.commitAfterExecution = commitAfterExecution;
        executionInput.valuesMap = new HashMap<String, String>(valuesMap);
        return executionInput;
    }
}
