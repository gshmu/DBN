package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.list.MostRecentStack;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.intellij.openapi.components.PersistentStateComponent;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringTokenizer;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.enumAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@Setter
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

    public boolean isProvided() {
        return useNull || valueHistory.get() != null;
    }

    @Override
    public boolean useNull() {
        return useNull;
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
        name = stringAttribute(state, "name");
        dataType = enumAttribute(state, "dataType", GenericDataType.class);
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
