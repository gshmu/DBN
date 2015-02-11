package com.dci.intellij.dbn.connection.transaction.options.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.option.InteractiveOption;
import com.dci.intellij.dbn.common.option.ui.InteractiveOptionComboBoxRenderer;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.connection.transaction.TransactionOption;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class TransactionManagerSettingsForm extends ConfigurationEditorForm<TransactionManagerSettings> {
    private JPanel mainPanel;
    private JComboBox<InteractiveOption> uncommittedChangesOnProjectCloseComboBox;
    private JComboBox<InteractiveOption> uncommittedChangesOnSwitchComboBox;
    private JComboBox<InteractiveOption> uncommittedChangesOnDisconnectComboBox;
    private JComboBox<InteractiveOption> multipleChangesOnCommitComboBox;
    private JComboBox<InteractiveOption> multipleChangesOnRollbackComboBox;

    public TransactionManagerSettingsForm(TransactionManagerSettings settings) {
        super(settings);

        updateBorderTitleForeground(mainPanel);
        uncommittedChangesOnProjectCloseComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        uncommittedChangesOnProjectCloseComboBox.addItem(TransactionOption.ASK);
        uncommittedChangesOnProjectCloseComboBox.addItem(TransactionOption.COMMIT);
        uncommittedChangesOnProjectCloseComboBox.addItem(TransactionOption.ROLLBACK);
        uncommittedChangesOnProjectCloseComboBox.addItem(TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnSwitchComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        uncommittedChangesOnSwitchComboBox.addItem(TransactionOption.ASK);
        uncommittedChangesOnSwitchComboBox.addItem(TransactionOption.COMMIT);
        uncommittedChangesOnSwitchComboBox.addItem(TransactionOption.ROLLBACK);
        uncommittedChangesOnSwitchComboBox.addItem(TransactionOption.REVIEW_CHANGES);

        uncommittedChangesOnDisconnectComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        uncommittedChangesOnDisconnectComboBox.addItem(TransactionOption.ASK);
        uncommittedChangesOnDisconnectComboBox.addItem(TransactionOption.COMMIT);
        uncommittedChangesOnDisconnectComboBox.addItem(TransactionOption.ROLLBACK);
        uncommittedChangesOnDisconnectComboBox.addItem(TransactionOption.REVIEW_CHANGES);

        multipleChangesOnCommitComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        multipleChangesOnCommitComboBox.addItem(TransactionOption.ASK);
        multipleChangesOnCommitComboBox.addItem(TransactionOption.COMMIT);
        multipleChangesOnCommitComboBox.addItem(TransactionOption.REVIEW_CHANGES);

        multipleChangesOnRollbackComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        multipleChangesOnRollbackComboBox.addItem(TransactionOption.ASK);
        multipleChangesOnRollbackComboBox.addItem(TransactionOption.COMMIT);
        multipleChangesOnRollbackComboBox.addItem(TransactionOption.REVIEW_CHANGES);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        TransactionManagerSettings settings = getConfiguration();
        settings.getCloseProjectOptionHandler().setSelectedOption((TransactionOption) uncommittedChangesOnProjectCloseComboBox.getSelectedItem());
        settings.getToggleAutoCommitOptionHandler().setSelectedOption((TransactionOption) uncommittedChangesOnSwitchComboBox.getSelectedItem());
        settings.getDisconnectOptionHandler().setSelectedOption((TransactionOption) uncommittedChangesOnDisconnectComboBox.getSelectedItem());
        settings.getCommitMultipleChangesOptionHandler().setSelectedOption((TransactionOption) multipleChangesOnCommitComboBox.getSelectedItem());
        settings.getRollbackMultipleChangesOptionHandler().setSelectedOption((TransactionOption) multipleChangesOnRollbackComboBox.getSelectedItem());
    }

    public void resetFormChanges() {
        TransactionManagerSettings settings = getConfiguration();
        uncommittedChangesOnProjectCloseComboBox.setSelectedItem(settings.getCloseProjectOptionHandler().getSelectedOption());
        uncommittedChangesOnSwitchComboBox.setSelectedItem(settings.getToggleAutoCommitOptionHandler().getSelectedOption());
        uncommittedChangesOnDisconnectComboBox.setSelectedItem(settings.getDisconnectOptionHandler().getSelectedOption());
        multipleChangesOnCommitComboBox.setSelectedItem(settings.getCommitMultipleChangesOptionHandler().getSelectedOption());
        multipleChangesOnRollbackComboBox.setSelectedItem(settings.getRollbackMultipleChangesOptionHandler().getSelectedOption());

    }
}
