package com.dci.intellij.dbn.execution.method;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdom.Element;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import gnu.trove.THashMap;

public class MethodExecutionArgumentValuesCache implements PersistentStateElement<Element> {
    private Map<String, Set<MethodExecutionArgumentValue>> variablesMap = new THashMap<String, Set<MethodExecutionArgumentValue>>();

    public MethodExecutionArgumentValue getVariable(String connectionId, String name, boolean create) {
        Set<MethodExecutionArgumentValue> executionVariables = variablesMap.get(connectionId);

        if (executionVariables != null) {
            for (MethodExecutionArgumentValue executionVariable : executionVariables) {
                if (executionVariable.getName().equalsIgnoreCase(name)) {
                    return executionVariable;
                }
            }
        }

        if (create) {
            if (executionVariables == null) {
                executionVariables = new HashSet<MethodExecutionArgumentValue>();
                variablesMap.put(connectionId, executionVariables);
            }

            MethodExecutionArgumentValue executionVariable = new MethodExecutionArgumentValue(name);
            executionVariables.add(executionVariable);
            return executionVariable;

        }
        return null;
    }

    public void cacheVariable(String connectionId, String name, String value) {
        MethodExecutionArgumentValue executionVariable = getVariable(connectionId, name, true);
        executionVariable.setValue(value);
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    public void readState(Element parent) {
        Element argumentValuesElement = parent.getChild("argument-values-cache");
        if (argumentValuesElement != null) {
            this.variablesMap.clear();
            List<Element> connectionElements = argumentValuesElement.getChildren();
            for (Element connectionElement : connectionElements) {
                String connectionId = connectionElement.getAttributeValue("connection-id");
                List<Element> argumentElements = connectionElement.getChildren();
                for (Element argumentElement : argumentElements) {
                    String name = argumentElement.getAttributeValue("name");
                    MethodExecutionArgumentValue executionVariable = getVariable(connectionId, name, true);
                    executionVariable.readState(argumentElement);
                }
            }
        }
    }

    public void writeState(Element parent) {
        Element argumentValuesElement = new Element("argument-values-cache");
        parent.addContent(argumentValuesElement);

        for (String connectionId : variablesMap.keySet()) {
            Set<MethodExecutionArgumentValue> executionVariables = variablesMap.get(connectionId);
            Element connectionElement = new Element("connection");
            connectionElement.setAttribute("connection-id", connectionId);
            argumentValuesElement.addContent(connectionElement);
            for (MethodExecutionArgumentValue executionVariable : executionVariables) {
                Element argumentElement = new Element("argument");
                connectionElement.addContent(argumentElement);
                executionVariable.writeState(argumentElement);
            }
        }
    }
}
