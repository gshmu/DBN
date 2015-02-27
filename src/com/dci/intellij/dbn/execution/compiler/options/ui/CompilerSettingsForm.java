package com.dci.intellij.dbn.execution.compiler.options.ui;

import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<CompileTypeOption> compileTypeComboBox;
    private DBNComboBox<CompileDependenciesOption> compileDependenciesComboBox;
    private JRadioButton showAlwaysRadioButton;
    private JRadioButton showWhenInvalidRadioButton;

    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);
        compileTypeComboBox.setValues(
                CompileTypeOption.ASK,
                CompileTypeOption.KEEP,
                CompileTypeOption.NORMAL,
                CompileTypeOption.DEBUG);

        compileDependenciesComboBox.setValues(
                CompileDependenciesOption.ASK,
                CompileDependenciesOption.YES,
                CompileDependenciesOption.NO);


        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CompilerSettings settings = getConfiguration();
        settings.setCompileTypeOption(compileTypeComboBox.getSelectedValue());
        settings.setCompileDependenciesOption(compileDependenciesComboBox.getSelectedValue());
        settings.setAlwaysShowCompilerControls(showAlwaysRadioButton.isSelected());
    }

    public void resetFormChanges() {
        CompilerSettings settings = getConfiguration();
        compileTypeComboBox.setSelectedValue(settings.getCompileTypeOption());
        compileDependenciesComboBox.setSelectedValue(settings.getCompileDependenciesOption());
        if (settings.alwaysShowCompilerControls())
            showAlwaysRadioButton.setSelected(true); else
            showWhenInvalidRadioButton.setSelected(true);
    }
}
