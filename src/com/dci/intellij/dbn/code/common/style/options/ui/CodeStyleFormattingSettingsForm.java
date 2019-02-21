package com.dci.intellij.dbn.code.common.style.options.ui;

import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingOption;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleFormattingSettings;
import com.dci.intellij.dbn.code.common.style.presets.CodeStylePreset;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.*;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeStyleFormattingSettingsForm extends ConfigurationEditorForm<CodeStyleFormattingSettings> {
    private JPanel mainPanel;
    private JPanel settingsPanel;
    private JCheckBox enableCheckBox;
    private Map<CodeStyleFormattingOption, JComboBox<CodeStylePreset>> mappings = new HashMap<>();

    public CodeStyleFormattingSettingsForm(CodeStyleFormattingSettings settings) {
        super(settings);
        List<CodeStyleFormattingOption> options = settings.getOptions();
        settingsPanel.setLayout(new GridLayoutManager(options.size() + 1, 2, new Insets(4, 4, 4, 4), -1, -1));
        updateBorderTitleForeground(mainPanel);
        for (int i=0; i< options.size(); i++) {
            CodeStyleFormattingOption option = options.get(i);
            JLabel label = new JLabel(option.getDisplayName());
            settingsPanel.add(label,
                    new GridConstraints(i, 0, 1, 1,
                            GridConstraints.ANCHOR_WEST,
                            GridConstraints.FILL_NONE,
                            GridConstraints.SIZEPOLICY_FIXED,
                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

            JComboBox<CodeStylePreset> comboBox = new ComboBox<>();
            initComboBox(comboBox, option.getPresets().toArray(new CodeStylePreset[0]));
            label.setLabelFor(comboBox);

            settingsPanel.add(comboBox,
                    new GridConstraints(i, 1, 1, 1,
                            GridConstraints.ANCHOR_WEST,
                            GridConstraints.FILL_HORIZONTAL,
                            GridConstraints.SIZEPOLICY_CAN_GROW,
                            GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

            mappings.put(option, comboBox);
        }

        resetFormChanges();
        enableDisableOptions();
        
        settingsPanel.add(new Spacer(),
                new GridConstraints(options.size(), 1, 1, 1,
                        GridConstraints.ANCHOR_CENTER,
                        GridConstraints.FILL_VERTICAL, 1,
                        GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));

        registerComponent(mainPanel);
        enableCheckBox.addActionListener(e -> enableDisableOptions());

        //Shortcut[] basicShortcuts = KeyUtil.getShortcuts("ReformatCode");
        //useOnReformatCheckBox.setText("Use on reformat code (" + KeymapUtil.getShortcutsText(basicShortcuts) + ")");
    }

    private void enableDisableOptions() {
        boolean selected = enableCheckBox.isSelected();
        for (JComboBox<CodeStylePreset> optionComboBox : mappings.values()) {
            optionComboBox.setEnabled(selected);
        }
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        for (CodeStyleFormattingOption option : mappings.keySet()) {
            JComboBox<CodeStylePreset> comboBox = mappings.get(option);
            option.setPreset(getSelection(comboBox));
        }
        getConfiguration().setEnabled(enableCheckBox.isSelected());
    }


    @Override
    public void resetFormChanges() {
        for (CodeStyleFormattingOption option : mappings.keySet()) {
            JComboBox<CodeStylePreset> comboBox = mappings.get(option);
            setSelection(comboBox, option.getPreset());
        }
        enableCheckBox.setSelected(getConfiguration().isEnabled());
    }
}
