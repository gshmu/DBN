package com.dci.intellij.dbn.execution.script.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.ScriptExecutionManager;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;

public class ScriptExecutionInputForm extends DBNFormImpl<ScriptExecutionInputDialog>{
    private JPanel headerPanel;
    private DBNComboBox<ConnectionHandler> connectionComboBox;
    private DBNComboBox<DBSchema> schemaComboBox;
    private JPanel mainPanel;
    private DBNComboBox<CmdLineInterface> cmdLineExecutableComboBox;
    private JTextArea hintTextArea;

    private DBNHeaderForm headerForm;

    public ScriptExecutionInputForm(@NotNull final ScriptExecutionInputDialog parentComponent, @NotNull VirtualFile sourceFile, @Nullable ConnectionHandler connectionHandler) {
        super(parentComponent);

        String headerTitle = sourceFile.getPath();
        Icon headerIcon = sourceFile.getFileType().getIcon();
        if (sourceFile instanceof DBVirtualFile) {
            DBVirtualFile databaseVirtualFile = (DBVirtualFile) sourceFile;
            headerIcon = databaseVirtualFile.getIcon();
        }

        headerForm = new DBNHeaderForm(headerTitle, headerIcon, null);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        hintTextArea.setText(
                "Script execution engine uses the Command-Line Interface executable supplied with your database client.\n" +
                "You can use default Command-Line Interface resolver (which tries to locate the executable using the \n" +
                "\"path\" environment variables) or point to a specific Command-Line executable.\n\n" +
                "Database client interfaces are configurable in DBN Settings > Execution Engine > Script Execution.\n");
        hintTextArea.setBackground(mainPanel.getBackground());
        hintTextArea.setFont(mainPanel.getFont());

        if (connectionHandler != null) {
            selectConnection(connectionHandler);
        } else {
            cmdLineExecutableComboBox.setEnabled(false);
        }
        cmdLineExecutableComboBox.setOptions(ValueSelectorOption.HIDE_ICON);

        ConnectionManager connectionManager = ConnectionManager.getInstance(getProject());
        connectionComboBox.setOptions(ValueSelectorOption.HIDE_DESCRIPTION);
        connectionComboBox.setEnabled(sourceFile.isInLocalFileSystem());
        connectionComboBox.setValues(connectionManager.getConnectionHandlers());
        connectionComboBox.setSelectedValue(connectionHandler);
        connectionComboBox.addListener(new ValueSelectorListener<ConnectionHandler>() {
            @Override
            public void selectionChanged(ConnectionHandler oldValue, ConnectionHandler newValue) {
                selectConnection(newValue);
            }
        });
        schemaComboBox.setOptions(ValueSelectorOption.HIDE_DESCRIPTION);
        schemaComboBox.addListener(new ValueSelectorListener<DBSchema>() {
            @Override
            public void selectionChanged(DBSchema oldValue, DBSchema newValue) {
                parentComponent.setSchema(newValue);
            }
        });

        cmdLineExecutableComboBox.addListener(new ValueSelectorListener<CmdLineInterface>() {
            @Override
            public void selectionChanged(CmdLineInterface oldValue, CmdLineInterface newValue) {
                getParentComponent().setCmdLineInterface(newValue);
            }
        });
    }

    private void selectConnection(@Nullable ConnectionHandler connectionHandler) {
        if (connectionHandler != null) {
            ScriptExecutionInputDialog parentComponent = getParentComponent();
            DBSchema userSchema = connectionHandler.getUserSchema();
            schemaComboBox.setValues(connectionHandler.getObjectBundle().getSchemas());
            schemaComboBox.setSelectedValue(userSchema);
            headerForm.setBackground(connectionHandler.getEnvironmentType().getColor());

            DatabaseType databaseType = connectionHandler.getDatabaseType();
            ScriptExecutionManager scriptExecutionManager = ScriptExecutionManager.getInstance(getProject());
            List<CmdLineInterface> interfaces = scriptExecutionManager.getAvailableInterfaces(databaseType);
            cmdLineExecutableComboBox.clearValues();
            cmdLineExecutableComboBox.addValues(interfaces);
            cmdLineExecutableComboBox.setEnabled(true);

            CmdLineInterface cmdLineInterface = scriptExecutionManager.getRecentInterface(databaseType);
            if (cmdLineInterface != null) {
                cmdLineExecutableComboBox.setSelectedValue(cmdLineInterface);

            }

            parentComponent.setConnection(connectionHandler);
            parentComponent.setSchema(userSchema);
            parentComponent.setCmdLineInterface(cmdLineInterface);
        }
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
