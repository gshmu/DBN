package com.dci.intellij.dbn.object.dependency.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.project.Project;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class ObjectDependencyTreeForm extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;

    private DBObjectRef<DBSchemaObject> objectRef;

    public ObjectDependencyTreeForm(Project project, DBSchemaObject schemaObject) {
        this.objectRef = DBObjectRef.from(schemaObject);
        DBNHeaderForm headerForm = new DBNHeaderForm(schemaObject);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
