package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.Insets;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.UIUtil;

public class DBNHintForm extends DBNFormImpl{
    private JPanel mainPanel;
    private JLabel hintLabel;
    private JTextPane hintTextPane;

    public DBNHintForm(String hintText, MessageType messageType, boolean withBorder) {
        hintLabel.setText("");
        if (messageType != null) {
            Icon icon = Icons.COMMON_INFO;
            switch (messageType) {
                case INFO: icon = Icons.COMMON_INFO; break;
                case WARNING: icon = Icons.COMMON_WARNING; break;
                case ERROR: icon = Icons.COMMON_ERROR; break;

            }
            hintLabel.setIcon(icon);
        } else {
            hintLabel.setVisible(false);
        }

        Color background = withBorder ? brighten(UIUtil.getPanelBackground(), 0.08) : UIUtil.getPanelBackground();

        mainPanel.setBackground(background);
        hintTextPane.setBackground(background);
        hintTextPane.setText(hintText);
        hintTextPane.setFont(UIUtil.getLabelFont());
        hintTextPane.setForeground(Colors.HINT_COLOR);
        if (withBorder) {
            mainPanel.setBorder(new RoundedLineBorder(UIUtil.getBoundsColor(), 6));
        } else {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) mainPanel.getLayout();
            gridLayoutManager.setMargin(new Insets(0,0,0,0));
        }

    }

    public static Color brighten(Color color, double fraction) {

        int red = (int) Math.round(Math.min(255, color.getRed() + 255 * fraction));
        int green = (int) Math.round(Math.min(255, color.getGreen() + 255 * fraction));
        int blue = (int) Math.round(Math.min(255, color.getBlue() + 255 * fraction));

        int alpha = color.getAlpha();

        return new Color(red, green, blue, alpha);

    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
