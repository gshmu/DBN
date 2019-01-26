package com.dci.intellij.dbn.common.ui;

import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;

import javax.swing.*;
import java.awt.*;

public abstract class DBNComboBoxAction extends ComboBoxAction implements DumbAware {
    @Override
    public JComponent createCustomComponent(Presentation presentation) {
    JPanel panel=new JPanel(new GridBagLayout());
    ComboBoxButton button = new ComboBoxButton(presentation);
        GridBagConstraints constraints = new GridBagConstraints(
                0, 0, 1, 1, 1, 1,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 0, 0);
        panel.add(button, constraints);
        panel.setFocusable(false);
        return panel;
    }
}
