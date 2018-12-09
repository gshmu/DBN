package com.dci.intellij.dbn.code.common.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleCase;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.getSelection;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.initComboBox;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.setSelection;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeStyleCaseSettingsForm extends ConfigurationEditorForm<CodeStyleCaseSettings> {
    private JPanel mainPanel;
    private JComboBox<CodeStyleCase> keywordCaseComboBox;
    private JComboBox<CodeStyleCase> functionCaseComboBox;
    private JComboBox<CodeStyleCase> parameterCaseComboBox;
    private JComboBox<CodeStyleCase> datatypeCaseComboBox;
    private JComboBox<CodeStyleCase> objectCaseComboBox;
    private JCheckBox enableCheckBox;

    public static final CodeStyleCase[] OBJECT_STYLE_CASES = new CodeStyleCase[]{
            CodeStyleCase.PRESERVE,
            CodeStyleCase.UPPER,
            CodeStyleCase.LOWER,
            CodeStyleCase.CAPITALIZED};

    public static final CodeStyleCase[] KEYWORD_STYLE_CASES = new CodeStyleCase[]{
            CodeStyleCase.UPPER,
            CodeStyleCase.LOWER,
            CodeStyleCase.CAPITALIZED};

    public CodeStyleCaseSettingsForm(CodeStyleCaseSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);

        initComboBox(keywordCaseComboBox, KEYWORD_STYLE_CASES);
        initComboBox(functionCaseComboBox, KEYWORD_STYLE_CASES);
        initComboBox(parameterCaseComboBox, KEYWORD_STYLE_CASES);
        initComboBox(datatypeCaseComboBox, KEYWORD_STYLE_CASES);
        initComboBox(objectCaseComboBox, OBJECT_STYLE_CASES);
        resetFormChanges();
        enableDisableOptions();

        registerComponent(mainPanel);
        enableCheckBox.addActionListener(e -> enableDisableOptions());



        //Shortcut[] basicShortcuts = KeyUtil.getShortcuts("ReformatCode");
        //enableCheckBox.setText("Use on reformat code (" + KeymapUtil.getShortcutsText(basicShortcuts) + ")");
    }

    private void enableDisableOptions() {
        boolean enabled = enableCheckBox.isSelected();
        keywordCaseComboBox.setEnabled(enabled);
        functionCaseComboBox.setEnabled(enabled);
        parameterCaseComboBox.setEnabled(enabled);
        datatypeCaseComboBox.setEnabled(enabled);
        objectCaseComboBox.setEnabled(enabled);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeStyleCaseSettings settings = getConfiguration();
        settings.getKeywordCaseOption().setStyleCase(getSelection(keywordCaseComboBox));
        settings.getFunctionCaseOption().setStyleCase(getSelection(functionCaseComboBox));
        settings.getParameterCaseOption().setStyleCase(getSelection(parameterCaseComboBox));
        settings.getDatatypeCaseOption().setStyleCase(getSelection(datatypeCaseComboBox));
        settings.getObjectCaseOption().setStyleCase(getSelection(objectCaseComboBox));
        settings.setEnabled(enableCheckBox.isSelected());
    }

    public void resetFormChanges() {
        CodeStyleCaseSettings settings = getConfiguration();
        setSelection(keywordCaseComboBox, settings.getKeywordCaseOption().getStyleCase());
        setSelection(functionCaseComboBox, settings.getFunctionCaseOption().getStyleCase());
        setSelection(parameterCaseComboBox, settings.getParameterCaseOption().getStyleCase());
        setSelection(datatypeCaseComboBox, settings.getDatatypeCaseOption().getStyleCase());
        setSelection(objectCaseComboBox, settings.getObjectCaseOption().getStyleCase());
        enableCheckBox.setSelected(settings.isEnabled());
    }
}
