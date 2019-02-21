package com.dci.intellij.dbn.object.filter.quick.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.filter.ConditionOperator;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilter;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterCondition;
import com.dci.intellij.dbn.object.filter.quick.ObjectQuickFilterManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectQuickFilterForm extends DBNFormImpl<ObjectQuickFilterDialog> {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel conditionsPanel;
    private JPanel actionsPanel;
    private JPanel hintPanel;

    private List<ObjectQuickFilterConditionForm> conditionForms = new ArrayList<ObjectQuickFilterConditionForm>();
    private DBObjectList objectList;
    private ObjectQuickFilter filter;

    ObjectQuickFilterForm(@NotNull ObjectQuickFilterDialog parent, DBObjectList objectList) {
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
/*            ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
            ObjectQuickFilterCondition condition = filter.addNewCondition(quickFilterManager.getLastUsedOperator());
            addConditionPanel(condition);*/
        }

        Filter filter = objectList.getConfigFilter();
        if (filter != null) {
            String hintText = "NOTE: This actions is filtered according to connection \"Filter\" settings. Any additional condition will narrow down the already filtered actions." ;
            DBNHintForm hintForm = new DBNHintForm(hintText, null, true);
            hintPanel.add(hintForm.getComponent());
        }

        actionsPanel.add(new NewFilterSelector(this.filter), BorderLayout.CENTER);

        Icon headerIcon = Icons.DATASET_FILTER;
        ConnectionHandler connectionHandler = objectList.getConnectionHandler();
        GenericDatabaseElement parentElement = objectList.getParentElement();
        String headerText = "[" + connectionHandler.getName() + "] " +
                (parentElement instanceof DBSchema ? (parentElement.getName() + " - ") : "") +
                NamingUtil.capitalizeWords(objectList.getObjectType().getListName()) + " filter";
        Color headerBackground = connectionHandler.getEnvironmentType().getColor();
        DBNHeaderForm headerForm = new DBNHeaderForm(headerText, headerIcon, headerBackground, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    void updateJoinTypeComponents() {
        for (ObjectQuickFilterConditionForm conditionForm : conditionForms) {
            conditionForm.updateJoinTypeComponent();
        }
    }

    private void addConditionPanel(ObjectQuickFilterCondition condition) {
        ObjectQuickFilterConditionForm conditionForm = new ObjectQuickFilterConditionForm(this, condition);
        conditionsPanel.add(conditionForm.getComponent());
        conditionsPanel.setBorder(new CompoundBorder(Borders.BOTTOM_LINE_BORDER, JBUI.Borders.emptyBottom(4)));
        GUIUtil.repaint(conditionsPanel);

        conditionForms.add(conditionForm);
        Disposer.register(this, conditionForm);
        updateJoinTypeComponents();

    }

    void removeConditionPanel(ObjectQuickFilterCondition condition) {
        filter.removeCondition(condition);
        for (ObjectQuickFilterConditionForm conditionForm : conditionForms) {
            if (conditionForm.getCondition() == condition) {
                conditionForms.remove(conditionForm);
                conditionsPanel.remove(conditionForm.getComponent());
                Disposer.dispose(conditionForm);

                break;
            }
        }
        int conditionsCount = filter.getConditions().size();
        conditionsPanel.setBorder(conditionsCount > 0 ? new CompoundBorder(Borders.BOTTOM_LINE_BORDER, JBUI.Borders.emptyBottom(4)) : null);
        GUIUtil.repaint(conditionsPanel);
        updateJoinTypeComponents();
    }

    private class NewFilterSelector extends ValueSelector<ConditionOperator> {
        @Override
        public String getOptionDisplayName(ConditionOperator value) {
            return super.getOptionDisplayName(value);
        }

        NewFilterSelector(final ObjectQuickFilter filter) {
            super(PlatformIcons.ADD_ICON, "Add Name Condition", null, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener((oldValue, newValue) -> {
                ObjectQuickFilterManager quickFilterManager = ObjectQuickFilterManager.getInstance(getProject());
                quickFilterManager.setLastUsedOperator(newValue);
                ObjectQuickFilterCondition condition = filter.addNewCondition(newValue);
                addConditionPanel(condition);
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

    @NotNull
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
