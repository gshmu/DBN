package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.intellij.ui.border.CustomLineBorder;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.Map;

public abstract class DBNTableGutterRendererBase implements DBNTableGutterRenderer{
    protected JLabel textLabel;
    protected JLabel iconLabel;
    protected JPanel mainPanel;

    private final Latent<Map<Integer, Integer>> indexWidth = Latent.mutable(
            () -> textLabel.getFont(),
            () -> new HashMap<>());

    public DBNTableGutterRendererBase() {
        textLabel.setText("");
        iconLabel.setText("");
        textLabel.setFont(GUIUtil.getEditorFont());
        textLabel.setForeground(Colors.tableLineNumberColor());
        mainPanel.setPreferredSize(new Dimension(40, -1));
        iconLabel.setBorder(Borders.insetBorder(4));

        Border border = new CustomLineBorder(Colors.TABLE_HEADER_GRID_COLOR, 0, 0, 0, 1);
        mainPanel.setBorder(border);
    }

    @Override
    public final Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        adjustListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        textLabel.setText(Integer.toString(index + 1));
        int textWidth = computeLabelWidth(list.getModel().getSize());
        int iconWidth = iconLabel.getIcon() == null ? 0 : 16;
        //iconLabel.setVisible(iconLabel.getIcon() == null);

        int preferredWidth = textWidth + iconWidth + 16;

        Dimension preferredSize = mainPanel.getPreferredSize();
        if (preferredSize.getWidth() != preferredWidth) {
            Dimension dimension = new Dimension(preferredWidth, -1);
            mainPanel.setPreferredSize(dimension);
            list.setPreferredSize(new Dimension(preferredWidth, (int) list.getPreferredSize().getHeight()));
        }
        return mainPanel;
    }

    private int computeLabelWidth(int count) {
        return indexWidth.get().computeIfAbsent(count, k -> {
            int digits = (int) Math.log10(count) + 1;
            String text = StringUtils.leftPad("", digits, "0");
            Font font = textLabel.getFont();
            FontRenderContext fontRenderContext = textLabel.getFontMetrics(font).getFontRenderContext();
            return (int) font.getStringBounds(text, fontRenderContext).getWidth();
        });
    }

    protected abstract void adjustListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus);
}
