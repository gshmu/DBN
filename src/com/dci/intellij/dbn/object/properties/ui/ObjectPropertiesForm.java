package com.dci.intellij.dbn.object.properties.ui;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventAdapter;
import com.dci.intellij.dbn.browser.model.BrowserTreeEventListener;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.thread.Progress;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ObjectPropertiesForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JLabel objectLabel;
    private JLabel objectTypeLabel;
    private JTable objectPropertiesTable;
    private JScrollPane objectPropertiesScrollPane;
    private JPanel closeActionPanel;
    private DBObjectRef<?> object;

    public ObjectPropertiesForm(DBNForm parent) {
        super(parent);
        //ActionToolbar objectPropertiesActionToolbar = ActionUtil.createActionToolbar("", true, "DBNavigator.ActionGroup.Browser.ObjectProperties");
        //closeActionPanel.add(objectPropertiesActionToolbar.getComponent(), BorderLayout.CENTER);
        objectPropertiesTable.setRowSelectionAllowed(false);
        objectPropertiesTable.setCellSelectionEnabled(true);
        objectPropertiesScrollPane.getViewport().setBackground(objectPropertiesTable.getBackground());
        objectTypeLabel.setText("Object properties:");
        objectLabel.setText("(no object selected)");

        subscribe(getProject(), this, BrowserTreeEventListener.TOPIC, browserTreeEventListener);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    private final BrowserTreeEventListener browserTreeEventListener = new BrowserTreeEventAdapter() {
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
        return DBObjectRef.get(object);
    }

    public void setObject(@NotNull DBObject object) {
        DBObject localObject = getObject();
        if (!object.equals(localObject)) {
            this.object = DBObjectRef.of(object);

            Project project = object.getProject();
            Progress.background(project, "Rendering object properties", false,
                    (progress) -> {
                        ObjectPropertiesTableModel tableModel = new ObjectPropertiesTableModel(object.getPresentableProperties());
                        Disposer.register(ObjectPropertiesForm.this, tableModel);

                        Dispatch.run(() -> {
                            objectLabel.setText(object.getName());
                            objectLabel.setIcon(object.getIcon());
                            objectTypeLabel.setText(NamingUtil.capitalize(object.getTypeName()) + ":");


                            ObjectPropertiesTableModel oldTableModel = (ObjectPropertiesTableModel) objectPropertiesTable.getModel();
                            objectPropertiesTable.setModel(tableModel);
                            ((DBNTable<?>) objectPropertiesTable).accommodateColumnsSize();

                            GUIUtil.repaint(mainPanel);
                            SafeDisposer.dispose(oldTableModel);
                        });
                    });
        }
    }

    private void createUIComponents() {
        objectPropertiesTable = new ObjectPropertiesTable(this, new ObjectPropertiesTableModel());
        objectPropertiesTable.getTableHeader().setReorderingAllowed(false);
        Disposer.register(this, (Disposable) objectPropertiesTable);
    }
}
