package com.dci.intellij.dbn.common.environment.options.ui;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.intellij.ui.ColoredSideBorder;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class EnvironmentTypesTableCellRenderer extends ColoredTableCellRenderer{
    @Override
    protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
        if (column == 2) {
            Color color = (Color) value;
            if (color == null) color = Color.WHITE;
            setBackground(color);
            setBorder(new CompoundBorder(new LineBorder(Color.WHITE), new ColoredSideBorder(color.brighter(), color.brighter(), color.darker(), color.darker(), 1)));
        } else {
            String stringValue = (String) value;
            if (StringUtil.isNotEmpty(stringValue)) {
                append(stringValue, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        }
    }
}
