package com.dci.intellij.dbn.execution.compiler.options.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dci.intellij.dbn.common.option.InteractiveOption;
import com.dci.intellij.dbn.common.option.ui.InteractiveOptionComboBoxRenderer;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private JComboBox<InteractiveOption> compileTypeComboBox;
    private JComboBox<InteractiveOption> compileDependenciesComboBox;
    private JRadioButton showAlwaysRadioButton;
    private JRadioButton showWhenInvalidRadioButton;

    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);
        compileTypeComboBox.addItem(CompileTypeOption.ASK);
        compileTypeComboBox.addItem(CompileTypeOption.KEEP);
        compileTypeComboBox.addItem(CompileTypeOption.NORMAL);
        compileTypeComboBox.addItem(CompileTypeOption.DEBUG);

        compileDependenciesComboBox.addItem(CompileDependenciesOption.ASK);
        compileDependenciesComboBox.addItem(CompileDependenciesOption.YES);
        compileDependenciesComboBox.addItem(CompileDependenciesOption.NO);


        updateBorderTitleForeground(mainPanel);
        compileTypeComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);
        compileDependenciesComboBox.setRenderer(InteractiveOptionComboBoxRenderer.INSTANCE);

        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CompilerSettings settings = getConfiguration();
        settings.setCompileTypeOption((CompileTypeOption) compileTypeComboBox.getSelectedItem());
        settings.setCompileDependenciesOption((CompileDependenciesOption) compileDependenciesComboBox.getSelectedItem());
        settings.setAlwaysShowCompilerControls(showAlwaysRadioButton.isSelected());
    }

    public void resetFormChanges() {
        CompilerSettings settings = getConfiguration();
        compileTypeComboBox.setSelectedItem(settings.getCompileTypeOption());
        compileDependenciesComboBox.setSelectedItem(settings.getCompileDependenciesOption());
        if (settings.alwaysShowCompilerControls())
            showAlwaysRadioButton.setSelected(true); else
            showWhenInvalidRadioButton.setSelected(true);
    }
}
