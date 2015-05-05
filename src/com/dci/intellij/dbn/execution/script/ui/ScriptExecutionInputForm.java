package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionInputForm extends DBNFormImpl<ScriptExecutionInputDialog>{
    private JPanel headerPanel;
    private DBNComboBox<ConnectionHandler> connectionComboBox;
    private DBNComboBox<DBSchema> schemaComboBox;
    private JPanel mainPanel;

    private DBNHeaderForm headerForm;

    public ScriptExecutionInputForm(@NotNull final ScriptExecutionInputDialog parentComponent, @NotNull VirtualFile sourceFile, @Nullable ConnectionHandler connectionHandler, @Nullable DBSchema schema) {
        super(parentComponent);

        String headerTitle = sourceFile.getPath();
        Icon headerIcon = sourceFile.getFileType().getIcon();
        Color headerColor = null;
        if (connectionHandler != null) {
            headerColor = connectionHandler.getEnvironmentType().getColor();
            schemaComboBox.setValues(connectionHandler.getObjectBundle().getSchemas());
            schemaComboBox.setSelectedValue(CommonUtil.nvl(schema, connectionHandler.getUserSchema()));
            parentComponent.setConnection(connectionHandler);
            parentComponent.setSchema(schema);
        }

        headerForm = new DBNHeaderForm(headerTitle, headerIcon, headerColor);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        connectionComboBox.setValues(connectionManager.getConnectionHandlers());
        connectionComboBox.setSelectedValue(connectionHandler);
        connectionComboBox.addListener(new ValueSelectorListener<ConnectionHandler>() {
            @Override
            public void selectionChanged(ConnectionHandler oldValue, ConnectionHandler newValue) {
                parentComponent.setConnection(newValue);
                schemaComboBox.setValues(newValue.getObjectBundle().getSchemas());
                schemaComboBox.setSelectedValue(newValue.getUserSchema());
                headerForm.setBackground(newValue.getEnvironmentType().getColor());
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
