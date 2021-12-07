package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.options.BasicConfiguration;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.database.DatabaseMetadataInterface;
import com.dci.intellij.dbn.editor.data.filter.ui.DatasetBasicFilterConditionForm;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Date;
import java.util.StringTokenizer;

import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.booleanAttribute;
import static com.dci.intellij.dbn.common.options.setting.SettingsSupport.stringAttribute;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class DatasetBasicFilterCondition extends BasicConfiguration<DatasetBasicFilter, DatasetBasicFilterConditionForm> {

    private String columnName = "";
    private ConditionOperator operator;
    private String value = "";
    private boolean active = true;

    public DatasetBasicFilterCondition(DatasetBasicFilter parent){
        super(parent);
    }

    public DatasetBasicFilterCondition(DatasetBasicFilter parent, String columnName, Object value, ConditionOperator operator, boolean active) {
        super(parent);
        this.columnName = columnName;
        this.operator = operator;
        this.value = value == null ? "" : value.toString();
        this.active = active;
    }

    public DatasetBasicFilterCondition(DatasetBasicFilter parent, String columnName, Object value, ConditionOperator operator) {
        super(parent);
        this.columnName = columnName;
        this.value = value == null ? "" : value.toString();
        this.operator = operator == null ? Strings.isEmpty(this.value) ? ConditionOperator.IS_NULL : ConditionOperator.EQUAL : operator;

        this.active = true;
    }

    public DatasetBasicFilter getFilter() {
        return getParent();
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    public void appendConditionString(StringBuilder buffer, DBDataset dataset) {
        DatasetBasicFilterConditionForm editorForm = getSettingsEditor();

        String columnName = this.columnName;
        ConditionOperator operator = this.operator;
        String value = this.value;

        if (Failsafe.check(editorForm)) {
            operator = editorForm.getSelectedOperator();
            DBColumn selectedColumn = editorForm.getSelectedColumn();
            if (selectedColumn != null) {
                columnName = selectedColumn.getName();
                value = editorForm.getValue();
            }
        }

        DBColumn column = dataset.getColumn(columnName);
        DBDataType dataType = column == null ? null : column.getDataType();


        if (dataType != null && dataType.isNative()) {
            GenericDataType genericDataType = dataType.getGenericDataType();

            if (operator == ConditionOperator.IN || operator == ConditionOperator.NOT_IN) {
                if (genericDataType == GenericDataType.LITERAL) {
                    StringTokenizer tokenizer = new StringTokenizer(value, ",");
                    StringBuilder valueBuilder = new StringBuilder();
                    while (tokenizer.hasMoreTokens()) {
                        if (valueBuilder.length() > 0) valueBuilder.append(", ");
                        String quotedValue = quoteValue(tokenizer.nextToken().trim());
                        valueBuilder.append(quotedValue);
                    }
                    value = valueBuilder.toString();
                }
                value = "(" + value + ")";
            }
            else if (Strings.isNotEmptyOrSpaces(value)) {
                ConnectionHandler connectionHandler = Failsafe.nn(dataset.getConnectionHandler());
                if (genericDataType == GenericDataType.LITERAL || genericDataType == GenericDataType.CLOB) {
                    value = quoteValue(value);
                } else if (genericDataType == GenericDataType.DATE_TIME) {
                    DatabaseMetadataInterface metadataInterface = connectionHandler.getInterfaceProvider().getMetadataInterface();
                    Formatter formatter = Formatter.getInstance(dataset.getProject());
                    try {
                        Date date = formatter.parseDateTime(value);
                        value = metadataInterface.createDateString(date);
                    } catch (ParseException e) {
                        try {
                            Date date = formatter.parseDate(value);
                            value = metadataInterface.createDateString(date);
                        } catch (ParseException e1) {
                            // value can be something like "sysdate" => not parseable
                            //e1.printStackTrace();
                        }
                    }
                } else if (genericDataType == GenericDataType.NUMERIC) {
                /*try {
                    regionalSettings.getFormatter().parseNumber(value);
                } catch (ParseException e) {
                    e.printStackTrace();

                }*/
                }
            }
        }

        buffer.append(column == null ? columnName : column.getQuotedName(false));
        buffer.append(" ");
        buffer.append(operator == null ? " " : operator.getText());
        buffer.append(" ");
        buffer.append(value);
    }

    @NotNull
    private String quoteValue(String value) {
        if (value.length() > 0) {
            boolean needsBeginQuote = value.charAt(0) != '\'';
            boolean needsEndQuote = value.charAt(value.length() -1) != '\'';

            if (needsBeginQuote && needsEndQuote) {
                value = Strings.replace(value, "'", "''");
            }

            if (needsBeginQuote) {
                value = '\'' + value;
            }

            if (needsEndQuote) {
                value = value + '\'';
            }
        }
        return value;
    }

    /****************************************************
    *                   Configuration                  *
    ****************************************************/
    @Override
    @NotNull
    public DatasetBasicFilterConditionForm createConfigurationEditor() {
        DBDataset dataset = getFilter().lookupDataset();
        return new DatasetBasicFilterConditionForm(dataset, this);
    }

    @Override
    public void readConfiguration(Element element) {
       columnName = stringAttribute(element, "column");
       operator = ConditionOperator.get(stringAttribute(element, "operator"));
       value = element.getAttributeValue("value");
       active = booleanAttribute(element, "active", true);
    }

    @Override
    public void writeConfiguration(Element element) {
        element.setAttribute("column", columnName);
        element.setAttribute("operator", operator.getText());
        element.setAttribute("value", value);
        element.setAttribute("active", Boolean.toString(active));
    }
}
