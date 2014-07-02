package com.dci.intellij.dbn.common.ui;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;

public class DBNHeaderForm extends DBNFormImpl{
    private JLabel objectLabel;
    private JPanel mainPanel;

    public DBNHeaderForm() {
    }

    public DBNHeaderForm(String title, Icon icon) {
        this(title, icon, null);
    }


    public DBNHeaderForm(String title, Icon icon, Color background) {
        objectLabel.setText(title);
        objectLabel.setIcon(icon);
        if (background != null) {
            mainPanel.setBackground(background);
        }
    }

    public void setBackground(Color background) {
        mainPanel.setBackground(background);
    }

    public void setTitle(String title) {
        objectLabel.setText(title);
    }

    public void setIcon(Icon icon) {
        objectLabel.setIcon(icon);
    }

    public Color getBackground() {
        return mainPanel.getBackground();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
