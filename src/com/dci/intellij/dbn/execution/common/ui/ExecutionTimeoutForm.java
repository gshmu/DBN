package com.dci.intellij.dbn.execution.common.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.execution.ExecutionInput;
import com.intellij.ui.DocumentAdapter;

public abstract class ExecutionTimeoutForm extends DBNFormImpl{
    private JTextField executionTimeoutTextField;
    private JLabel errorLabel;
    private JPanel mainPanel;
    private JLabel executionTimeoutLabel;

    public ExecutionTimeoutForm(final ExecutionInput executionInput, final DBDebuggerType debuggerType) {
        errorLabel.setIcon(Icons.COMMON_ERROR);
        errorLabel.setVisible(false);

        executionTimeoutTextField.setText(String.valueOf(executionInput.getExecutionTimeout()));
        executionTimeoutTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                String text = executionTimeoutTextField.getText();
                try {
                    int timeout = Integer.parseInt(text);

                    if (debuggerType.isActive())
                        executionInput.setDebugExecutionTimeout(timeout); else
                        executionInput.setExecutionTimeout(timeout);
                    errorLabel.setVisible(false);
                    handleChange(false);
                } catch (NumberFormatException e1) {
                    errorLabel.setText("Timeout must be an integer");
                    errorLabel.setVisible(true);
                    handleChange(true);
                }
            }
        });
    }

    protected abstract void handleChange(boolean hasError);

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
