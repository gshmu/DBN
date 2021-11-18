package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsDeltaResult;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.ui.model.ParserDiagnosticsTableModel;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class ParserDiagnosticsDetailsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JBScrollPane diagnosticsTableScrollPane;

    private final DBNTable<ParserDiagnosticsTableModel> diagnosticsTable;

    public ParserDiagnosticsDetailsForm(@NotNull ParserDiagnosticsForm parent) {
        super(parent);

        diagnosticsTable = new ParserDiagnosticsTable(this, ParserDiagnosticsTableModel.EMPTY);
        diagnosticsTable.accommodateColumnsSize();
        diagnosticsTableScrollPane.setViewportView(diagnosticsTable);
        diagnosticsTableScrollPane.getViewport().setBackground(diagnosticsTable.getBackground());
    }


    public ParserDiagnosticsDeltaResult renderDeltaResult(@Nullable ParserDiagnosticsResult previous, @NotNull ParserDiagnosticsResult current) {
        ParserDiagnosticsDeltaResult deltaResult = current.delta(previous);
        ParserDiagnosticsTableModel tableModel = new ParserDiagnosticsTableModel(deltaResult);
        diagnosticsTable.setModel(tableModel);
        diagnosticsTable.accommodateColumnsSize();
        return deltaResult;
    }


    @Override

    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
