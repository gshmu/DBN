package com.dci.intellij.dbn.object.factory.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.data.type.ui.DataTypeEditor;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.factory.MethodFactoryInput;
import com.dci.intellij.dbn.object.factory.ObjectFactoryInput;
import com.dci.intellij.dbn.object.factory.ui.common.ObjectFactoryInputForm;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class MethodFactoryInputForm extends ObjectFactoryInputForm<MethodFactoryInput> {
    private JPanel mainPanel;
    private JLabel connectionLabel;
    private JLabel schemaLabel;
    protected JTextField nameTextField;
    private JPanel returnArgumentPanel;
    private JPanel argumentListComponent;
    private JLabel returnArgumentIconLabel;
    JPanel returnArgumentDataTypeEditor;

    private ArgumentFactoryInputListPanel argumentListPanel;
    private DBSchema schema;

    public MethodFactoryInputForm(DBSchema schema, DBObjectType objectType, int index) {
        super(schema.getConnectionHandler(), objectType, index);
        this.schema = schema;
        connectionLabel.setText(getConnectionHandler().getName());
        connectionLabel.setIcon(getConnectionHandler().getIcon());

        schemaLabel.setText(schema.getName());
        schemaLabel.setIcon(schema.getIcon());

        returnArgumentPanel.setVisible(hasReturnArgument());
        argumentListPanel.createObjectPanel();
        argumentListPanel.createObjectPanel();
        argumentListPanel.createObjectPanel();

        returnArgumentIconLabel.setText(null);
        returnArgumentIconLabel.setIcon(Icons.DBO_ARGUMENT_OUT);
    }

    public MethodFactoryInput createFactoryInput(ObjectFactoryInput parent) {
        MethodFactoryInput methodFactoryInput = new MethodFactoryInput(schema, nameTextField.getText(), getObjectType(), getIndex());
        methodFactoryInput.setArguments(argumentListPanel.createFactoryInputs(methodFactoryInput));
        return methodFactoryInput;
    }

    public abstract boolean hasReturnArgument();

    private void createUIComponents() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseCompatibilityInterface compatibilityInterface = connectionHandler.getInterfaceProvider().getCompatibilityInterface();
        boolean enforceInArguments = hasReturnArgument() && !compatibilityInterface.supportsFeature(DatabaseFeature.FUNCTION_OUT_ARGUMENTS);
        argumentListPanel = new ArgumentFactoryInputListPanel(connectionHandler, enforceInArguments);
        argumentListComponent = argumentListPanel.getComponent();
        returnArgumentDataTypeEditor = new DataTypeEditor(getConnectionHandler());
    }

    public void focus() {
        nameTextField.requestFocus();
    }

    public JPanel getComponent() {
        return mainPanel;
    }
}
