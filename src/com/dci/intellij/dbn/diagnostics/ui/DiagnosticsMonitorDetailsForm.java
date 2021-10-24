package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.diagnostics.ui.model.ConnectivityDiagnosticsTableModel;
import com.dci.intellij.dbn.diagnostics.ui.model.MetadataDiagnosticsTableModel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DiagnosticsMonitorDetailsForm extends DBNFormImpl {
    private final DBNTable<MetadataDiagnosticsTableModel> metadataDiagnosticsTable;
    private final DBNTable<ConnectivityDiagnosticsTableModel> connectivityDiagnosticsTable;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JBScrollPane metadataDiagnosticsScrollPane;
    private JBScrollPane connectivityDiagnosticsScrollPane;
    private JTabbedPane diagnosticsTabbedPane;

    public DiagnosticsMonitorDetailsForm(@NotNull DiagnosticsMonitorForm parent, ConnectionHandler connectionHandler) {
        super(parent);

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connectionHandler);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        MetadataDiagnosticsTableModel metadataDiagnosticsTableModel = new MetadataDiagnosticsTableModel(connectionHandler);
        metadataDiagnosticsTable = new DiagnosticsTable<>(this, metadataDiagnosticsTableModel);
        metadataDiagnosticsScrollPane.setViewportView(metadataDiagnosticsTable);
        metadataDiagnosticsScrollPane.getViewport().setBackground(metadataDiagnosticsTable.getBackground());

        ConnectivityDiagnosticsTableModel connectivityDiagnosticsTableModel = new ConnectivityDiagnosticsTableModel(connectionHandler);
        connectivityDiagnosticsTable = new DiagnosticsTable<>(this, connectivityDiagnosticsTableModel);
        connectivityDiagnosticsScrollPane.setViewportView(connectivityDiagnosticsTable);
        connectivityDiagnosticsScrollPane.getViewport().setBackground(connectivityDiagnosticsTable.getBackground());

        diagnosticsTabbedPane.addChangeListener(e -> {
            JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
            int selectedIndex = tabbedPane.getSelectedIndex();
            DiagnosticsMonitorForm parent1 = parent();
            parent1.setTabSelectionIndex(selectedIndex);
        });
   }

   protected void selectTab(int tabIndex) {
        diagnosticsTabbedPane.setSelectedIndex(tabIndex);
   }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
