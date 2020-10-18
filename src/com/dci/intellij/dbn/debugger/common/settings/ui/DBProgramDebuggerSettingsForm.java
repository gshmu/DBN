package com.dci.intellij.dbn.debugger.common.settings.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class DBProgramDebuggerSettingsForm extends DBNFormImpl {
    private JPanel mainPanel;

    public DBProgramDebuggerSettingsForm() {
        super(null);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
