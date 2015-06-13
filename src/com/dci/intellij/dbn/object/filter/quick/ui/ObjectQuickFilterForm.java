package com.dci.intellij.dbn.object.filter.quick.ui;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.ui.DBNFormImpl;

public class ObjectQuickFilterForm extends DBNFormImpl<ObjectQuickFilterDialog> {
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel conditionListPanel;

    public ObjectQuickFilterForm(@NotNull ObjectQuickFilterDialog parent) {
        super(parent);

        conditionListPanel.setLayout(new BoxLayout(conditionListPanel, BoxLayout.Y_AXIS));
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
