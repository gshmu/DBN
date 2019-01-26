package com.dci.intellij.dbn.editor.data.filter.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.editor.ui.TextFieldPopupType;
import com.dci.intellij.dbn.data.editor.ui.TextFieldWithPopup;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.editor.data.filter.ConditionOperator;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilterCondition;
import com.dci.intellij.dbn.editor.data.filter.action.DeleteBasicFilterConditionAction;
import com.dci.intellij.dbn.editor.data.filter.action.EnableDisableBasicFilterConditionAction;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DatasetBasicFilterConditionForm extends ConfigurationEditorForm<DatasetBasicFilterCondition> {

    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JPanel valueFieldPanel;
    private boolean active = true;

    private DBNComboBox<DBColumn> columnSelector;
    private DBNComboBox<ConditionOperator> operatorSelector;

    private DatasetBasicFilterForm basicFilterForm;
    private TextFieldWithPopup editorComponent;
    private DBObjectRef<DBDataset> datasetRef;

    public DatasetBasicFilterConditionForm(DBDataset dataset, DatasetBasicFilterCondition condition) {
        super(condition);
        datasetRef = DBObjectRef.from(dataset);
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.SimpleFilter.Condition", true,
                new EnableDisableBasicFilterConditionAction(this),
                new DeleteBasicFilterConditionAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

        DBColumn column = dataset.getColumn(condition.getColumnName());
        if (column == null) {
            for (DBColumn col : dataset.getColumns()) {
                if (col.getDataType().isNative()) {
                    column = col;
                    break;
                }
            }
        }
        GenericDataType dataType = column == null ? null : column.getDataType().getGenericDataType();

        columnSelector.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
        columnSelector.setValueLoader(this::loadColumns);
        columnSelector.setSelectedValue(column);
        columnSelector.addListener((oldValue, newValue) -> {
            if (newValue != null) {
                GenericDataType selectedDataType = newValue.getDataType().getGenericDataType();
                editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, selectedDataType == GenericDataType.DATE_TIME);
            }
            if (basicFilterForm != null) {
                basicFilterForm.updateNameAndPreview();
            }
            operatorSelector.reloadValues();
        });


        operatorSelector.setValueLoader(this::loadOperators);
        operatorSelector.setSelectedValue(condition.getOperator());
        operatorSelector.addListener((oldValue, newValue) -> {
            if (basicFilterForm != null) {
                basicFilterForm.updateNameAndPreview();
                updateValueTextField();
            }
        });

        editorComponent = new TextFieldWithPopup(dataset.getProject());
        editorComponent.createCalendarPopup(false);
        editorComponent.setPopupEnabled(TextFieldPopupType.CALENDAR, dataType == GenericDataType.DATE_TIME);
        
        valueFieldPanel.add(editorComponent, BorderLayout.CENTER);

        JTextField valueTextField = editorComponent.getTextField();
        valueTextField.setText(condition.getValue());
        setActive(condition.isActive());


        DocumentListener documentListener = new DocumentListener();
        valueTextField.getDocument().addDocumentListener(documentListener);
        valueTextField.addKeyListener(ComboBoxSelectionKeyListener.create(columnSelector, false));
        valueTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorSelector, true));

        updateValueTextField();

        valueTextField.setToolTipText("<html>While editing value, <br> " +
                "press <b>Up/Down</b> keys to change column or <br> " +
                "press <b>Ctrl-Up/Ctrl-Down</b> keys to change operator</html>");

        Disposer.register(this, editorComponent);

    }

    @NotNull
    List<ConditionOperator> loadOperators() {
        DBColumn selectedColumn = getSelectedColumn();
        if (selectedColumn != null) {
            Class typeClass = selectedColumn.getDataType().getTypeClass();
            return Arrays.asList(ConditionOperator.getConditionOperators(typeClass));
        }
        return Collections.emptyList();
    }

    @NotNull
    List<DBColumn> loadColumns() {
        DBDataset dataset1 = datasetRef.get();
        if (dataset1 != null) {
            List<DBColumn> columns = new ArrayList<>(dataset1.getColumns());
            Collections.sort(columns);
            return columns;
        }
        return Collections.emptyList();
    }

    @Override
    public void focus() {
        JTextField valueTextField = editorComponent.getTextField();
        valueTextField.selectAll();
        valueTextField.grabFocus();
    }

    private class DocumentListener extends DocumentAdapter{
        @Override
        protected void textChanged(DocumentEvent e) {
            basicFilterForm.updateNameAndPreview();
        }
    }

    public void setBasicFilterPanel(DatasetBasicFilterForm basicFilterForm) {
        this.basicFilterForm = basicFilterForm;
    }

    @Nullable
    public DBColumn getSelectedColumn() {
        return columnSelector.getSelectedValue();
    }

    public ConditionOperator getSelectedOperator() {
        return operatorSelector.getSelectedValue();
    }

    public String getValue() {
        return editorComponent.getText();
    }

    public DatasetBasicFilterCondition getCondition() {
        return getConfiguration();
    }

    public DatasetBasicFilterCondition createCondition() {
        return new DatasetBasicFilterCondition(
                basicFilterForm.getConfiguration(),
                getSelectedColumn().getName(),
                editorComponent.getText(), getSelectedOperator(),
                active);
    }

    public void remove() {
        DatasetBasicFilterCondition condition = getConfiguration();
        DatasetBasicFilterForm settingsEditor = (DatasetBasicFilterForm) condition.getFilter().getSettingsEditor();
        settingsEditor.removeConditionPanel(this);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        columnSelector.setEnabled(active);
        operatorSelector.setEnabled(active);
        editorComponent.getTextField().setEnabled(active);
        if (basicFilterForm != null) {
            basicFilterForm.updateNameAndPreview();
        }
    }

    private ListCellRenderer cellRenderer = new ColoredListCellRenderer() {
        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            DBObjectRef<DBColumn> columnRef = (DBObjectRef<DBColumn>) value;
            DBColumn column = DBObjectRef.get(columnRef);
            if (column != null) {
                setIcon(column.getIcon());
                append(column.getName(), active ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAYED_ATTRIBUTES);
            }
        }
    };

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DatasetBasicFilterCondition condition = getConfiguration();
        DBColumn column = getSelectedColumn();
        ConditionOperator operator = getSelectedOperator();
        String value = editorComponent.getText();

        condition.setColumnName(column == null ? "" : column.getName());
        condition.setOperator(operator);
        condition.setValue(value == null ? "" : value);
        condition.setActive(active);
    }

    private void updateValueTextField() {
        JTextField valueTextField = editorComponent.getTextField();
        ConditionOperator selectedOperator = getSelectedOperator();
        valueTextField.setEnabled(selectedOperator!= null && !selectedOperator.isFinal() && active);
        if (selectedOperator == null || selectedOperator.isFinal()) valueTextField.setText(null);
    }

    @Override
    public void resetFormChanges() {

    }

    @Override
    public void dispose() {
        super.dispose();
        editorComponent = null;
        basicFilterForm = null;
    }


}
