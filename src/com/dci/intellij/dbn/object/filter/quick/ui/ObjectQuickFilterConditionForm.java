package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterCondition;

public class ObjectQuickFilterConditionForm extends DBNFormImpl<ObjectQuickFilterForm> {
    private JPanel mainPanel;
    private JTextField patternTextField;
    private JLabel objectNameLabel;
    private DBNComboBox<ConditionOperator> operatorComboBox;
    private DBNComboBox<ConditionJoinType> joinTypeComboBox;

    private ObjectQuickFilterCondition condition;

    public ObjectQuickFilterConditionForm(ObjectQuickFilterForm parentComponent, @NotNull ObjectQuickFilterCondition condition) {
        super(parentComponent);
        this.condition = condition;
        joinTypeComboBox.setValues(ConditionJoinType.values());
        joinTypeComboBox.setVisible(true);
        joinTypeComboBox.setSelectedValue(ConditionJoinType.AND);

        DBObjectType objectType = condition.getFilter().getObjectType();
        objectNameLabel.setIcon(objectType.getIcon());
        objectNameLabel.setText(objectType.getName().toUpperCase() + "_NAME");

        operatorComboBox.setValues(ConditionOperator.values());;
        patternTextField.setText(condition.getPattern());
        operatorComboBox.setSelectedValue(condition.getOperator());

        patternTextField.setToolTipText("<html>While editing, <br> " +
                "press <b>Up/Down</b> keys to change the operator</html>");
        patternTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorComboBox, false));
    }

    public JComponent getPreferredFocusedComponent() {
        return patternTextField;
    }

    public ObjectQuickFilterCondition getCondition() {
        condition.setOperator(operatorComboBox.getSelectedValue());
        condition.setPattern(patternTextField.getText().trim());
        return condition;
    }

    public ConditionJoinType getJoinType() {
        return joinTypeComboBox.getSelectedValue();
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        condition = null;
    }
}
