package com.dci.intellij.dbn.object.filter.name.ui;

import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.name.CompoundFilterCondition;
import com.dci.intellij.dbn.object.filter.name.SimpleNameFilterCondition;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ObjectNameFilterConditionForm extends DBNFormImpl<ObjectNameFilterConditionDialog> {
    private JPanel mainPanel;
    private JLabel objectNameLabel;
    private JLabel wildcardsHintLabel;
    private JTextField textPatternTextField;
    private DBNComboBox<ConditionOperator> operatorComboBox;
    private DBNComboBox<ConditionJoinType> joinTypeComboBox;

    private SimpleNameFilterCondition condition;
    public enum Operation {CREATE, EDIT, JOIN}

    ObjectNameFilterConditionForm(
            ObjectNameFilterConditionDialog parentComponent,
            CompoundFilterCondition parentCondition,
            SimpleNameFilterCondition condition,
            DBObjectType objectType,
            Operation operation) {

        super(parentComponent);
        this.condition = condition == null ? new SimpleNameFilterCondition(ConditionOperator.EQUAL, "") : condition;
        joinTypeComboBox.setValues(ConditionJoinType.values());
        joinTypeComboBox.setVisible(false);
        if (operation == Operation.JOIN) {
            joinTypeComboBox.setVisible(true);
            if (parentCondition != null && parentCondition.getJoinType() == ConditionJoinType.AND) {
                joinTypeComboBox.setSelectedValue(ConditionJoinType.OR);
            } else {
                joinTypeComboBox.setSelectedValue(ConditionJoinType.AND);
            }
        } else if (operation == Operation.CREATE) {
            if (parentCondition != null && parentCondition.getConditions().size() == 1) {
                joinTypeComboBox.setVisible(true);
                joinTypeComboBox.setSelectedValue(ConditionJoinType.AND);
            }
        }

        objectNameLabel.setIcon(objectType.getIcon());
        objectNameLabel.setText(objectType.getName().toUpperCase() + " NAME");

        operatorComboBox.setValues(ConditionOperator.values());;
        if (operation == Operation.EDIT) {
            textPatternTextField.setText(condition == null ? "" : condition.getPattern());
            operatorComboBox.setSelectedValue(condition == null ? null : condition.getOperator());

        }
        textPatternTextField.selectAll();
        textPatternTextField.setToolTipText("<html>While editing, <br> " +
                "press <b>Up/Down</b> keys to change the operator</html>");
        textPatternTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorComboBox, false));

        //wildcardsHintLabel.setIcon(Icons.COMMON_INFO);
        //wildcardsHintLabel.setDisabledIcon(Icons.COMMON_INFO_DISABLED);
        showHideWildcardHint();

        operatorComboBox.addListener(new ValueSelectorListener<ConditionOperator>() {
            @Override
            public void selectionChanged(ConditionOperator oldValue, ConditionOperator newValue) {
                showHideWildcardHint();
            }
        });
    }

    private void showHideWildcardHint() {
        ConditionOperator operator = operatorComboBox.getSelectedValue();
        wildcardsHintLabel.setEnabled(operator != null && operator.allowsWildcards());
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return textPatternTextField;
    }

    public SimpleNameFilterCondition getCondition() {
        condition.setOperator(operatorComboBox.getSelectedValue());
        condition.setPattern(textPatternTextField.getText().trim());
        return condition;
    }

    public ConditionJoinType getJoinType() {
        return joinTypeComboBox.getSelectedValue();
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }
}
