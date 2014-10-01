package com.dci.intellij.dbn.code.common.style.options.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCase;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.intellij.openapi.options.ConfigurationException;

public class CodeStyleCaseSettingsForm extends ConfigurationEditorForm<CodeStyleCaseSettings> implements DBNForm {
    private JPanel mainPanel;
    private JComboBox keywordCaseComboBox;
    private JComboBox functionCaseComboBox;
    private JComboBox parameterCaseComboBox;
    private JComboBox datatypeCaseComboBox;
    private JComboBox objectCaseComboBox;
    private JCheckBox enableCheckBox;


    public CodeStyleCaseSettingsForm(CodeStyleCaseSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        keywordCaseComboBox.setModel(createDefaultModel());
        functionCaseComboBox.setModel(createDefaultModel());
        parameterCaseComboBox.setModel(createDefaultModel());
        datatypeCaseComboBox.setModel(createDefaultModel());
        objectCaseComboBox.setModel(createCustomModel());
        resetFormChanges();

        registerComponent(mainPanel);

        //Shortcut[] basicShortcuts = KeyUtil.getShortcuts("ReformatCode");
        //enableCheckBox.setText("Use on reformat code (" + KeymapUtil.getShortcutsText(basicShortcuts) + ")");
    }

    private DefaultComboBoxModel createCustomModel() {
        return new DefaultComboBoxModel(
                new CodeStyleCase[] {
                        CodeStyleCase.PRESERVE,
                        CodeStyleCase.UPPER,
                        CodeStyleCase.LOWER,
                        CodeStyleCase.CAPITALIZED});
    }

    private DefaultComboBoxModel createDefaultModel() {
        return new DefaultComboBoxModel(
                new CodeStyleCase[] {
                        CodeStyleCase.UPPER,
                        CodeStyleCase.LOWER,
                        CodeStyleCase.CAPITALIZED});
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeStyleCaseSettings settings = getConfiguration();
        settings.getKeywordCaseOption().setStyleCase((CodeStyleCase) keywordCaseComboBox.getSelectedItem());
        settings.getFunctionCaseOption().setStyleCase((CodeStyleCase) functionCaseComboBox.getSelectedItem());
        settings.getParameterCaseOption().setStyleCase((CodeStyleCase) parameterCaseComboBox.getSelectedItem());
        settings.getDatatypeCaseOption().setStyleCase((CodeStyleCase) datatypeCaseComboBox.getSelectedItem());
        settings.getObjectCaseOption().setStyleCase((CodeStyleCase) objectCaseComboBox.getSelectedItem());
        settings.setEnabled(enableCheckBox.isSelected());
    }

    public void resetFormChanges() {
        CodeStyleCaseSettings settings = getConfiguration();
        keywordCaseComboBox.setSelectedItem(settings.getKeywordCaseOption().getStyleCase());
        functionCaseComboBox.setSelectedItem(settings.getFunctionCaseOption().getStyleCase());
        parameterCaseComboBox.setSelectedItem(settings.getParameterCaseOption().getStyleCase());
        datatypeCaseComboBox.setSelectedItem(settings.getDatatypeCaseOption().getStyleCase());
        objectCaseComboBox.setSelectedItem(settings.getObjectCaseOption().getStyleCase());
        enableCheckBox.setSelected(settings.isEnabled());
    }
}
