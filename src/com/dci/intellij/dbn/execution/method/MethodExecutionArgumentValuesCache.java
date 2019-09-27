package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import gnu.trove.THashMap;
import org.jdom.Element;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MethodExecutionArgumentValuesCache implements PersistentStateElement {
    private Map<ConnectionId, Set<MethodExecutionArgumentValue>> variablesMap = new THashMap<ConnectionId, Set<MethodExecutionArgumentValue>>();

    public MethodExecutionArgumentValue getArgumentValue(ConnectionId connectionId, String name, boolean create) {
        Set<MethodExecutionArgumentValue> argumentValues = variablesMap.get(connectionId);

        if (argumentValues != null) {
            for (MethodExecutionArgumentValue argumentValue : argumentValues) {
                if (StringUtil.equalsIgnoreCase(argumentValue.getName(), name)) {
                    return argumentValue;
                }
            }
        }

        if (create) {
            if (argumentValues == null) {
                argumentValues = new HashSet<MethodExecutionArgumentValue>();
                variablesMap.put(connectionId, argumentValues);
            }

            MethodExecutionArgumentValue argumentValue = new MethodExecutionArgumentValue(name);
            argumentValues.add(argumentValue);
            return argumentValue;

        }
        return null;
    }

    public void cacheVariable(ConnectionId connectionId, String name, String value) {
        if (StringUtil.isNotEmpty(value)) {
            MethodExecutionArgumentValue argumentValue = getArgumentValue(connectionId, name, true);
            argumentValue.setValue(value);
        }
    }

    /*********************************************
     *            PersistentStateElement         *
     *********************************************/
    @Override
    public void readState(Element element) {
        Element argumentValuesElement = element.getChild("argument-values-cache");
        if (argumentValuesElement != null) {
            this.variablesMap.clear();
            List<Element> connectionElements = argumentValuesElement.getChildren();
            for (Element connectionElement : connectionElements) {
                ConnectionId connectionId = ConnectionId.get(connectionElement.getAttributeValue("connection-id"));
                List<Element> argumentElements = connectionElement.getChildren();
                for (Element argumentElement : argumentElements) {
                    String name = argumentElement.getAttributeValue("name");
                    MethodExecutionArgumentValue argumentValue = getArgumentValue(connectionId, name, true);
                    argumentValue.readState(argumentElement);
                }
            }
        }
    }

    @Override
    public void writeState(Element element) {
        Element argumentValuesElement = new Element("argument-values-cache");
        element.addContent(argumentValuesElement);

        for (ConnectionId connectionId : variablesMap.keySet()) {
            Set<MethodExecutionArgumentValue> argumentValues = variablesMap.get(connectionId);
            Element connectionElement = new Element("connection");
            connectionElement.setAttribute("connection-id", connectionId.id());
            argumentValuesElement.addContent(connectionElement);
            for (MethodExecutionArgumentValue argumentValue : argumentValues) {
                if (argumentValue.getValueHistory().size() > 0) {
                    Element argumentElement = new Element("argument");
                    connectionElement.addContent(argumentElement);
                    argumentValue.writeState(argumentElement);
                }
            }
        }
    }
}
