package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.ui.table.DBNTableHeaderRendererBase;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class BasicTableHeaderRenderer extends DBNTableHeaderRendererBase {
    private JPanel mainPanel;
    private JLabel nameLabel;

    public BasicTableHeaderRenderer() {
        mainPanel.setOpaque(true);
        mainPanel.setBackground(UIUtil.getPanelBackground());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        int width = 0;
        String columnName = value.toString();

        nameLabel.setText(columnName);

        FontMetrics fontMetrics = getFontMetrics();
        width += fontMetrics.stringWidth(columnName) + 24;
        int height = fontMetrics.getHeight() + 6;
        mainPanel.setPreferredSize(new Dimension(width, height));
        mainPanel.setBorder(columnIndex == 0 ? BORDER_LBR.get() : BORDER_BR.get());
        return mainPanel;
    }

    @Override
    protected JLabel getNameLabel() {
        return nameLabel;
    }
}
