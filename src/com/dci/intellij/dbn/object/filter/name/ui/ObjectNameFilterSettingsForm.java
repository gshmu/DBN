package com.dci.intellij.dbn.object.filter.name.ui;

import com.dci.intellij.dbn.browser.options.ObjectFilterChangeListener;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.filter.name.FilterCondition;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilter;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterManager;
import com.dci.intellij.dbn.object.filter.name.ObjectNameFilterSettings;
import com.dci.intellij.dbn.object.filter.name.SimpleNameFilterCondition;
import com.dci.intellij.dbn.object.filter.name.action.AddConditionAction;
import com.dci.intellij.dbn.object.filter.name.action.CreateFilterAction;
import com.dci.intellij.dbn.object.filter.name.action.MoveConditionDownAction;
import com.dci.intellij.dbn.object.filter.name.action.MoveConditionUpAction;
import com.dci.intellij.dbn.object.filter.name.action.RemoveConditionAction;
import com.dci.intellij.dbn.object.filter.name.action.SwitchConditionJoinTypeAction;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class ObjectNameFilterSettingsForm extends ConfigurationEditorForm<ObjectNameFilterSettings> {
    private JPanel mainPanel;
    private JTree filtersTree;
    private JPanel actionsPanel;

    public ObjectNameFilterSettingsForm(ObjectNameFilterSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);

/*        ObjectNameFilter schemaFilter = new ObjectNameFilter(DBObjectType.SCHEMA, ConditionOperator.NOT_LIKE, "T%");

        schemaFilter.addCondition(ConditionOperator.LIKE, "AE9%");
        configuration.addFilter(schemaFilter);

        ObjectNameFilter tableFilter = new ObjectNameFilter(DBObjectType.TABLE, ConditionOperator.NOT_LIKE, "T%");
        SimpleFilterCondition filterX = new SimpleFilterCondition(ConditionOperator.NOT_LIKE, "ZZ_%");
        tableFilter.addCondition(filterX);
        filterX.joinCondition(ConditionOperator.EQUAL, "BLA");

        configuration.addFilter(tableFilter);*/

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.ObjectNameFilters.Setup", true,
                new CreateFilterAction(this),
                new AddConditionAction(this),
                new RemoveConditionAction(this),
                new SwitchConditionJoinTypeAction(this),
                new Separator(),
                new MoveConditionUpAction(this),
                new MoveConditionDownAction(this));
        actionsPanel.add(actionToolbar.getComponent());

        filtersTree.setCellRenderer(new FilterSettingsTreeCellRenderer());
        ObjectNameFilterSettings tableModel = configuration;

        filtersTree.setModel(tableModel);
        filtersTree.setShowsRootHandles(true);
        filtersTree.setRootVisible(false);

        for (ObjectNameFilter filter : tableModel.getFilters()) {
            filtersTree.expandPath(tableModel.createTreePath(filter));
        }

        filtersTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                    Object selection = getSelection();
                    if (selection instanceof SimpleNameFilterCondition) {
                        SimpleNameFilterCondition condition = (SimpleNameFilterCondition) selection;
                        getManager().editFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                }
            }
        });

        filtersTree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == 10) {  // ENTER
                    Object selection = getSelection();
                    if (selection instanceof SimpleNameFilterCondition) {
                        SimpleNameFilterCondition condition = (SimpleNameFilterCondition) selection;
                        getManager().editFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                } else if (e.getKeyChar() == 127) { //DEL
                    Object selection = getSelection();
                    if (selection instanceof FilterCondition) {
                        FilterCondition condition = (FilterCondition) selection;
                        getManager().removeFilterCondition(condition, ObjectNameFilterSettingsForm.this);
                    }
                }
            }
        });
    }

    private ObjectNameFilterManager getManager() {
        return ObjectNameFilterManager.getInstance(getConfiguration().getProject());
    }

    public Object getSelection() {
        TreePath selectionPath = filtersTree.getSelectionPath();
        return selectionPath == null ? null : selectionPath.getLastPathComponent();
    }


    public JTree getFiltersTree() {
        return filtersTree;
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        Set<DBObjectType> filterObjectTypes = new HashSet<>();
        ObjectNameFilterSettings configuration = getConfiguration();
        boolean notifyFilterListeners = configuration.isModified();

        // collect before after applying the changes
        filterObjectTypes.addAll(configuration.getFilteredObjectTypes());

        Element element = new Element("Temp");
        ObjectNameFilterSettings tempSettings = (ObjectNameFilterSettings) filtersTree.getModel();
        tempSettings.writeConfiguration(element);
        configuration.readConfiguration(element);
        // after applying the changes
        filterObjectTypes.addAll(configuration.getFilteredObjectTypes());

        Project project = configuration.getProject();
        SettingsChangeNotifier.register(() -> {
            if (notifyFilterListeners) {
                DBObjectType[] refreshObjectTypes = filterObjectTypes.toArray(new DBObjectType[0]);
                EventUtil.notify(project, ObjectFilterChangeListener.TOPIC,
                        (listener) -> listener.nameFiltersChanged(configuration.getConnectionId(), refreshObjectTypes));
            }
        });
    }

    @Override
    public void resetFormChanges() {}
}
