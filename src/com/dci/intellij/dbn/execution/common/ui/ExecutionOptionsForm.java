package com.dci.intellij.dbn.execution.common.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.AutoCommitLabel;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionOption;
import com.dci.intellij.dbn.execution.ExecutionOptions;
import com.dci.intellij.dbn.execution.LocalExecutionInput;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutionOptionsForm extends DBNFormImpl<DisposableProjectComponent> {
    private JPanel mainPanel;
    private JPanel timeoutPanel;
    private JLabel connectionLabel;
    private JLabel targetSchemaLabel;
    private JLabel targetSessionLabel;
    private JCheckBox commitCheckBox;
    private JCheckBox reuseVariablesCheckBox;
    private JCheckBox enableLoggingCheckBox;
    private DBNComboBox<SchemaId> targetSchemaComboBox;
    private DBNComboBox<DatabaseSession> targetSessionComboBox;
    private AutoCommitLabel autoCommitLabel;

    private LocalExecutionInput executionInput;
    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();
    private DBDebuggerType debuggerType;

    public ExecutionOptionsForm(DBNForm parent, LocalExecutionInput executionInput, @NotNull DBDebuggerType debuggerType) {
        super(parent);
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;

        ConnectionHandler connectionHandler = Failsafe.get(executionInput.getConnectionHandler());

        if (isSchemaSelectionAllowed()) {
            //ActionToolbar actionToolbar = ActionUtil.createActionToolbar("", true, new SetExecutionSchemaComboBoxAction(executionInput));

            targetSchemaComboBox.setValues(connectionHandler.getSchemaIds());
            targetSchemaComboBox.setSelectedValue(executionInput.getTargetSchemaId());
            targetSchemaComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
            targetSchemaComboBox.addActionListener(actionListener);
            targetSchemaLabel.setVisible(false);
        } else {
            targetSchemaComboBox.setVisible(false);
            targetSchemaLabel.setVisible(true);
            SchemaId targetSchema = executionInput.getTargetSchemaId();
            if (targetSchema == null) {
                targetSessionLabel.setText("No schema selected");
                targetSessionLabel.setIcon(Icons.DBO_SCHEMA);
            } else {
                targetSchemaLabel.setText(targetSchema.id());
                targetSchemaLabel.setIcon(Icons.DBO_SCHEMA);
            }
        }

        DatabaseSessionBundle sessionBundle = connectionHandler.getSessionBundle();
        SessionId targetSessionId = executionInput.getTargetSessionId();
        if (isSessionSelectionAllowed()) {
            DatabaseSession targetSession = sessionBundle.getSession(targetSessionId);
            List<DatabaseSession> sessions = sessionBundle.getSessions(ConnectionType.MAIN, ConnectionType.POOL, ConnectionType.SESSION);

            targetSessionComboBox.setValues(sessions);
            targetSessionComboBox.setSelectedValue(targetSession);
            targetSessionComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
            targetSessionComboBox.addActionListener(actionListener);
            targetSessionLabel.setVisible(false);
        } else {
            targetSessionComboBox.setVisible(false);
            targetSessionLabel.setVisible(true);
            targetSessionId = debuggerType == DBDebuggerType.NONE ? targetSessionId : SessionId.DEBUG;
            DatabaseSession targetSession = sessionBundle.getSession(targetSessionId);
            targetSessionLabel.setText(targetSession.getName());
            targetSessionLabel.setIcon(targetSession.getIcon());
        }

        connectionLabel.setText(connectionHandler.getPresentableText());
        connectionLabel.setIcon(connectionHandler.getIcon());
        autoCommitLabel.init(getProject(), null, connectionHandler, targetSessionId);

        ExecutionOptions options = executionInput.getOptions();
        commitCheckBox.setSelected(options.is(ExecutionOption.COMMIT_AFTER_EXECUTION));
        commitCheckBox.setEnabled(!connectionHandler.isAutoCommit());

        commitCheckBox.addActionListener(actionListener);

        if (DatabaseFeature.DATABASE_LOGGING.isSupported(connectionHandler) && executionInput.isDatabaseLogProducer()) {
            enableLoggingCheckBox.setEnabled(!debuggerType.isDebug());
            enableLoggingCheckBox.setSelected(!debuggerType.isDebug() && options.is(ExecutionOption.ENABLE_LOGGING));
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

    @NotNull
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
        options.set(ExecutionOption.COMMIT_AFTER_EXECUTION, commitCheckBox.isSelected());
        options.set(ExecutionOption.ENABLE_LOGGING, enableLoggingCheckBox.isSelected());

        if (isSchemaSelectionAllowed()) {
            SchemaId schema = targetSchemaComboBox.getSelectedValue();
            executionInput.setTargetSchemaId(schema);
        }

        if (isSessionSelectionAllowed()) {
            DatabaseSession targetSession = targetSessionComboBox.getSelectedValue();
            executionInput.setTargetSession(targetSession);
        }
    }

    private boolean isSchemaSelectionAllowed() {
        return getExecutionInput().isSchemaSelectionAllowed();
    }

    private boolean isSessionSelectionAllowed() {
        return getExecutionInput().isSessionSelectionAllowed() && debuggerType == DBDebuggerType.NONE;
    }

    @Deprecated
    public void touch() {
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
        commitCheckBox.setSelected(!commitCheckBox.isSelected());
    }

    public LocalExecutionInput getExecutionInput() {
        return Failsafe.get(executionInput);
    }

    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = getExecutionInput().getConnectionHandler();
        return Failsafe.get(connectionHandler);
    }

    private ActionListener actionListener = e -> {
        if (changeListeners != null) {
            for (ChangeListener changeListener : changeListeners) {
                changeListener.stateChanged(new ChangeEvent(this));
            }
        }
    };

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }
}
