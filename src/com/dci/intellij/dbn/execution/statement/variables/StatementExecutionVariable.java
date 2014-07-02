package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StatementExecutionVariable implements Comparable<StatementExecutionVariable>{
    private GenericDataType dataType;
    private String name;
    private String value;
    private TemporaryValueProvider temporaryValueProvider;
    private transient Project project;

    public StatementExecutionVariable(ExecVariablePsiElement variablePsiElement) {
        this.name = variablePsiElement.getText();
        this.project = variablePsiElement.getProject();
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public GenericDataType getDataType() {
        return dataType;
    }

    public void setDataType(GenericDataType dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public TemporaryValueProvider getTemporaryValueProvider() {
        return temporaryValueProvider;
    }

    public void setTemporaryValueProvider(TemporaryValueProvider temporaryValueProvider) {
        this.temporaryValueProvider = temporaryValueProvider;
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
        return o.name.length()-name.length();
    }

    public interface TemporaryValueProvider {
        String getValue();
        GenericDataType getDataType();
    }
}
