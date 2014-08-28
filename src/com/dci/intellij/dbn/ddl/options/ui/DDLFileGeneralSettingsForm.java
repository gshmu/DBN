package com.dci.intellij.dbn.ddl.options.ui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DDLFileGeneralSettingsForm extends ConfigurationEditorForm<DDLFileGeneralSettings> {
    private JPanel mainPanel;
    private JTextField statementPostfixTextField;
    private JCheckBox lookupDDLFilesCheckBox;
    private JCheckBox createDDLFileCheckBox;
    private JCheckBox useQualifiedObjectNamesCheckBox;
    private JCheckBox prepareDropIfExistsCheckBox;

    public DDLFileGeneralSettingsForm(DDLFileGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DDLFileGeneralSettings settings = getConfiguration();
        settings.getStatementPostfix().applyChanges(statementPostfixTextField);
        settings.getLookupDDLFilesEnabled().applyChanges(lookupDDLFilesCheckBox);
        settings.getCreateDDLFilesEnabled().applyChanges(createDDLFileCheckBox);
        settings.getUseQualifiedObjectNames().applyChanges(useQualifiedObjectNamesCheckBox);
        settings.getMakeScriptsRerunnable().applyChanges(prepareDropIfExistsCheckBox);
    }

    public void resetChanges() {
        DDLFileGeneralSettings settings = getConfiguration();
        settings.getStatementPostfix().resetChanges(statementPostfixTextField);
        settings.getLookupDDLFilesEnabled().resetChanges(lookupDDLFilesCheckBox);
        settings.getCreateDDLFilesEnabled().resetChanges(createDDLFileCheckBox);
        settings.getUseQualifiedObjectNames().resetChanges(useQualifiedObjectNamesCheckBox);
        settings.getMakeScriptsRerunnable().resetChanges(prepareDropIfExistsCheckBox);
    }
}
