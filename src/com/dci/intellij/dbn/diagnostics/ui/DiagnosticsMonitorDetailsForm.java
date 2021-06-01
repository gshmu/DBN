package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class DiagnosticsMonitorDetailsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel headerPanel;

    private final ConnectionHandlerRef connectionHandler;

    public DiagnosticsMonitorDetailsForm(@Nullable DiagnosticsMonitorForm parent, ConnectionHandler connectionHandler) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
