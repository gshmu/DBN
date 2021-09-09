package com.dci.intellij.dbn.options.general.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.environment.Environment;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

public class GeneralProjectSettingsForm extends CompositeConfigurationEditorForm<GeneralProjectSettings> {
    private JPanel mainPanel;
    private JPanel localeSettingsPanel;
    private JPanel environmentSettingsPanel;
    private JCheckBox enableDeveloperCheckBox;
    private JLabel developerInfoLabel;

    public GeneralProjectSettingsForm(GeneralProjectSettings generalSettings) {
        super(generalSettings);
        developerInfoLabel.setIcon(Icons.COMMON_WARNING);
        developerInfoLabel.setText("NOTE: Developer mode enables actions that may compromise your system stability and data integrity.");
        resetFormChanges();

        registerComponent(mainPanel);

        localeSettingsPanel.add(generalSettings.getRegionalSettings().createComponent(), BorderLayout.CENTER);
        environmentSettingsPanel.add(generalSettings.getEnvironmentSettings().createComponent(), BorderLayout.CENTER);
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            getConfiguration().setModified(true);
            developerInfoLabel.setVisible(enableDeveloperCheckBox.isSelected());
        };
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() {
        Environment.updateDeveloperMode(enableDeveloperCheckBox.isSelected());
    }

    @Override
    public void resetFormChanges() {
        enableDeveloperCheckBox.setSelected(Environment.DEVELOPER_MODE);
        developerInfoLabel.setVisible(enableDeveloperCheckBox.isSelected());
    }
}
