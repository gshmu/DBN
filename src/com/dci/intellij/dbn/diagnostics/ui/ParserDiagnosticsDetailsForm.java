package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.action.ParserDiagnosticsFileTypeFilterAction;
import com.dci.intellij.dbn.diagnostics.action.ParserDiagnosticsStateFilterAction;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsDeltaResult;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.data.StateTransition;
import com.dci.intellij.dbn.diagnostics.ui.model.ParserDiagnosticsTableModel;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;

public class ParserDiagnosticsDetailsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JLabel detailsLabel;
    private JLabel stateTransitionLabel;
    private JPanel actionsPanel;
    private JBScrollPane diagnosticsTableScrollPane;

    private final DBNTable<ParserDiagnosticsTableModel> diagnosticsTable;
    private final ParserDiagnosticsManager manager;

    public ParserDiagnosticsDetailsForm(@NotNull ParserDiagnosticsForm parent) {
        super(parent);
        manager = ParserDiagnosticsManager.getInstance(ensureProject());

        diagnosticsTable = new ParserDiagnosticsTable(this, ParserDiagnosticsTableModel.EMPTY);
        diagnosticsTable.accommodateColumnsSize();
        diagnosticsTableScrollPane.setViewportView(diagnosticsTable);
        diagnosticsTableScrollPane.getViewport().setBackground(diagnosticsTable.getBackground());

        detailsLabel.setText("No result selected");
        stateTransitionLabel.setText("");


        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(actionsPanel,"", true,
                new ParserDiagnosticsStateFilterAction(this),
                new ParserDiagnosticsFileTypeFilterAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);
    }

    public void renderResult(@Nullable ParserDiagnosticsResult previous, @Nullable ParserDiagnosticsResult current) {
        ParserDiagnosticsDeltaResult deltaResult = current == null ? null : current.delta(previous);
        ParserDiagnosticsTableModel tableModel = new ParserDiagnosticsTableModel(deltaResult, manager.getResultFilter());
        diagnosticsTable.setModel(tableModel);
        diagnosticsTable.accommodateColumnsSize();

        detailsLabel.setText(deltaResult == null ? "" : deltaResult.getName());

        StateTransition stateTransition = deltaResult == null ? StateTransition.UNCHANGED : deltaResult.getFilter();
        StateTransition.Category category = stateTransition.getCategory();
        stateTransitionLabel.setText(previous == null ? "INITIAL" : stateTransition.name());
        stateTransitionLabel.setForeground(category.getColor());
        stateTransitionLabel.setFont(category.isBold() ?
                UIUtil.getLabelFont().deriveFont(Font.BOLD) :
                UIUtil.getLabelFont());
    }

    public void refreshResult() {
        ParserDiagnosticsTableModel model = diagnosticsTable.getModel();
        ParserDiagnosticsDeltaResult result = model.getResult();
        model = new ParserDiagnosticsTableModel(result, manager.getResultFilter());
        diagnosticsTable.setModel(model);
        //GUIUtil.repaint(diagnosticsTable);
    }

    public ParserDiagnosticsManager getManager() {
        return manager;
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
