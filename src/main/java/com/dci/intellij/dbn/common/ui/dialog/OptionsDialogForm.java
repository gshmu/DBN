package com.dci.intellij.dbn.common.ui.dialog;

import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.form.DBNHintForm;
import com.dci.intellij.dbn.common.ui.misc.DBNComboBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OptionsDialogForm<O extends Presentable> extends DBNFormBase {
    private JPanel mainPanel;
    private JLabel optionLabel;
    private DBNComboBox<O> optionComboBox;
    private JPanel optionDescriptionPanel;

    public OptionsDialogForm(OptionsDialog<O> dialog) {
        super(dialog);

        DBNHintForm optionDescriptionForm = new DBNHintForm(this, "", null, false);
        optionDescriptionPanel.add(optionDescriptionForm.getComponent());

        optionLabel.setText(dialog.getOptionLabel());

        optionComboBox.setValues(dialog.getOptions());
        optionComboBox.setSelectedValue(dialog.getSelectedOption());

        optionComboBox.addListener((oldValue, newValue) -> {
           optionDescriptionForm.setHintText(newValue == null ? "" : newValue.getDescription());
           dialog.setSelectedOption(newValue);
           dialog.setActionsEnabled(newValue != null);
        });
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return optionComboBox;
    }
}
