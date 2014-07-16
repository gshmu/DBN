package com.dci.intellij.dbn.execution.method.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jdom.Element;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.options.setting.SettingsUtil;
import com.dci.intellij.dbn.execution.method.MethodExecutionInput;
import com.dci.intellij.dbn.object.DBMethod;
import com.dci.intellij.dbn.object.lookup.DBMethodRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;

public class MethodExecutionHistory implements Disposable{
    private List<MethodExecutionInput> executionInputs = new ArrayList<MethodExecutionInput>();
    private boolean groupEntries = true;
    private DBMethodRef selection;

    public List<MethodExecutionInput> getExecutionInputs() {
        return executionInputs;
    }

    public void setExecutionInputs(List<MethodExecutionInput> executionInputs) {
        this.executionInputs = executionInputs;
    }

    public boolean isGroupEntries() {
        return groupEntries;
    }

    public void setGroupEntries(boolean groupEntries) {
        this.groupEntries = groupEntries;
    }

    public DBMethodRef getSelection() {
        return selection;
    }

    public void setSelection(DBMethodRef selection) {
        this.selection = selection;
    }

    public MethodExecutionInput getExecutionInput(DBMethod method) {
        for (MethodExecutionInput executionInput : executionInputs) {
            if (executionInput.getMethodRef().is(method)) {
                return executionInput;
            }
        }
        MethodExecutionInput executionInput = new MethodExecutionInput(method);
        executionInputs.add(executionInput);
        Collections.sort(executionInputs);
        selection = (DBMethodRef) method.getRef();
        return executionInput;
    }

    public MethodExecutionInput getExecutionInput(DBMethodRef methodRef) {
        for (MethodExecutionInput executionInput : executionInputs) {
            if (executionInput.getMethodRef().equals(methodRef)) {
                return executionInput;
            }
        }

        DBMethod method = methodRef.get();
        if (method != null) {
            MethodExecutionInput executionInput = new MethodExecutionInput(method);
            executionInputs.add(executionInput);
            Collections.sort(executionInputs);
            selection = methodRef;
            return executionInput;
        }

        return null;
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(executionInputs);
    }

    public void readConfiguration(Element element) throws InvalidDataException {
        groupEntries = SettingsUtil.getBoolean(element, "group-history-entries", groupEntries);

        Element executionInputsElement = element.getChild("execution-inputs");
        for (Object object : executionInputsElement.getChildren()) {
            Element configElement = (Element) object;
            MethodExecutionInput executionInput = new MethodExecutionInput();
            executionInput.readConfiguration(configElement);
            if (executionInput.getMethodRef().getSchemaName() != null) {
                executionInputs.add(executionInput);
            }
        }
        Collections.sort(executionInputs);

        Element selectionElement = element.getChild("selection");
        if (selectionElement != null) {
            selection = new DBMethodRef();
            selection.readConfiguration(selectionElement);
        }
    }

    public void writeConfiguration(Element element) throws WriteExternalException {
        SettingsUtil.setBoolean(element, "group-entries", groupEntries);

        Element configsElement = new Element("execution-inputs");
        element.addContent(configsElement);
        for (MethodExecutionInput executionInput : this.executionInputs) {
            Element configElement = new Element("execution-input");
            executionInput.writeConfiguration(configElement);
            configsElement.addContent(configElement);
        }

        if (selection != null) {
            Element selectionElement = new Element("selection");
            element.addContent(selectionElement);
            selection.writeConfiguration(selectionElement);
        }

    }

    public MethodExecutionInput getLastSelection() {
        if (selection != null) {
            for (MethodExecutionInput executionInput : executionInputs) {
                if (executionInput.getMethodRef().equals(selection)) {
                    return executionInput;
                }
            }
        }
        return null;
    }
}
