package com.dci.intellij.dbn.connection.transaction.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.connection.transaction.options.TransactionManagerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class TransactionManagerSettingsForm extends ConfigurationEditorForm<TransactionManagerSettings> {
    private JCheckBox showObjectNavigationGutterCheckBox;
    private JCheckBox specDeclarationGutterCheckBox;
    private JPanel mainPanel;

    public TransactionManagerSettingsForm(TransactionManagerSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        TransactionManagerSettings settings = getConfiguration();
        settings.setShowObjectsNavigationGutter(showObjectNavigationGutterCheckBox.isSelected());
        settings.setShowSpecDeclarationNavigationGutter(specDeclarationGutterCheckBox.isSelected());
    }

    public void resetFormChanges() {
        TransactionManagerSettings settings = getConfiguration();
        showObjectNavigationGutterCheckBox.setSelected(settings.isShowObjectsNavigationGutter());
        specDeclarationGutterCheckBox.setSelected(settings.isShowSpecDeclarationNavigationGutter());
    }
}
