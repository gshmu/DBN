package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.list.MostRecentStack;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringTokenizer;

public class StatementExecutionVariable extends VariableValueProvider implements Comparable<StatementExecutionVariable>, PersistentStateComponent<Element>{
    private GenericDataType dataType;
    private String name;
    private int offset;
    private MostRecentStack<String> valueHistory = new MostRecentStack<String>();
    private VariableValueProvider previewValueProvider;
    private boolean useNull;

    public StatementExecutionVariable(Element state) {
        loadState(state);
    }

    public StatementExecutionVariable(StatementExecutionVariable source) {
        dataType = source.dataType;
        name = source.name;
        valueHistory = new MostRecentStack<>(source.getValueHistory());
    }

    public StatementExecutionVariable(ExecVariablePsiElement variablePsiElement) {
        this.name = variablePsiElement.getText();
        this.offset = variablePsiElement.getTextOffset();
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    @Override
    public GenericDataType getDataType() {
        return dataType;
    }

    public void setDataType(GenericDataType dataType) {
        this.dataType = dataType;
    }

    @Override
    public String getValue() {
        return valueHistory.get();
    }

    public void setValue(String value) {
        valueHistory.stack(value);
    }

    public Iterable<String> getValueHistory() {
        return valueHistory;
    }

    @NotNull
    public VariableValueProvider getPreviewValueProvider() {
        return previewValueProvider == null ? this : previewValueProvider;
    }

    public void setPreviewValueProvider(VariableValueProvider previewValueProvider) {
        this.previewValueProvider = previewValueProvider;
    }

    public boolean isProvided() {
        return useNull || valueHistory.get() != null;
    }

    @Override
    public boolean useNull() {
        return useNull;
    }

    public void setUseNull(boolean isNull) {
        this.useNull = isNull;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatementExecutionVariable that = (StatementExecutionVariable) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(@NotNull StatementExecutionVariable o) {
        return name.compareTo(o.name);
    }

    @Nullable
    @Override
    public Element getState() {
        Element state = new Element("variable");
        state.setAttribute("name", name);
        state.setAttribute("dataType", dataType.name());
        StringBuilder values = new StringBuilder();
        for (String value : valueHistory) {
            if (values.length() > 0) values.append(", ");
            values.append(value);
        }
        state.setAttribute("values", values.toString());

        return state;
    }

    @Override
    public void loadState(Element state) {
        name = state.getAttributeValue("name");
        dataType = GenericDataType.valueOf(state.getAttributeValue("dataType"));
        String variableValues = state.getAttributeValue("values");
        StringTokenizer valuesTokenizer = new StringTokenizer(variableValues, ",");

        while (valuesTokenizer.hasMoreTokens()) {
            String value = valuesTokenizer.nextToken().trim();
            valueHistory.add(value);
        }
    }

    public void populate(StatementExecutionVariable variable) {
        setUseNull(variable.useNull());
        setValue(variable.getValue());
    }
}
