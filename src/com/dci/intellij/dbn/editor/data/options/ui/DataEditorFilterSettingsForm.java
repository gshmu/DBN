package com.dci.intellij.dbn.editor.data.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.options.DataEditorFilterSettings;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionListener;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DataEditorFilterSettingsForm extends ConfigurationEditorForm<DataEditorFilterSettings> {
    private JPanel mainPanel;
    private JCheckBox promptFilterDialogCheckBox;
    private JComboBox<DatasetFilterType> defaultFilterTypeComboBox;

    public DataEditorFilterSettingsForm(DataEditorFilterSettings settings) {
        super(settings);
        initComboBox(defaultFilterTypeComboBox,
                DatasetFilterType.NONE,
                DatasetFilterType.BASIC,
                DatasetFilterType.CUSTOM);

        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
        registerComponent(mainPanel);
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            defaultFilterTypeComboBox.setEnabled(promptFilterDialogCheckBox.isSelected());
        };
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        DataEditorFilterSettings settings = getConfiguration();
        settings.setPromptFilterDialog(promptFilterDialogCheckBox.isSelected());
        settings.setDefaultFilterType(getSelection(defaultFilterTypeComboBox));
    }


    @Override
    public void resetFormChanges() {
        DataEditorFilterSettings settings = getConfiguration();
        promptFilterDialogCheckBox.setSelected(settings.isPromptFilterDialog());
        setSelection(defaultFilterTypeComboBox, settings.getDefaultFilterType());
    }
}
