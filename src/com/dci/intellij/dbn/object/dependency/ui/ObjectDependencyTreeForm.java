package com.dci.intellij.dbn.object.dependency.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;

public class ObjectDependencyTreeForm extends DBNFormImpl<ObjectDependencyTreeDialog>{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;

    private DBObjectRef<DBSchemaObject> objectRef;

    public ObjectDependencyTreeForm(ObjectDependencyTreeDialog parentComponent, DBSchemaObject schemaObject) {
        super(parentComponent);
        this.objectRef = DBObjectRef.from(schemaObject);
        DBNHeaderForm headerForm = new DBNHeaderForm(schemaObject);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
