package com.dci.intellij.dbn.object.properties.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventAdapter;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public class ObjectPropertiesForm extends DBNFormImpl<DBNForm> {
    private JPanel mainPanel;
    private JLabel objectLabel;
    private JLabel objectTypeLabel;
    private JTable objectPropertiesTable;
    private JScrollPane objectPropertiesScrollPane;
    private JPanel closeActionPanel;
    private DBObject object;

    public ObjectPropertiesForm(DBNForm parentForm) {
        super(parentForm);
        //ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        //closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);
        objectPropertiesTable.setRowSelectionAllowed(false);
        objectPropertiesTable.setCellSelectionEnabled(true);
        objectPropertiesScrollPane.getViewport().setBackground(objectPropertiesTable.getBackground());
        objectTypeLabel.setText("Object properties:");
        objectLabel.setText("(no object selected)");

        EventUtil.subscribe(getProject(), this, BrowserTreeEventListener.TOPIC, browserTreeEventListener);
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    private BrowserTreeEventListener browserTreeEventListener = new BrowserTreeEventAdapter() {
        @Override
        public void selectionChanged() {
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(getProject());
            if (browserManager.getShowObjectProperties().value()) {
                DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
                if (activeBrowserTree != null) {
                    BrowserTreeNode treeNode = activeBrowserTree.getSelectedNode();
                    if (treeNode instanceof DBObject) {
                        DBObject object = (DBObject) treeNode;
                        setObject(object);
                    }
                }
            }
        }
    };

    public DBObject getObject() {
        return object;
    }

    public void setObject(@NotNull DBObject object) {
        if (!object.equals(this.object)) {
            this.object = object;

            Project project = object.getProject();
            BackgroundTask.invoke(project,
                    instructions("Rendering object properties", TaskInstruction.BACKGROUNDED),
                    (task, progress) -> {
                        ObjectPropertiesTableModel tableModel = new ObjectPropertiesTableModel(object.getPresentableProperties());
                        Disposer.register(ObjectPropertiesForm.this, tableModel);

                        SimpleLaterInvocator.invokeNonModal(() -> {
                            objectLabel.setText(object.getName());
                            objectLabel.setIcon(object.getIcon());
                            objectTypeLabel.setText(NamingUtil.capitalize(object.getTypeName()) + ":");


                            ObjectPropertiesTableModel oldTableModel = (ObjectPropertiesTableModel) objectPropertiesTable.getModel();
                            objectPropertiesTable.setModel(tableModel);
                            ((DBNTable) objectPropertiesTable).accommodateColumnsSize();

                            GUIUtil.repaint(mainPanel);
                            Disposer.dispose(oldTableModel);
                        });
                    });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        object = null;
    }

    private void createUIComponents() {
        objectPropertiesTable = new ObjectPropertiesTable(null, new ObjectPropertiesTableModel());
        objectPropertiesTable.getTableHeader().setReorderingAllowed(false);
        Disposer.register(this, (Disposable) objectPropertiesTable);
    }
}
