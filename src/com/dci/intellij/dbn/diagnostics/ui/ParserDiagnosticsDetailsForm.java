package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsDeltaResult;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ParserDiagnosticsDetailsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JBScrollPane diagnosticsTableScrollPane;

    private DBNTable<ParserDiagnosticsTableModel> diagnosticsTable;

    public ParserDiagnosticsDetailsForm(@NotNull ParserDiagnosticsForm parent) {
        super(parent);
    }


    public void renderDeltaResult(ParserDiagnosticsResult previous, ParserDiagnosticsResult current) {
        ParserDiagnosticsDeltaResult deltaResult = current.delta(previous);
        ParserDiagnosticsTableModel tableModel = new ParserDiagnosticsTableModel(deltaResult);

        if (diagnosticsTable == null) {
            diagnosticsTable = new ParserDiagnosticsTable(this, tableModel);
            diagnosticsTable.accommodateColumnsSize();
            diagnosticsTableScrollPane.setViewportView(diagnosticsTable);
            diagnosticsTableScrollPane.getViewport().setBackground(diagnosticsTable.getBackground());
        } else {
            diagnosticsTable.setModel(tableModel);
            diagnosticsTable.accommodateColumnsSize();
        }

    }


    @Override

    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
