package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.diagnostics.ui.model.ConnectivityDiagnosticsTableModel;
import com.dci.intellij.dbn.diagnostics.ui.model.MetadataDiagnosticsTableModel;
import com.intellij.ui.GuiUtils;
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

    private final ConnectionHandlerRef connectionHandler;

    public DiagnosticsMonitorDetailsForm(@NotNull DiagnosticsMonitorForm parent, ConnectionHandler connectionHandler) {
        super(parent);
        this.connectionHandler = connectionHandler.getRef();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);

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

        GUIUtil.updateSplitterProportion(mainPanel, 0.5F);
   }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
