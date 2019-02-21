package com.dci.intellij.dbn.debugger.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.debugger.options.DebuggerSettings;
import com.dci.intellij.dbn.debugger.options.DebuggerTypeOption;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DebuggerSettingsForm extends ConfigurationEditorForm<DebuggerSettings> {
    private JPanel mainPanel;
    private JComboBox<DebuggerTypeOption> debuggerTypeComboBox;
    private JCheckBox useGenericRunnersCheckBox;
    private JPanel genericRunnersHintPanel;

    public DebuggerSettingsForm(DebuggerSettings settings) {
        super(settings);

        initComboBox(debuggerTypeComboBox,
                DebuggerTypeOption.JDWP,
                DebuggerTypeOption.JDBC,
                DebuggerTypeOption.ASK);

/*
        String genericRunnersHintText = "NOTE: Using generic runners prevents creating a run configuration for each method that is being debugged. ";
        DBNHintForm hintForm = new DBNHintForm(genericRunnersHintText, MessageType.INFO, false);
        genericRunnersHintPanel.add(hintForm.getComponent());
*/


        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DebuggerSettings settings = getConfiguration();

        settings.getDebuggerType().set(getSelection(debuggerTypeComboBox));
        settings.setUseGenericRunners(useGenericRunnersCheckBox.isSelected());
    }

    @Override
    public void resetFormChanges() {
        DebuggerSettings settings = getConfiguration();
        setSelection(debuggerTypeComboBox, settings.getDebuggerType().get());
        useGenericRunnersCheckBox.setSelected(settings.isUseGenericRunners());
    }
}
