package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.action.GroupPopupAction;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.dci.intellij.dbn.execution.TargetConnectionOption;
import com.dci.intellij.dbn.execution.common.options.ExecutionTimeoutSettings;
import com.dci.intellij.dbn.execution.common.options.TimeoutSettingsListener;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

public abstract class ExecutionOptionsForm extends DBNFormImpl{
    private JTextField executionTimeoutTextField;
    private JPanel mainPanel;
    private JPanel actionsPanel;
    private JLabel hintLabel;
    private DBNComboBox<TargetConnectionOption> targetConnectionComboBox;

    private boolean hasErrors;
    private transient int timeout;

    private ExecutionInput executionInput;
    private DBDebuggerType debuggerType;

    public ExecutionOptionsForm(final ExecutionInput executionInput, final DBDebuggerType debuggerType) {
        this.executionInput = executionInput;
        this.debuggerType = debuggerType;

        timeout = getInputTimeout();
        executionTimeoutTextField.setText(String.valueOf(timeout));
        executionTimeoutTextField.setForeground(timeout == getSettingsTimeout() ?
                UIUtil.getLabelDisabledForeground() :
                UIUtil.getTextFieldForeground());

        targetConnectionComboBox.setValues(
                TargetConnectionOption.MAIN,
                TargetConnectionOption.POOL);


        executionTimeoutTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String text = executionTimeoutTextField.getText();
                try {
                    timeout = Integer.parseInt(text);
                    executionTimeoutTextField.setForeground(timeout == getSettingsTimeout() ?
                            UIUtil.getLabelDisabledForeground() :
                            UIUtil.getTextFieldForeground());

                    if (debuggerType.isDebug())
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
                new SettingsAction());

        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    private int getInputTimeout() {
        return debuggerType.isDebug() ?
                    executionInput.getDebugExecutionTimeout() :
                    executionInput.getExecutionTimeout();
    }

    private int getSettingsTimeout() {
        ExecutionTimeoutSettings timeoutSettings = executionInput.getExecutionTimeoutSettings();
        return debuggerType.isDebug() ?
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

    public class SettingsAction extends GroupPopupAction {
        public SettingsAction() {
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
            presentation.setEnabled(!hasErrors && timeout != getSettingsTimeout());
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

            boolean settingsChanged = debuggerType.isDebug() ?
                        timeoutSettings.setDebugExecutionTimeout(timeout) :
                        timeoutSettings.setExecutionTimeout(timeout);

            if (settingsChanged) {
                EventUtil.notify(getProject(), TimeoutSettingsListener.TOPIC).settingsChanged(executionInput.getExecutionTarget());
            }
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
