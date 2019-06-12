package com.dci.intellij.dbn.editor.data.ui.table.renderer;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableGutter;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;

import static com.dci.intellij.dbn.editor.data.model.RecordStatus.*;

public class DatasetEditorTableGutterRenderer extends JPanel implements ListCellRenderer {
    private JLabel textLabel;
    private JLabel imageLabel;
    private JPanel textPanel;
    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    public DatasetEditorTableGutterRenderer() {
        setBackground(UIUtil.getPanelBackground());
        setBorder(new CompoundBorder(new CustomLineBorder(UIUtil.getPanelBackground(), 0, 0, 1, 1), Borders.TEXT_FIELD_BORDER));
        setLayout(new BorderLayout());
        textLabel = new JLabel();
        textLabel.setFont(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));
        imageLabel = new JLabel();

        textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(JBUI.Borders.emptyRight(3));
        textPanel.add(textLabel, BorderLayout.EAST);
        add(textPanel, BorderLayout.CENTER);
        add(imageLabel, BorderLayout.EAST);
        textLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
        textLabel.setCursor(HAND_CURSOR);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (textLabel != null) textLabel.setFont(font);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        BasicTableGutter tableGutter = (BasicTableGutter) list;
        ListModel model = list.getModel();
        DatasetEditorModelRow row = (DatasetEditorModelRow) model.getElementAt(index);
        DatasetEditorTable table = (DatasetEditorTable) tableGutter.getTable();
        if (row != null) {
            Icon icon =
                    row.is(INSERTING) ? Icons.DATA_EDITOR_ROW_INSERT :
                    row.is(INSERTED) ? Icons.DATA_EDITOR_ROW_INSERTED :
                    row.is(DELETED) ? Icons.DATA_EDITOR_ROW_DELETED :
                    row.is(MODIFIED) ? Icons.DATA_EDITOR_ROW_MODIFIED :
                    table.getModel().is(MODIFIED) ? Icons.DATA_EDITOR_ROW_DEFAULT : null;

            textLabel.setText(Integer.toString(row.getIndex() + 1));
            if (imageLabel.getIcon() != icon) {
                imageLabel.setIcon(icon);
            }
        }
        //lText.setFont(isSelected ? BOLD_FONT : REGULAR_FONT);

        boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == index && table.getSelectedRowCount() == 1;
        Color background = isSelected ?
                Colors.tableSelectionBackgroundColor() :
                isCaretRow ?
                        Colors.tableCaretRowColor() :
                        UIUtil.getPanelBackground();
        setBackground(background);
        textPanel.setBackground(background);
        textLabel.setForeground(isSelected ?
                Colors.tableSelectionForegroundColor() :
                Colors.tableLineNumberColor());
        return this;
    }
}
