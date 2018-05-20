package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;

public class IndexTableGutterCellRenderer extends JPanel implements ListCellRenderer {

    static EditorColorsScheme getGlobalScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    private static final Border BORDER = new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), Borders.TEXT_FIELD_BORDER);
    private JLabel textLabel;

    public IndexTableGutterCellRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(BORDER);
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setForeground(BasicTableColors.getLineNumberColor());
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        add(textLabel, BorderLayout.EAST);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        DBNTableGutter tableGutter = (DBNTableGutter) list;
        textLabel.setText(Integer.toString(index));
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        setBackground(isSelected ?
                BasicTableColors.getSelectionBackgroundColor() :
                isCaretRow ?
                        BasicTableColors.getCaretRowColor() :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ? BasicTableColors.getSelectionForegroundColor() : BasicTableColors.getLineNumberColor());
        return this;
    }
}
