package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.*;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.util.Disposer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutionOptionsForm extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel timeoutPanel;
    private JPanel targetSchemaPanel;
    private JPanel targetSessionPanel;
    private JCheckBox commitCheckBox;
    private JCheckBox reuseVariablesCheckBox;
    private JCheckBox enableLoggingCheckBox;
    private AutoCommitLabel autoCommitLabel;
    private JLabel connectionLabel;
    private JLabel targetSchemaLabel;
    private JLabel targetSessionLabel;

    private LocalExecutionInput executionInput;
    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    public ExecutionOptionsForm(DBNForm parent, LocalExecutionInput executionInput, DBDebuggerType debuggerType) {
        super(parent);
        this.executionInput = executionInput;

        ConnectionHandler connectionHandler = FailsafeUtil.get(executionInput.getConnectionHandler());

        if (executionInput.isSchemaSelectionAllowed()) {
            //ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SetExecutionSchemaComboBoxAction(executionInput));
            targetSchemaPanel.add(new SchemaSelector(), BorderLayout.CENTER);
            targetSchemaLabel.setVisible(false);
        } else {
            targetSchemaLabel.setVisible(true);
            DBSchema targetSchema = executionInput.getTargetSchema();
            targetSchemaLabel.setText(targetSchema.getName());
            targetSchemaLabel.setIcon(targetSchema.getIcon());
        }
        connectionLabel.setText(connectionHandler.getPresentableText());
        connectionLabel.setIcon(connectionHandler.getIcon());
        autoCommitLabel.setConnectionHandler(connectionHandler);

        ExecutionOptions options = executionInput.getOptions();
        commitCheckBox.setSelected(options.isCommitAfterExecution());
        commitCheckBox.setEnabled(!connectionHandler.isAutoCommit());

        commitCheckBox.addActionListener(actionListener);

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler) && executionInput.isDatabaseLogProducer()) {
            enableLoggingCheckBox.setEnabled(!debuggerType.isDebug());
            enableLoggingCheckBox.setSelected(!debuggerType.isDebug() && options.isEnableLogging());
            DatabaseCompatibilityInterface compatibilityInterface = DatabaseCompatibilityInterface.getInstance(connectionHandler);
            String databaseLogName = compatibilityInterface == null ? null : compatibilityInterface.getDatabaseLogName();
            if (StringUtil.isNotEmpty(databaseLogName)) {
                enableLoggingCheckBox.setText("Enable logging (" + databaseLogName + ")");
            }
        } else{
            enableLoggingCheckBox.setVisible(false);
        }

        Disposer.register(this, autoCommitLabel);

        ExecutionTimeoutForm timeoutForm = new ExecutionTimeoutForm(executionInput, debuggerType) {
            @Override
            protected void handleChange(boolean hasError) {
                super.handleChange(hasError);
            }
        };

        timeoutPanel.add(timeoutForm.getComponent(), BorderLayout.CENTER);

        reuseVariablesCheckBox.setVisible(executionInput.hasExecutionVariables());
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public JCheckBox getReuseVariablesCheckBox() {
        return reuseVariablesCheckBox;
    }

    public void updateExecutionInput() {
        ExecutionOptions options = executionInput.getOptions();
        //options.setUsePoolConnection(usePoolConnectionCheckBox.isSelected());
        options.setCommitAfterExecution(commitCheckBox.isSelected());
        options.setEnableLogging(enableLoggingCheckBox.isSelected());
        //DBSchema schema = (DBSchema) schemaList.getSelectedValue();
        //executionInput.setExecutionSchema(schema);
    }

    @Deprecated
    public void touch() {
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
    }

    private class SchemaSelector extends ValueSelector<DBSchema> {
        public SchemaSelector() {
            super(Icons.DBO_SCHEMA, "Select Schema...", executionInput.getTargetSchema(), true, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener(new ValueSelectorListener<DBSchema>() {
                @Override
                public void selectionChanged(DBSchema oldValue, DBSchema newValue) {
                    executionInput.setTargetSchema(newValue);
                    notifyChangeListeners();
                }
            });
        }

        @Override
        public List<DBSchema> loadValues() {
            ConnectionHandler connectionHandler = executionInput.getConnectionHandler();
            return connectionHandler.getObjectBundle().getSchemas();
        }
    }

    private class SessionSelector extends ValueSelector<DatabaseSession> {
        public SessionSelector() {
            super(Icons.SESSION_CUSTOM, "Select Session...", getExecutionInput().getTargetSession(), true, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener(new ValueSelectorListener<DatabaseSession>() {
                @Override
                public void selectionChanged(DatabaseSession oldValue, DatabaseSession newValue) {
                    executionInput.setTargetSession(newValue);
                    notifyChangeListeners();
                }
            });
        }

        @Override
        public List<DatabaseSession> loadValues() {
            return getConnectionHandler().getSessionBundle().getSessions();
        }
    }

    public LocalExecutionInput getExecutionInput() {
        return FailsafeUtil.get(executionInput);
    }

    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = getExecutionInput().getConnectionHandler();
        return FailsafeUtil.get(connectionHandler);
    }

    private ActionListener actionListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            notifyChangeListeners();
        }
    };

    private void notifyChangeListeners() {
        if (changeListeners != null) {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void dispose() {
        super.dispose();
        changeListeners.clear();
        changeListeners = null;
        executionInput = null;
    }
}
