package com.dci.intellij.dbn.debugger.common.settings;

import com.dci.intellij.dbn.debugger.common.settings.ui.DBProgramDebuggerSettingsForm;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;

import javax.swing.*;

public class DBProgramDebuggerConfigurable implements Configurable {
    @Nls
    public String getDisplayName() {
        return "Database";
    }

    public Icon getIcon() {
        return null;
    }

    public String getHelpTopic() {
        return null;
    }

    public JComponent createComponent() {
        return new DBProgramDebuggerSettingsForm().getComponent();
    }

    public boolean isModified() {
        return false;
    }

    public void apply() throws ConfigurationException {
    }

    public void reset() {
    }

    public void disposeUIResources() {
    }
}
