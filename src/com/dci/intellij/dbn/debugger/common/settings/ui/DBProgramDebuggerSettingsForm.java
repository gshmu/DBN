package com.dci.intellij.dbn.debugger.common.settings.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;

import javax.swing.*;

public class DBProgramDebuggerSettingsForm extends DBNFormImpl {
    private JPanel mainPanel;

    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
