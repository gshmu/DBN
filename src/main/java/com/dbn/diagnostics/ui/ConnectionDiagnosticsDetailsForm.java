package com.dbn.diagnostics.ui;

import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.misc.DBNScrollPane;
import com.dbn.common.ui.tab.DBNTabbedPane;
import com.dbn.common.ui.table.DBNMutableTableModel;
import com.dbn.common.ui.table.DBNTable;
import com.dbn.connection.ConnectionHandler;
import com.dbn.diagnostics.ui.model.AbstractDiagnosticsTableModel;
import com.dbn.diagnostics.ui.model.ConnectivityDiagnosticsTableModel;
import com.dbn.diagnostics.ui.model.MetadataDiagnosticsTableModel2;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static com.dbn.common.dispose.Failsafe.nd;

public class ConnectionDiagnosticsDetailsForm extends DBNFormBase {
    private final DBNTable<AbstractDiagnosticsTableModel> metadataTable;
    private final DBNTable<AbstractDiagnosticsTableModel> connectivityTable;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel diagnosticsTabsPanel;
    private final DBNTabbedPane<DBNTable> diagnosticsTabs;

    public ConnectionDiagnosticsDetailsForm(@NotNull ConnectionDiagnosticsForm parent, ConnectionHandler connection) {
        super(parent);

        DBNHeaderForm headerForm = new DBNHeaderForm(this, connection).withEmptyBorder();
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        diagnosticsTabs = new DBNTabbedPane<>(this);
        diagnosticsTabsPanel.add(diagnosticsTabs, BorderLayout.CENTER);


        AbstractDiagnosticsTableModel metadataTableModel = new MetadataDiagnosticsTableModel2(connection);
        metadataTable = new DiagnosticsTable<>(this, metadataTableModel);
        metadataTable.getRowSorter().toggleSortOrder(0);
        addTab(metadataTable, "Metadata Interface");

        AbstractDiagnosticsTableModel connectivityTableModel = new ConnectivityDiagnosticsTableModel(connection);
        connectivityTable = new DiagnosticsTable<>(this, connectivityTableModel);
        connectivityTable.getRowSorter().toggleSortOrder(0);
        addTab(connectivityTable, "Database Connectivity");


        diagnosticsTabs.addTabsListener(i -> {
            ConnectionDiagnosticsForm parentForm = nd(getParentComponent());
            parentForm.setTabSelectionIndex(i);

        });
   }

    private void addTab(DBNTable component, String title) {
        JScrollPane scrollPane = new DBNScrollPane(component);
        diagnosticsTabs.addTab(title, scrollPane, component);
    }

    protected void selectTab(int tabIndex) {
        diagnosticsTabs.setSelectedIndex(tabIndex);
        DBNTable table = diagnosticsTabs.getContentAt(tabIndex);
        DBNMutableTableModel model = (DBNMutableTableModel) table.getModel();
        model.notifyRowChanges();
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
