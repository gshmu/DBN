package com.dci.intellij.dbn.common.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DBNLinkComponent extends JPanel{
    private JLabel label;
    public DBNLinkComponent(String text) {
        label = new JLabel(text);
        label.setForeground(JBColor.BLUE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }
    
    public void setLabel(String text) {
        label.setText(text);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
    }
}
