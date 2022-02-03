package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.message.MessageType;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import java.awt.Color;

public class DBNHintForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JLabel hintLabel;
    private JTextPane hintTextPane;

    private final boolean boxed;

    public DBNHintForm(DBNForm parent, String hintText, MessageType messageType, boolean boxed) {
        super(parent);
        this.boxed = boxed;
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

        Color background = getBackground();

        mainPanel.setBackground(background);
        hintTextPane.setBackground(background);
        hintTextPane.setText(hintText);
        hintTextPane.setFont(UIUtil.getLabelFont());
        hintTextPane.setForeground(boxed ? Colors.lighter(UIUtil.getLabelForeground(), 1) : Colors.HINT_COLOR);
        if (boxed) {
            mainPanel.setBorder(new RoundedLineBorder(UIUtil.getBoundsColor(), 4));
        } else {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) mainPanel.getLayout();
            gridLayoutManager.setMargin(JBUI.emptyInsets());
        }
    }

    @NotNull
    private Color getBackground() {
        return boxed ? Colors.lighter(UIUtil.getPanelBackground(), 1) : UIUtil.getPanelBackground();
    }

    public void setHighlighted(boolean highlighted) {
        Color background = highlighted ? UIUtil.getTextFieldBackground() : getBackground();
        mainPanel.setBackground(background);
        hintTextPane.setBackground(background);
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
