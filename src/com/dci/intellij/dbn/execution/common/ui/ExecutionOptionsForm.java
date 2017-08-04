package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.util.Disposer;

public class ExecutionOptionsForm extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel timeoutPanel;
    private JPanel targetSchemaPanel;
    private JCheckBox usePoolConnectionCheckBox;
    private JCheckBox commitCheckBox;
    private JCheckBox reuseVariablesCheckBox;
    private JCheckBox enableLoggingCheckBox;
    private AutoCommitLabel autoCommitLabel;
    private JLabel connectionLabel;
    private JLabel targetSchemaLabel;

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

        commitCheckBox.setSelected(executionInput.isCommitAfterExecution());
        commitCheckBox.setEnabled(!connectionHandler.isAutoCommit());
        usePoolConnectionCheckBox.setSelected(executionInput.isUsePoolConnection());

        commitCheckBox.addActionListener(actionListener);
        usePoolConnectionCheckBox.addActionListener(actionListener);
        usePoolConnectionCheckBox.setEnabled(!debuggerType.isDebug());

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler) && executionInput.isDatabaseLogProducer()) {
            enableLoggingCheckBox.setEnabled(!debuggerType.isDebug());
            enableLoggingCheckBox.setSelected(!debuggerType.isDebug() && executionInput.isLoggingEnabled());
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
        executionInput.setUsePoolConnection(usePoolConnectionCheckBox.isSelected());
        executionInput.setCommitAfterExecution(commitCheckBox.isSelected());
        executionInput.setLoggingEnabled(enableLoggingCheckBox.isSelected());
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
