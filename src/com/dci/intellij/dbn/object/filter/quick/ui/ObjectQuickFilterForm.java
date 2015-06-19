package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterCondition;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.PlatformIcons;

public class ObjectQuickFilterForm extends DBNFormImpl<ObjectQuickFilterDialog> {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel conditionsPanel;
    private JPanel actionsPanel;

    private List<ObjectQuickFilterConditionForm> conditionForms = new ArrayList<ObjectQuickFilterConditionForm>();
    private DBObjectList objectList;
    private ObjectQuickFilter filter;

    public ObjectQuickFilterForm(@NotNull ObjectQuickFilterDialog parent, DBObjectList objectList) {
        super(parent);
        this.objectList = objectList;
        conditionsPanel.setLayout(new BoxLayout(conditionsPanel, BoxLayout.Y_AXIS));
        filter = objectList.getQuickFilter();
        if (filter == null) {
            filter = new ObjectQuickFilter(objectList.getObjectType());
        } else {
            filter = filter.clone();
        }
        List<ObjectQuickFilterCondition> conditions = filter.getConditions();
        if (conditions.size() > 0) {
            for (ObjectQuickFilterCondition condition : conditions) {
                addConditionPanel(condition);
            }
        } else {
            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            ObjectQuickFilterCondition condition = filter.addNewCondition(quickFilterManager.getLastUsedOperator());
            addConditionPanel(condition);
        }
        actionsPanel.add(new NewFilterSelector(filter), BorderLayout.CENTER);
    }

    private void addConditionPanel(ObjectQuickFilterCondition condition) {
        ObjectQuickFilterConditionForm conditionForm = new ObjectQuickFilterConditionForm(this, condition);
        conditionsPanel.add(conditionForm.getComponent());
        conditionsPanel.revalidate();
        conditionsPanel.repaint();

        conditionForms.add(conditionForm);
        Disposer.register(this, conditionForm);

    }

    public void removeConditionPanel(ObjectQuickFilterCondition condition) {
        filter.removeCondition(condition);
        for (ObjectQuickFilterConditionForm conditionForm : conditionForms) {
            if (conditionForm.getCondition() == condition) {
                conditionForms.remove(conditionForm);
                conditionsPanel.remove(conditionForm.getComponent());
                Disposer.dispose(conditionForm);

                break;
            }
        }
        conditionsPanel.revalidate();
        conditionsPanel.repaint();
    }

    private class NewFilterSelector extends ValueSelector<ConditionOperator> {
        public NewFilterSelector(final ObjectQuickFilter filter) {
            super(PlatformIcons.ADD_ICON, "Add Condition", null, false, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener(new ValueSelectorListener<ConditionOperator>() {
                @Override
                public void selectionChanged(ConditionOperator oldValue, ConditionOperator newValue) {
                    ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
                    quickFilterManager.setLastUsedOperator(newValue);
                    ObjectQuickFilterCondition condition = filter.addNewCondition(newValue);
                    addConditionPanel(condition);
                }
            });
        }

        @Override
        public List<ConditionOperator> loadValues() {
            return Arrays.asList(ConditionOperator.values());
        }
    }

    public ObjectQuickFilter getFilter() {
        return filter;
    }

    public DBObjectList getObjectList() {
        return objectList;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
        conditionForms.clear();
    }
}
