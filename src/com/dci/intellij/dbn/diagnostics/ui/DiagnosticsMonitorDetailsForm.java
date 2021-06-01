package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.diagnostics.ui.model.MetadataDiagnosticsTableModel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DiagnosticsMonitorDetailsForm extends DBNFormImpl {
    private final DBNTable<MetadataDiagnosticsTableModel> diagnosticsTable;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JLabel metadataLabel;
    private JBScrollPane diagnosticsTableScrollPane;

    private final ConnectionHandlerRef connectionHandler;

    public DiagnosticsMonitorDetailsForm(@NotNull DiagnosticsMonitorForm parent, ConnectionHandler connectionHandler) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);


        MetadataDiagnosticsTableModel diagnosticsTableModel = new MetadataDiagnosticsTableModel(connectionHandler);
        diagnosticsTable = new DiagnosticsTable<>(this, diagnosticsTableModel);
        diagnosticsTableScrollPane.setViewportView(diagnosticsTable);
        diagnosticsTableScrollPane.getViewport().setBackground(diagnosticsTable.getBackground());

    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
