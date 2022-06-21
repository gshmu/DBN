package com.dci.intellij.dbn.data.grid.options.ui;

import com.dci.intellij.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dci.intellij.dbn.data.grid.options.DataGridSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import java.awt.BorderLayout;

public class DataGridSettingsForm extends CompositeConfigurationEditorForm<DataGridSettings> {
    private JPanel mainPanel;
    private JPanel auditColumnSettingsPanel;
    private JPanel sortingSettingsPanel;
    private JPanel generalSettingsPanel;

    public DataGridSettingsForm(DataGridSettings settings) {
        super(settings);
        auditColumnSettingsPanel.add(settings.getAuditColumnSettings().createComponent(), BorderLayout.CENTER);
        sortingSettingsPanel.add(settings.getSortingSettings().createComponent(), BorderLayout.CENTER);
        generalSettingsPanel.add(settings.getGeneralSettings().createComponent(), BorderLayout.CENTER);
        resetFormChanges();
    }


    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }
}
