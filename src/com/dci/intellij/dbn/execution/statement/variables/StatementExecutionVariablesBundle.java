package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecutablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.intellij.openapi.util.text.StringUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatementExecutionVariablesBundle {
    private Map<StatementExecutionVariable, String> errorMap;
    private Set<StatementExecutionVariable> variables = new THashSet<StatementExecutionVariable>();
    private ConnectionHandler activeConnection;
    private DBSchema currentSchema;

    public StatementExecutionVariablesBundle(ConnectionHandler activeConnection, DBSchema currentSchema, Set<ExecVariablePsiElement> variablePsiElements) {
        this.activeConnection = activeConnection;
        this.currentSchema = currentSchema;
        initialize(variablePsiElements);
    }

    public void initialize(Set<ExecVariablePsiElement> variablePsiElements) {
        Set<StatementExecutionVariable> newVariables = new THashSet<StatementExecutionVariable>();
        for (ExecVariablePsiElement variablePsiElement : variablePsiElements) {
            StatementExecutionVariable variable = getVariable(variablePsiElement.getText());
            if (variable == null) {
                variable = new StatementExecutionVariable(variablePsiElement);
            }

            if (variable.getDataType() == null) {
                DBDataType dataType = lookupDataType(variablePsiElement);
                if (dataType != null && dataType.isNative()) {
                    variable.setDataType(dataType.getNativeDataType().getBasicDataType());
                } else {
                    variable.setDataType(GenericDataType.LITERAL);
                }
            }
            newVariables.add(variable);
        }
        variables = newVariables;
    }

    public ConnectionHandler getActiveConnection() {
        return activeConnection;
    }

    public DBSchema getCurrentSchema() {
        return currentSchema;
    }

    public boolean isIncomplete() {
        for (StatementExecutionVariable variable : variables) {
            if (StringUtil.isEmpty(variable.getValue())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasErrors() {
        return errorMap != null && errorMap.size() > 0;
    }

    private DBDataType lookupDataType(ExecVariablePsiElement variablePsiElement) {
        BasePsiElement parent = variablePsiElement.lookupEnclosingNamedPsiElement();
        Set<BasePsiElement> bucket = null;
        while (parent != null) {
            bucket = parent.collectObjectPsiElements(bucket, DBObjectType.COLUMN.getFamilyTypes(), IdentifierCategory.REFERENCE);
            if (bucket != null) {
                if (bucket.size() > 1) {
                    return null;
                }

                if (bucket.size() == 1) {
                    Object psiElement = bucket.toArray()[0];
                    if (psiElement instanceof IdentifierPsiElement) {
                        IdentifierPsiElement columnPsiElement = (IdentifierPsiElement) psiElement;
                        DBObject object = columnPsiElement.resolveUnderlyingObject();
                        if (object != null && object instanceof DBColumn) {
                            DBColumn column = (DBColumn) object;
                            return column.getDataType();
                        }

                    }
                    return null;
                }
            }

            parent = parent.lookupEnclosingNamedPsiElement();
            if (parent instanceof ExecutablePsiElement) break;
        }
        return null;
    }

    @Nullable
    public StatementExecutionVariable getVariable(String name) {
        for (StatementExecutionVariable variable : variables) {
            if (variable.getName().equalsIgnoreCase(name)) {
                return variable;
            }
        }
        return null;
    }

    public Set<StatementExecutionVariable> getVariables() {
        return variables;
    }

    public String prepareStatementText(ConnectionHandler connectionHandler, String statementText, boolean temporary) {
        errorMap = null;
        List<StatementExecutionVariable> variables = new ArrayList<StatementExecutionVariable>(this.variables);
        Collections.sort(variables);
        for (StatementExecutionVariable variable : variables) {
            String value = temporary ? variable.getTemporaryValueProvider().getValue() : variable.getValue();
            GenericDataType genericDataType = temporary ? variable.getTemporaryValueProvider().getDataType() : variable.getDataType();

            if (!StringUtil.isEmpty(value)) {
                RegionalSettings regionalSettings = RegionalSettings.getInstance(connectionHandler.getProject());

                if (genericDataType == GenericDataType.LITERAL) {
                    value = StringUtil.replace(value, "'", "''");
                    value = "'" + value + "'" ;
                } else if (genericDataType == GenericDataType.DATE_TIME){
                    DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                    try {
                        Date date = regionalSettings.getFormatter().parseDateTime(value);
                        value = metadataInterface.createDateString(date);
                    } catch (ParseException e) {
                        try {
                            Date date = regionalSettings.getFormatter().parseDate(value);
                            value = metadataInterface.createDateString(date);
                        } catch (ParseException e1) {
                            addError(variable, "Invalid date");
                        }
                    }
                } else if (genericDataType == GenericDataType.NUMERIC){
                    try {
                        regionalSettings.getFormatter().parseNumber(value);
                    } catch (ParseException e) {
                        addError(variable, "Invalid number");
                    }

                } else {
                    throw new IllegalArgumentException("Data type " + genericDataType.getName() + " not supported with execution variables.");
                }

                statementText = StringUtil.replaceIgnoreCase(statementText, variable.getName(), value);
            }
        }
        return statementText;
    }

    private void addError(StatementExecutionVariable variable, String value) {
        if (errorMap == null) {
            errorMap = new THashMap<StatementExecutionVariable, String>();
        }
        errorMap.put(variable, value);
    }

    public String getError(StatementExecutionVariable variable) {
        return errorMap == null ? null : errorMap.get(variable);
    }
}
