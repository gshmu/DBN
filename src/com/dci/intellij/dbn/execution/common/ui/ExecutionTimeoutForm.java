package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.DocumentAdapter;

public abstract class ExecutionTimeoutForm extends DBNFormImpl{
    private JTextField executionTimeoutTextField;
    private JPanel mainPanel;
    private JLabel executionTimeoutLabel;
    private JPanel actionsPanel;
    private JLabel hintLabel;
    private boolean hasErrors;
    private transient int timeout;

    private ExecutionInput executionInput;
    private DBDebuggerType debuggerType;

    public ExecutionTimeoutForm(final ExecutionInput executionInput, final DBDebuggerType debuggerType) {
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;

        timeout = getInputTimeout();
        executionTimeoutTextField.setText(String.valueOf(timeout));

        executionTimeoutTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String text = executionTimeoutTextField.getText();
                try {
                    timeout = Integer.parseInt(text);

                    if (debuggerType.isActive())
                        executionInput.setDebugExecutionTimeout(timeout); else
                        executionInput.setExecutionTimeout(timeout);
                    hintLabel.setIcon(null);
                    hintLabel.setToolTipText(null);
                    hasErrors = false;
                    handleChange(false);
                } catch (NumberFormatException e1) {
                    //errorLabel.setText("Timeout must be an integer");
                    hintLabel.setIcon(Icons.COMMON_ERROR);
                    hintLabel.setToolTipText("Timeout must be an integer");
                    hasErrors = true;
                    handleChange(true);
                }
            }
        });

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.ExecutionTimeoutForm.Settings", true,
                new DatasetEditorOptionsAction());

        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    private int getInputTimeout() {
        return debuggerType.isActive() ?
                    executionInput.getDebugExecutionTimeout() :
                    executionInput.getExecutionTimeout();
    }

    private int getSettingsTimeout() {
        ExecutionTimeoutSettings timeoutSettings = executionInput.getExecutionTimeoutSettings();
        return debuggerType.isActive() ?
                timeoutSettings.getDebugExecutionTimeout() :
                timeoutSettings.getExecutionTimeout();
    }

    protected void handleChange(boolean hasError){};

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public class DatasetEditorOptionsAction extends GroupPopupAction {
        public DatasetEditorOptionsAction() {
            super("Settings", null, Icons.ACTION_OPTIONS);
        }
        @Override
        protected AnAction[] getActions(AnActionEvent e) {
            return new AnAction[]{
                    new SaveToSettingsAction(),
                    new ReloadDefaultAction()
            };
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setVisible(!hasErrors && timeout != getSettingsTimeout());
        }
    }

    class SaveToSettingsAction extends AnAction {
        public SaveToSettingsAction() {
            super("Save to Settings");
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            ExecutionTimeoutSettings timeoutSettings = executionInput.getExecutionTimeoutSettings();
            String text = executionTimeoutTextField.getText();
            int timeout = Integer.parseInt(text);

            if (debuggerType.isActive())
                timeoutSettings.setDebugExecutionTimeout(timeout); else
                timeoutSettings.setExecutionTimeout(timeout);
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(!hasErrors);
        }
    }

    class ReloadDefaultAction extends AnAction {

        public ReloadDefaultAction() {
            super("Reload from Settings");
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            int timeout = getSettingsTimeout();
            executionTimeoutTextField.setText(String.valueOf(timeout));
        }

        @Override
        public void update(AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabled(!hasErrors);
        }
    }

}
