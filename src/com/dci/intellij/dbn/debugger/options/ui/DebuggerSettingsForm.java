package com.dci.intellij.dbn.debugger.options.ui;

import javax.swing.JPanel;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.debugger.DBDebuggerType;
import com.dci.intellij.dbn.debugger.options.DebuggerSettings;
import com.intellij.openapi.options.ConfigurationException;

public class DebuggerSettingsForm extends ConfigurationEditorForm<DebuggerSettings> {
    private JPanel mainPanel;
    private DBNComboBox<DBDebuggerType> debuggerTypeComboBox;

    public DebuggerSettingsForm(DebuggerSettings settings) {
        super(settings);

        debuggerTypeComboBox.setValues(
                DBDebuggerType.JDBC, DBDebuggerType.JDWP);

        updateBorderTitleForeground(mainPanel);
        resetFormChanges();

        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        DebuggerSettings settings = getConfiguration();
        settings.setDebuggerType(debuggerTypeComboBox.getSelectedValue());
    }

    public void resetFormChanges() {
        DebuggerSettings settings = getConfiguration();
        debuggerTypeComboBox.setSelectedValue(settings.getDebuggerType());
    }
}
