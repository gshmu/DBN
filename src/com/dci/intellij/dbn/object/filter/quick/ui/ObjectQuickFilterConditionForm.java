package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.ComboBoxSelectionKeyListener;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterCondition;
import com.dci.intellij.dbn.object.filter.quick.action.DeleteQuickFilterConditionAction;
import com.dci.intellij.dbn.object.filter.quick.action.EnableDisableQuickFilterConditionAction;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.DocumentAdapter;

public class ObjectQuickFilterConditionForm extends DBNFormImpl<ObjectQuickFilterForm> {
    private JPanel mainPanel;
    private JTextField patternTextField;
    private JLabel objectNameLabel;
    private DBNComboBox<ConditionOperator> operatorComboBox;
    private JPanel actionsPanel;

    private ObjectQuickFilterCondition condition;

    public ObjectQuickFilterConditionForm(ObjectQuickFilterForm parentComponent, @NotNull final ObjectQuickFilterCondition condition) {
        super(parentComponent);
        this.condition = condition;

        DBObjectType objectType = condition.getFilter().getObjectType();
        objectNameLabel.setIcon(objectType.getIcon());
        objectNameLabel.setText(objectType.getName().toUpperCase() + "_NAME");

        operatorComboBox.setValues(ConditionOperator.values());;
        patternTextField.setText(condition.getPattern());
        operatorComboBox.setSelectedValue(condition.getOperator());
        operatorComboBox.addListener(new ValueSelectorListener<ConditionOperator>() {
            @Override
            public void selectionChanged(ConditionOperator oldValue, ConditionOperator newValue) {
                condition.setOperator(newValue);
            }
        });

        patternTextField.setToolTipText("<html>press <b>Up/Down</b> keys to change the operator</html>");
        patternTextField.addKeyListener(ComboBoxSelectionKeyListener.create(operatorComboBox, false));
        patternTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                condition.setPattern(patternTextField.getText().trim());
            }
        });

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.SimpleFilter.Condition", true,
                new EnableDisableQuickFilterConditionAction(this),
                new DeleteQuickFilterConditionAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);

    }

    public JComponent getPreferredFocusedComponent() {
        return patternTextField;
    }

    public ObjectQuickFilterCondition getCondition() {
        condition.setOperator(operatorComboBox.getSelectedValue());
        condition.setPattern(patternTextField.getText().trim());
        return condition;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void dispose() {
        super.dispose();
        condition = null;
    }

    public void remove() {
        getParentComponent().removeConditionPanel(condition);
    }

    public boolean isActive() {
        return condition.isActive();
    }

    public void setActive(boolean active) {
        condition.setActive(active);
    }
}
