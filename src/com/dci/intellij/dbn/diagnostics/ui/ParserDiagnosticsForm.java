package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsDeltaResult;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.dci.intellij.dbn.diagnostics.data.StateTransition;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

public class ParserDiagnosticsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel detailsPanel;
    private JList<ParserDiagnosticsResult> resultsList;
    private JLabel detailsLabel;
    private JLabel stateTransitionLabel;

    private final ParserDiagnosticsDetailsForm detailsForm;
    private final ParserDiagnosticsManager manager;

    public ParserDiagnosticsForm(@NotNull ParserDiagnosticsDialog dialog) {
        super(dialog);
        manager = ParserDiagnosticsManager.getInstance(ensureProject());
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, (float) 0.2);

        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        detailsForm = new ParserDiagnosticsDetailsForm(this);
        detailsPanel.add(detailsForm.getComponent(), BorderLayout.CENTER);
        detailsLabel.setText("No result selected");
        stateTransitionLabel.setText("");

        resultsList.addListSelectionListener(e -> {
            ParserDiagnosticsResult current = resultsList.getSelectedValue();
            if (current != null) {
                ParserDiagnosticsResult previous = manager.getPreviousResult(current);
                ParserDiagnosticsDeltaResult deltaResult = detailsForm.renderDeltaResult(previous, current);
                detailsLabel.setText(deltaResult.getName());

                StateTransition stateTransition = deltaResult.getStateTransition();
                stateTransitionLabel.setText(stateTransition.toString());
                stateTransitionLabel.setForeground(stateTransition.getColor());
                stateTransitionLabel.setFont(stateTransition.isBold() ?
                        UIUtil.getLabelFont().deriveFont(Font.BOLD) :
                        UIUtil.getLabelFont());
                dialog.updateButtons();
            }
        });

        resultsList.setCellRenderer(new ResultListCellRenderer());
        refreshResults();
    }

    public void refreshResults() {
        DefaultListModel<ParserDiagnosticsResult> model = new DefaultListModel<>();
        List<ParserDiagnosticsResult> history = manager.getResultHistory();
        for (ParserDiagnosticsResult result : history) {
            model.addElement(result);
        }

        resultsList.setModel(model);
    }

    private DefaultListModel<ParserDiagnosticsResult> getResultsModel() {
        return (DefaultListModel<ParserDiagnosticsResult>) resultsList.getModel();
    }

    @Nullable
    public ParserDiagnosticsResult selectedResult() {
        return resultsList.getSelectedValue();
    }

    public void selectResult(@Nullable ParserDiagnosticsResult result) {
        if (result != null) {
            resultsList.setSelectedValue(result, true);
        }
    }

    private static class ResultListCellRenderer extends ColoredListCellRenderer<ParserDiagnosticsResult> {
        @Override
        protected void customizeCellRenderer(@NotNull JList list, ParserDiagnosticsResult value, int index, boolean selected, boolean hasFocus) {
            append(value.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
