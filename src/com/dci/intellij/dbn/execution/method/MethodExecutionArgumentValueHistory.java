package com.dci.intellij.dbn.execution.method;

import com.dci.intellij.dbn.common.state.PersistentStateElement;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionId;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;

public class MethodExecutionArgumentValueHistory implements PersistentStateElement {
    private final Map<ConnectionId, Map<String, MethodExecutionArgumentValue>> argumentValues = new HashMap<>();

    public MethodExecutionArgumentValue getArgumentValue(ConnectionId connectionId, String name, boolean create) {
        Map<String, MethodExecutionArgumentValue> argumentValues = this.argumentValues.get(connectionId);

        if (argumentValues != null) {
            for (String argumentName : argumentValues.keySet()) {
                if (StringUtil.equalsIgnoreCase(argumentName, name)) {
                    return argumentValues.get(argumentName);
                }
            }
        }

        if (create) {
            if (argumentValues == null) {
                argumentValues = new HashMap<>();
                this.argumentValues.put(connectionId, argumentValues);
            }

            MethodExecutionArgumentValue argumentValue = new MethodExecutionArgumentValue(name);
            argumentValues.put(name, argumentValue);
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
            this.argumentValues.clear();
            for (Element argumentValueElement : argumentValuesElement.getChildren()) {
                ConnectionId connectionId = ConnectionId.get(argumentValueElement.getAttributeValue("connection-id"));
                for (Element argumentElement : argumentValueElement.getChildren()) {
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

        for (ConnectionId connectionId : argumentValues.keySet()) {
            Map<String, MethodExecutionArgumentValue> argumentValues = this.argumentValues.get(connectionId);
            Element connectionElement = new Element("connection");
            connectionElement.setAttribute("connection-id", connectionId.id());
            argumentValuesElement.addContent(connectionElement);
            for (String argumentName : argumentValues.keySet()) {
                MethodExecutionArgumentValue argumentValue = argumentValues.get(argumentName);
                if (argumentValue.getValueHistory().size() > 0) {
                    Element argumentElement = new Element("argument");
                    connectionElement.addContent(argumentElement);
                    argumentValue.writeState(argumentElement);
                }
            }
        }
    }
}
