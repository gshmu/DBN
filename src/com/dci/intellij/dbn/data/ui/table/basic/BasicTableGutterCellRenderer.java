package com.dci.intellij.dbn.data.ui.table.basic;

import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.data.model.DataModelRow;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.UIUtil;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

public class BasicTableGutterCellRenderer extends JPanel implements ListCellRenderer {
    public interface Colors {
        Color LINE_NUMBER_COLOR = getGlobalScheme().getColor(EditorColors.LINE_NUMBERS_COLOR);
        Color SELECTION_FOREGROUND_COLOR = getGlobalScheme().getColor(EditorColors.SELECTION_FOREGROUND_COLOR);
        Color SELECTION_BACKGROUND_COLOR = getGlobalScheme().getColor(EditorColors.SELECTION_BACKGROUND_COLOR);
        Color CARET_ROW_COLOR = getGlobalScheme().getColor(EditorColors.CARET_ROW_COLOR);
    }

    static EditorColorsScheme getGlobalScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }

    private Border border = new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), new EmptyBorder(0, 3, 0, 3));
    private JLabel textLabel;

    public BasicTableGutterCellRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(border);
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setForeground(Colors.LINE_NUMBER_COLOR);
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        add(textLabel, BorderLayout.EAST);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;
        BasicDataModel model = tableGutter.getModel();
        DataModelRow row = model.getRowAtIndex(index);
        textLabel.setText(row == null ? "" : Integer.toString(row.getIndex() + 1));
        DBNTable table = tableGutter.getTable();
        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;

        setBackground(isSelected ?
                Colors.SELECTION_BACKGROUND_COLOR :
                isCaretRow ?
                        Colors.CARET_ROW_COLOR :
                        UIUtil.getPanelBackground());
        textLabel.setForeground(isSelected ? Colors.SELECTION_FOREGROUND_COLOR : Colors.LINE_NUMBER_COLOR);
        return this;
    }
}
