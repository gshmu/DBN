package com.dci.intellij.dbn.execution.compiler.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.execution.compiler.CompileType;
import com.dci.intellij.dbn.execution.compiler.options.CompilerSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class CompilerSettingsForm extends ConfigurationEditorForm<CompilerSettings> {
    private JPanel mainPanel;
    private JComboBox compileTypeComboBox;
    private JRadioButton showAlwaysRadioButton;
    private JRadioButton showWhenInvalidRadioButton;

    public CompilerSettingsForm(CompilerSettings settings) {
        super(settings);
        compileTypeComboBox.addItem(CompileType.NORMAL);
        compileTypeComboBox.addItem(CompileType.DEBUG);
        compileTypeComboBox.addItem(CompileType.KEEP);
        compileTypeComboBox.addItem(CompileType.ASK);

        updateBorderTitleForeground(mainPanel);
        compileTypeComboBox.setRenderer(new ColoredListCellRenderer() {
            protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
                CompileType compileType = (CompileType) value;
                setIcon(compileType.getIcon());
                append(compileType.getDisplayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });

        resetChanges();

        registerComponent(compileTypeComboBox);
        registerComponent(showAlwaysRadioButton);
        registerComponent(showWhenInvalidRadioButton);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        CompilerSettings settings = getConfiguration();
        settings.setCompileType((CompileType) compileTypeComboBox.getSelectedItem());
        settings.setAlwaysShowCompilerControls(showAlwaysRadioButton.isSelected());
    }

    public void resetChanges() {
        CompilerSettings settings = getConfiguration();
        compileTypeComboBox.setSelectedItem(settings.getCompileType());
        if (settings.alwaysShowCompilerControls())
            showAlwaysRadioButton.setSelected(true); else
            showWhenInvalidRadioButton.setSelected(true);
    }
}
