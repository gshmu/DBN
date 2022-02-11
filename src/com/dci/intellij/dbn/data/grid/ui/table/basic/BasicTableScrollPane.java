package com.dci.intellij.dbn.data.grid.ui.table.basic;

import com.dci.intellij.dbn.common.color.Colors;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.common.ui.Fonts;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import com.intellij.ide.IdeTooltip;
import com.intellij.ide.IdeTooltipManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.Alarm;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;

public class BasicTableScrollPane extends JBScrollPane{
    private final Alarm resizeAlarm = new Alarm();
    private transient Font font;

    public BasicTableScrollPane() {
        JPanel panel = new JPanel();
        panel.setBorder(new CustomLineBorder(Colors.getTableHeaderGridColor(), 0, 0, 1, 1));
        setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER, panel);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        if (e.isControlDown()) {
            Component view = getViewport().getView();
            BasicTable resultTable = (BasicTable) view;

            Project project = resultTable.getProject();
            DataGridSettings dataGridSettings = DataGridSettings.getInstance(project);
            if (dataGridSettings.getGeneralSettings().isZoomingEnabled()) {
                if (font == null) {
                    font = resultTable.getFont();
                }
                float size = font.getSize() + e.getWheelRotation();
                if (size > 7 && size < 20) {
                    font = Fonts.deriveFont(font, size);
                    float defaultSize = Fonts.getLabelFont().getSize();
                    int percentage = (int) (size / defaultSize * 100);

                    Dispatch.alarmRequest(resizeAlarm, 10, true, () -> {
                        resultTable.setFont(font);
                        IdeTooltip tooltip = new IdeTooltip(this, e.getPoint(), new JLabel(percentage + "%"));
                        tooltip.setFont(Fonts.deriveFont(Fonts.REGULAR, (float) 16));
                        IdeTooltipManager.getInstance().show(tooltip, true);
                    });
                }
            } else {
                super.processMouseWheelEvent(e);
            }

        } else{
            super.processMouseWheelEvent(e);
        }
    }
}
