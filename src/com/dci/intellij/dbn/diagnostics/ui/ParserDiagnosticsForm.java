package com.dci.intellij.dbn.diagnostics.ui;

import com.dci.intellij.dbn.common.locale.Formatter;
import com.dci.intellij.dbn.common.locale.options.RegionalSettings;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.diagnostics.ParserDiagnosticsManager;
import com.dci.intellij.dbn.diagnostics.data.ParserDiagnosticsResult;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.GuiUtils;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.List;

public class ParserDiagnosticsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JPanel detailsPanel;
    private JList<ParserDiagnosticsResult> resultsList;

    private final ParserDiagnosticsDetailsForm detailsForm;
    private final ParserDiagnosticsManager manager;
    private final Formatter formatter;

    public ParserDiagnosticsForm(@NotNull ParserDiagnosticsDialog dialog) {
        super(dialog);
        manager = ParserDiagnosticsManager.getInstance(ensureProject());
        formatter = RegionalSettings.getInstance(ensureProject()).getBaseFormatter();
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        GUIUtil.updateSplitterProportion(mainPanel, 0.2F);

        mainPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        detailsForm = new ParserDiagnosticsDetailsForm(this);
        detailsPanel.add(detailsForm.getComponent(), BorderLayout.CENTER);

        resultsList.addListSelectionListener(e -> {
            ParserDiagnosticsResult current = resultsList.getSelectedValue();
            ParserDiagnosticsResult previous = manager.getPreviousResult(current);
            detailsForm.renderDeltaResult(previous, current);
            dialog.updateButtons();
        });

        resultsList.setCellRenderer(new ResultListCellRenderer());
        refreshResults();
    }

    public void initResult(@NotNull ParserDiagnosticsResult current) {
        ParserDiagnosticsResult previous = manager.getPreviousResult(current);
        detailsForm.renderDeltaResult(previous, current);
        List<ParserDiagnosticsResult> history = manager.getResultHistory();
        if (!history.contains(current)) {
            DefaultListModel<ParserDiagnosticsResult> model = getResultsModel();
            model.insertElementAt(current, 0);
        }
        resultsList.setSelectedIndex(0);
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

    public void addResult(ParserDiagnosticsResult result) {
        DefaultListModel<ParserDiagnosticsResult> model = getResultsModel();
        model.insertElementAt(result, 0);
    }

    private class ResultListCellRenderer extends ColoredListCellRenderer<ParserDiagnosticsResult> {
        @Override
        protected void customizeCellRenderer(@NotNull JList list, ParserDiagnosticsResult value, int index, boolean selected, boolean hasFocus) {
            String text = formatter.formatDateTime(value.getTimestamp());
            if (!value.isSaved()) {
                text = text + " (draft)";
            }
            append(text, SimpleTextAttributes.REGULAR_ATTRIBUTES);
        }
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
