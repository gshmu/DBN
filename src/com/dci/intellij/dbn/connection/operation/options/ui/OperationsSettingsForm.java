package com.dci.intellij.dbn.connection.operation.options.ui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.connection.operation.options.OperationSettings;

public class OperationsSettingsForm extends CompositeConfigurationEditorForm<OperationSettings> {
    private JPanel mainPanel;
    private JPanel transactionSettingsPanel;

    public OperationsSettingsForm(OperationSettings settings) {
        super(settings);
        transactionSettingsPanel.add(settings.getTransactionManagerSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    public JPanel getComponent() {
        return mainPanel;
    }
}
