package com.dci.intellij.dbn.execution.compiler.options.ui;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.compiler.CompileDependenciesOption;
import com.dci.intellij.dbn.execution.compiler.CompileTypeOption;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private JComboBox compileTypeComboBox;
    private JComboBox compileDependenciesComboBox;
    private JRadioButton showAlwaysRadioButton;
    private JRadioButton showWhenInvalidRadioButton;

    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);
        compileTypeComboBox.addItem(CompileTypeOption.NORMAL);
        compileTypeComboBox.addItem(CompileTypeOption.DEBUG);
        compileTypeComboBox.addItem(CompileTypeOption.KEEP);
        compileTypeComboBox.addItem(CompileTypeOption.ASK);

        compileDependenciesComboBox.addItem(CompileDependenciesOption.YES);
        compileDependenciesComboBox.addItem(CompileDependenciesOption.NO);
        compileDependenciesComboBox.addItem(CompileDependenciesOption.ASK);


        updateBorderTitleForeground(mainPanel);
        compileTypeComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                CompileTypeOption compileType = (CompileTypeOption) value;
                setIcon(compileType.getIcon());
                append(compileType.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });

        compileDependenciesComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                CompileDependenciesOption compileDependenciesOption = (CompileDependenciesOption) value;
                append(compileDependenciesOption.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });

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
