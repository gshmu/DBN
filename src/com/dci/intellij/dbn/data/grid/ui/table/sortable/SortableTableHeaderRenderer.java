package com.dci.intellij.dbn.data.grid.ui.table.sortable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.table.DBNTableHeaderRendererBase;
import com.dci.intellij.dbn.data.model.sortable.SortableDataModel;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;

public class SortableTableHeaderRenderer extends DBNTableHeaderRendererBase {
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JLabel sortingLabel;

    public SortableTableHeaderRenderer() {
        mainPanel.setOpaque(true);
        mainPanel.setBackground(UIUtil.getPanelBackground());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        SortableDataModel model = (SortableDataModel) table.getModel();
        sortingLabel.setText(null);
        int width = 0;
        String columnName = value.toString();
        SortingState sortingState = model.getSortingState();
        SortingInstruction sortingInstruction = sortingState.getSortingInstruction(columnName);

        if (sortingInstruction != null) {
            Icon icon = sortingInstruction.getDirection() == SortDirection.ASCENDING ?
                    Icons.DATA_EDITOR_SORT_ASC :
                    Icons.DATA_EDITOR_SORT_DESC;
            sortingLabel.setIcon(icon);
            width += icon.getIconWidth();
            if (sortingState.size() > 1) {
                sortingLabel.setText(Integer.toString(sortingInstruction.getIndex()));
            }
        } else {
            sortingLabel.setIcon(null);
        }

        nameLabel.setText(columnName);

        FontMetrics fontMetrics = getFontMetrics();
        width += fontMetrics.stringWidth(columnName) + 24;
        int height = fontMetrics.getHeight() + 6;
        mainPanel.setPreferredSize(new Dimension(width, height));
        mainPanel.setBorder(columnIndex == 0 ? BORDER_LR.get() : BORDER_R.get());
        return mainPanel;
    }

    @Override
    protected JLabel getNameLabel() {
        return nameLabel;
    }
}
