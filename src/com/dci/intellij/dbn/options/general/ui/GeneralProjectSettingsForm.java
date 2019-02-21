package com.dci.intellij.dbn.options.general.ui;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class GeneralProjectSettingsForm extends CompositeConfigurationEditorForm<GeneralProjectSettings> {
    private JPanel mainPanel;
    private JLabel debugInfoLabel;
    private JCheckBox enableDebugCheckBox;
    private JPanel localeSettingsPanel;
    private JPanel environmentSettingsPanel;
    private JCheckBox enableDeveloperCheckBox;
    private JLabel developerInfoLabel;

    public GeneralProjectSettingsForm(GeneralProjectSettings generalSettings) {
        super(generalSettings);
        debugInfoLabel.setIcon(Icons.COMMON_WARNING);
        debugInfoLabel.setText("NOTE: Active debug mode considerably slows down your system.");
        developerInfoLabel.setIcon(Icons.COMMON_WARNING);
        developerInfoLabel.setText("NOTE: Developer mode enables actions that may compromise your system stability and database integrity.");
        resetFormChanges();

        registerComponent(mainPanel);

        localeSettingsPanel.add(generalSettings.getRegionalSettings().createComponent(), BorderLayout.CENTER);
        environmentSettingsPanel.add(generalSettings.getEnvironmentSettings().createComponent(), BorderLayout.CENTER);
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            debugInfoLabel.setVisible(enableDebugCheckBox.isSelected());
            developerInfoLabel.setVisible(enableDeveloperCheckBox.isSelected());
        };
    }

    @NotNull
    @Override
    public JPanel getComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() {
        DatabaseNavigator.debugModeEnabled = enableDebugCheckBox.isSelected();
        DatabaseNavigator.developerModeEnabled = enableDeveloperCheckBox.isSelected();
    }

    @Override
    public void resetFormChanges() {
        enableDebugCheckBox.setSelected(DatabaseNavigator.debugModeEnabled);
        debugInfoLabel.setVisible(enableDebugCheckBox.isSelected());
        enableDeveloperCheckBox.setSelected(DatabaseNavigator.developerModeEnabled);
        developerInfoLabel.setVisible(enableDeveloperCheckBox.isSelected());
    }
}
