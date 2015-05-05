package com.dci.intellij.dbn.execution.script.ui;

import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;

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
        if (sourceFile instanceof DBVirtualFile) {
            DBVirtualFile databaseVirtualFile = (DBVirtualFile) sourceFile;
            headerIcon = databaseVirtualFile.getIcon();
        }
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
        connectionComboBox.setEnabled(sourceFile.isInLocalFileSystem());
        connectionComboBox.setValues(connectionManager.getConnectionHandlers());
        connectionComboBox.setSelectedValue(connectionHandler);
        connectionComboBox.addListener(new ValueSelectorListener<ConnectionHandler>() {
            @Override
            public void selectionChanged(ConnectionHandler oldValue, ConnectionHandler newValue) {
                DBSchema userSchema = newValue.getUserSchema();
                parentComponent.setConnection(newValue);
                parentComponent.setSchema(userSchema);
                schemaComboBox.setValues(newValue.getObjectBundle().getSchemas());
                schemaComboBox.setSelectedValue(userSchema);
                headerForm.setBackground(newValue.getEnvironmentType().getColor());
            }
        });
        schemaComboBox.addListener(new ValueSelectorListener<DBSchema>() {
            @Override
            public void selectionChanged(DBSchema oldValue, DBSchema newValue) {
                parentComponent.setSchema(newValue);
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
