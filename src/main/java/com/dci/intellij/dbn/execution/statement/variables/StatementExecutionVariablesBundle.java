package com.dci.intellij.dbn.execution.statement.variables;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.util.Lists;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.interfaces.DatabaseMetadataInterface;
import com.dci.intellij.dbn.language.common.element.util.ElementTypeAttribute;
import com.dci.intellij.dbn.language.common.element.util.IdentifierCategory;
import com.dci.intellij.dbn.language.common.psi.BasePsiElement;
import com.dci.intellij.dbn.language.common.psi.ExecVariablePsiElement;
import com.dci.intellij.dbn.language.common.psi.IdentifierPsiElement;
import com.dci.intellij.dbn.language.common.psi.lookup.ObjectLookupAdapter;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.*;

public class StatementExecutionVariablesBundle extends StatefulDisposableBase implements StatefulDisposable {
    public static final Comparator<StatementExecutionVariable> NAME_COMPARATOR = (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName());
    public static final Comparator<StatementExecutionVariable> NAME_LENGTH_COMPARATOR = (o1, o2) -> o2.getName().length() - o1.getName().length();
    public static final Comparator<StatementExecutionVariable> OFFSET_COMPARATOR = (o1, o2) -> o1.getOffset() - o2.getOffset();

    private Map<String, String> errorMap;
    private List<StatementExecutionVariable> variables = new ArrayList<>();

    public StatementExecutionVariablesBundle(List<ExecVariablePsiElement> variablePsiElements) {
        initialize(variablePsiElements);
    }

    public void initialize(List<ExecVariablePsiElement> variablePsiElements) {
        List<StatementExecutionVariable> variables = new ArrayList<>();
        for (ExecVariablePsiElement variablePsiElement : variablePsiElements) {
            StatementExecutionVariable variable = getVariable(variablePsiElement.getText());
            if (variable == null) {
                variable = new StatementExecutionVariable(variablePsiElement);
            } else {
                variable.setOffset(variablePsiElement.getTextOffset());
            }

            if (variable.getDataType() == null) {
                DBDataType dataType = lookupDataType(variablePsiElement);
                if (dataType != null && dataType.isNative()) {
                    variable.setDataType(dataType.getGenericDataType());
                } else {
                    variable.setDataType(GenericDataType.LITERAL);
                }
            }
            uniqueAddVariable(variables, variable);
        }
        this.variables = variables;
    }

    private void uniqueAddVariable(List<StatementExecutionVariable> variables, StatementExecutionVariable variable) {
        if (Lists.noneMatch(variables, v -> Objects.equals(v.getName(), variable.getName()))) {
            variables.add(variable);
        }
    }

    public boolean isProvided() {
        for (StatementExecutionVariable variable : variables) {
            if (!variable.isProvided()) {
                return false;
            }
        }
        return true;
    }

    public void populate(Map<String, StatementExecutionVariable> variableCache, boolean force) {
        for (StatementExecutionVariable variable : variables) {
            if (!variable.isProvided() || force) {
                StatementExecutionVariable cacheVariable = variableCache.get(variable.getName().toUpperCase());
                if (cacheVariable != null) {
                    variable.populate(cacheVariable);
                }
            }
        }
    }

    public boolean hasErrors() {
        return errorMap != null && errorMap.size() > 0;
    }

    private static DBDataType lookupDataType(ExecVariablePsiElement variablePsiElement) {
        BasePsiElement conditionPsiElement = variablePsiElement.findEnclosingPsiElement(ElementTypeAttribute.CONDITION);

        if (conditionPsiElement != null) {
            ObjectLookupAdapter lookupAdapter = new ObjectLookupAdapter(variablePsiElement, IdentifierCategory.REFERENCE, DBObjectType.COLUMN);
            BasePsiElement basePsiElement = lookupAdapter.findInScope(conditionPsiElement);
            if (basePsiElement instanceof IdentifierPsiElement) {
                IdentifierPsiElement columnPsiElement = (IdentifierPsiElement) basePsiElement;
                DBObject object = columnPsiElement.getUnderlyingObject();
                if (object instanceof DBColumn) {
                    DBColumn column = (DBColumn) object;
                    return column.getDataType();
                }
            }
        }
        return null;
    }

    @Nullable
    public StatementExecutionVariable getVariable(String name) {
        for (StatementExecutionVariable variable : variables) {
            if (Strings.equalsIgnoreCase(variable.getName(), name)) {
                return variable;
            }
        }
        return null;
    }

    public List<StatementExecutionVariable> getVariables() {
        return variables;
    }

    public String prepareStatementText(@NotNull ConnectionHandler connection, String statementText, boolean forPreview) {
        errorMap = null;
        List<StatementExecutionVariable> variables = new ArrayList<>(this.variables);
        variables.sort(NAME_LENGTH_COMPARATOR);
        Formatter formatter = Formatter.getInstance(connection.getProject());
        for (StatementExecutionVariable variable : variables) {
            VariableValueProvider previewValueProvider = variable.getPreviewValueProvider();
            boolean useNullValue = forPreview ? previewValueProvider.useNull() : variable.useNull();

            if (useNullValue) {
                statementText = Strings.replaceIgnoreCase(statementText, variable.getName(), "NULL");
            } else {
                String value = forPreview ? previewValueProvider.getValue() : variable.getValue();
                if (!Strings.isEmpty(value)) {
                    GenericDataType genericDataType = forPreview ? previewValueProvider.getDataType() : variable.getDataType();
                    if (genericDataType == GenericDataType.LITERAL) {
                        value = Strings.replace(value, "'", "''");
                        value = '\'' + value + '\'';
                    } else {
                        if (genericDataType == GenericDataType.DATE_TIME){
                            DatabaseMetadataInterface dmi = connection.getMetadataInterface();
                            try {
                                Date date = formatter.parseDateTime(value);
                                value = dmi.createDateString(date);
                            } catch (ParseException e) {
                                try {
                                    Date date = formatter.parseDate(value);
                                    value = dmi.createDateString(date);
                                } catch (ParseException e1) {
                                    addError(variable, "Invalid date");
                                }
                            }
                        } else if (genericDataType == GenericDataType.NUMERIC){
                            try {
                                formatter.parseNumber(value);
                            } catch (ParseException e) {
                                addError(variable, "Invalid number");
                            }

                        } else {
                            throw new IllegalArgumentException("Data type " + genericDataType.getName() + " not supported with execution variables.");
                        }
                    }

                    statementText = Strings.replaceIgnoreCase(statementText, variable.getName(), value);
                }
            }
        }
        return statementText;
    }

    private void addError(StatementExecutionVariable variable, String value) {
        if (errorMap == null) {
            errorMap = new HashMap<>();
        }
        errorMap.put(variable.getName(), value);
    }

    public String getError(StatementExecutionVariable variable) {
        return errorMap == null ? null : errorMap.get(variable.getName());
    }

    @Override
    protected void disposeInner() {
        nullify();
    }
}
