package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.database.DatabaseExecutionInterface;
import com.dci.intellij.dbn.execution.common.options.ExecutionEngineSettings;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionInputForm extends DBNFormImpl<ScriptExecutionInputDialog>{
    private JPanel headerPanel;
    private DBNComboBox<ConnectionHandler> connectionComboBox;
    private DBNComboBox<DBSchema> schemaComboBox;
    private JPanel mainPanel;
    private DBNComboBox<CmdLineInterface> cmdLineExecutableComboBox;

    private DBNHeaderForm headerForm;

    public ScriptExecutionInputForm(@NotNull final ScriptExecutionInputDialog parentComponent, @NotNull VirtualFile sourceFile, @Nullable ConnectionHandler connectionHandler, @Nullable DBSchema schema) {
        super(parentComponent);

        String headerTitle = sourceFile.getPath();
        Icon headerIcon = sourceFile.getFileType().getIcon();
        if (sourceFile instanceof DBVirtualFile) {
            DBVirtualFile databaseVirtualFile = (DBVirtualFile) sourceFile;
            headerIcon = databaseVirtualFile.getIcon();
        }

        headerForm = new DBNHeaderForm(headerTitle, headerIcon, null);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        if (connectionHandler != null) {
            selectConnection(connectionHandler);
        } else {
            cmdLineExecutableComboBox.setEnabled(false);
        }

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        connectionComboBox.setEnabled(sourceFile.isInLocalFileSystem());
        connectionComboBox.setValues(connectionManager.getConnectionHandlers());
        connectionComboBox.setSelectedValue(connectionHandler);
        connectionComboBox.addListener(new ValueSelectorListener<ConnectionHandler>() {
            @Override
            public void selectionChanged(ConnectionHandler oldValue, ConnectionHandler newValue) {
                selectConnection(newValue);
            }
        });
        schemaComboBox.addListener(new ValueSelectorListener<DBSchema>() {
            @Override
            public void selectionChanged(DBSchema oldValue, DBSchema newValue) {
                parentComponent.setSchema(newValue);
            }
        });
        schemaComboBox.withValueDescriptions(false);
    }

    private void selectConnection(@Nullable ConnectionHandler connectionHandler) {
        if (connectionHandler != null) {
            ScriptExecutionInputDialog parentComponent = getParentComponent();
            DBSchema userSchema = connectionHandler.getUserSchema();
            parentComponent.setConnection(connectionHandler);
            parentComponent.setSchema(userSchema);
            schemaComboBox.setValues(connectionHandler.getObjectBundle().getSchemas());
            schemaComboBox.setSelectedValue(userSchema);
            headerForm.setBackground(connectionHandler.getEnvironmentType().getColor());

            cmdLineExecutableComboBox.clearValues();
            DatabaseExecutionInterface executionInterface = connectionHandler.getInterfaceProvider().getDatabaseExecutionInterface();
            CmdLineInterface defaultCmdLineInterface = executionInterface.getDefaultCmdLineInterface();
            if (defaultCmdLineInterface != null) {
                cmdLineExecutableComboBox.addValue(defaultCmdLineInterface);
            }
            ExecutionEngineSettings executionEngineSettings = ExecutionEngineSettings.getInstance(connectionHandler.getProject());
            CmdLineInterfaceBundle commandLineInterfaces = executionEngineSettings.getScriptExecutionSettings().getCommandLineInterfaces();
            List<CmdLineInterface> interfaces = commandLineInterfaces.getInterfaces(connectionHandler.getDatabaseType());
            cmdLineExecutableComboBox.addValues(interfaces);
            cmdLineExecutableComboBox.setEnabled(true);
        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
