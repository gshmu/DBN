package com.dci.intellij.dbn.connection.transaction.options.ui;

import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.intellij.openapi.options.ConfigurationException;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class TransactionManagerSettingsForm extends ConfigurationEditorForm<TransactionManagerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<TransactionOption> uncommittedChangesOnProjectCloseComboBox;
    private DBNComboBox<TransactionOption> uncommittedChangesOnSwitchComboBox;
    private DBNComboBox<TransactionOption> uncommittedChangesOnDisconnectComboBox;
    private DBNComboBox<TransactionOption> multipleChangesOnCommitComboBox;
    private DBNComboBox<TransactionOption> multipleChangesOnRollbackComboBox;

    public TransactionManagerSettingsForm(TransactionManagerSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        uncommittedChangesOnProjectCloseComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnSwitchComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnDisconnectComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        multipleChangesOnCommitComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.COMMIT,
                TransactionOption.REVIEW_CHANGES);

        multipleChangesOnRollbackComboBox.setValues(
                TransactionOption.ASK,
                TransactionOption.ROLLBACK,
                TransactionOption.REVIEW_CHANGES);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        TransactionManagerSettings settings = getConfiguration();
        settings.getCloseProject().set(uncommittedChangesOnProjectCloseComboBox.getSelectedValue());
        settings.getToggleAutoCommit().set(uncommittedChangesOnSwitchComboBox.getSelectedValue());
        settings.getDisconnect().set(uncommittedChangesOnDisconnectComboBox.getSelectedValue());
        settings.getCommitMultipleChanges().set(multipleChangesOnCommitComboBox.getSelectedValue());
        settings.getRollbackMultipleChanges().set(multipleChangesOnRollbackComboBox.getSelectedValue());
    }

    public void resetFormChanges() {
        TransactionManagerSettings settings = getConfiguration();
        uncommittedChangesOnProjectCloseComboBox.setSelectedValue(settings.getCloseProject().get());
        uncommittedChangesOnSwitchComboBox.setSelectedValue(settings.getToggleAutoCommit().get());
        uncommittedChangesOnDisconnectComboBox.setSelectedValue(settings.getDisconnect().get());
        multipleChangesOnCommitComboBox.setSelectedValue(settings.getCommitMultipleChanges().get());
        multipleChangesOnRollbackComboBox.setSelectedValue(settings.getRollbackMultipleChanges().get());

    }
}
