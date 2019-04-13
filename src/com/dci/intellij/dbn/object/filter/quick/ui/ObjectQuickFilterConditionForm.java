package com.dci.intellij.dbn.object.filter.quick.ui;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionJoinType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterCondition;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.dci.intellij.dbn.object.filter.quick.action.DeleteQuickFilterConditionAction;
import com.dci.intellij.dbn.object.filter.quick.action.EnableDisableQuickFilterConditionAction;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class ObjectQuickFilterConditionForm extends DBNFormImpl<ObjectQuickFilterForm> {
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JLabel objectNameLabel;
    private JTextField patternTextField;
    private DBNComboBox<ConditionOperator> operatorComboBox;
    private DBNComboBox<ConditionJoinType> joinTypeComboBox;

    private ObjectQuickFilterCondition condition;

    ObjectQuickFilterConditionForm(final ObjectQuickFilterForm parentComponent, @NotNull final ObjectQuickFilterCondition condition) {
        super(parentComponent);
        this.condition = condition;

        final ObjectQuickFilter filter = condition.getFilter();
        joinTypeComboBox.setValues(ConditionJoinType.values());
        joinTypeComboBox.addListener((oldValue, newValue) -> {
            if (condition.index() == 0) {
                filter.setJoinType(newValue);
                parentComponent.updateJoinTypeComponents();
            }
        });


        DBObjectType objectType = filter.getObjectType();
        objectNameLabel.setIcon(objectType.getIcon());
        objectNameLabel.setText(objectType.getName().toUpperCase() + " NAME");

        operatorComboBox.setValues(ConditionOperator.values());
        patternTextField.setText(condition.getPattern());
        operatorComboBox.setSelectedValue(condition.getOperator());
        operatorComboBox.addListener((oldValue, newValue) -> {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            quickFilterManager.setLastUsedOperator(newValue);
            condition.setOperator(newValue);
        });

        patternTextField.setToolTipText("<html>press <b>Up/Down</b> keys to change the operator</html>");
        patternTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorComboBox, false));
        patternTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                condition.setPattern(patternTextField.getText().trim());
            }
        });

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.SimpleFilter.Condition", true,
                new EnableDisableQuickFilterConditionAction(this),
                new DeleteQuickFilterConditionAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    void updateJoinTypeComponent() {
        joinTypeComboBox.setSelectedValue(condition.getFilter().getJoinType());
        int conditionsCount = condition.getFilter().getConditions().size();
        joinTypeComboBox.setEnabled(conditionsCount > 1 && condition.index() == 0);
        joinTypeComboBox.setVisible(conditionsCount > 1 && condition.index() < conditionsCount - 1);
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return patternTextField;
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @NotNull
    protected ObjectQuickFilterCondition getCondition() {
        return Failsafe.nn(condition);
    }

    public void remove() {
        ensureParentComponent().removeConditionPanel(condition);
    }

    public boolean isActive() {
        return getCondition().isActive();
    }

    public void setActive(boolean active) {
        getCondition().setActive(active);
    }
}
