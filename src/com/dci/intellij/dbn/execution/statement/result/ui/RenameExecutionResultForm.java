package com.dci.intellij.dbn.execution.statement.result.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.execution.ExecutionManager;
import com.dci.intellij.dbn.execution.statement.result.StatementExecutionResult;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.awt.BorderLayout;
import java.util.Objects;

public class RenameExecutionResultForm extends DBNFormImpl{
    private JPanel headerPanel;
    private JPanel mainPanel;
    private JPanel hintPanel;
    private JLabel errorLabel;
    private JTextField resultNameTextField;
    private JCheckBox stickyCheckBox;

    RenameExecutionResultForm(RenameExecutionResultDialog parent, @NotNull StatementExecutionResult executionResult) {
        super(parent);
        errorLabel.setForeground(JBColor.RED);
        errorLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        errorLabel.setVisible(false);

        DBNHeaderForm headerForm = new DBNHeaderForm(this,
                "Execution result - " + executionResult.getName(),
                executionResult.getIcon(),
                executionResult.getConnectionHandler().getEnvironmentType().getColor());
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        String hint = "Use \"Sticky\" option to retain the name after the result is closed.";
        DBNHintForm hintForm = new DBNHintForm(this, hint, null, false);
        hintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);

        String resultName = executionResult.getName();
        resultNameTextField.setText(resultName);

        ExecutionManager executionManager = ExecutionManager.getInstance(ensureProject());
        stickyCheckBox.setSelected(executionManager.isRetainStickyNames());

        resultNameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                String errorText = null;
                String text = Strings.trim(resultNameTextField.getText());

                if (Strings.isEmpty(text)) {
                    errorText = "Result name must be specified";
                }

/*
                else if (consoleNames.contains(text)) {
                    errorText = "Console name already in use";
                }
*/


                errorLabel.setVisible(errorText != null);
                parent.getOKAction().setEnabled(errorText == null && (!Objects.equals(executionResult.getName(), text)));
                if (errorText != null) {
                    errorLabel.setText(errorText);
                }
            }
        });
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return resultNameTextField;
    }

    public String getResultName() {
        return resultNameTextField.getText();
    }

    public boolean isStickyResultName() {
        return stickyCheckBox.isSelected();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
