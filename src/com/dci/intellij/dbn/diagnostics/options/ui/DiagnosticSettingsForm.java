package com.dci.intellij.dbn.diagnostics.options.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;

import static com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil.validateIntegerValue;

public class DiagnosticSettingsForm extends DBNFormImpl {
    private JPanel mainPanel;
    private JCheckBox developerModeCheckBox;
    private JCheckBox languageParserCheckBox;
    private JCheckBox databaseResourcesCheckBox;
    private JCheckBox databaseAccessCheckBox;

    private JCheckBox databaseLaggingCheckBox;
    private JTextField connectivityLagTextField;
    private JTextField queryingLagTextField;
    private JTextField fetchingLagTextField;
    private JLabel developerInfoLabel;

    public DiagnosticSettingsForm(@Nullable Disposable parent) {
        super(parent);
        developerModeCheckBox.setSelected(Diagnostics.isDeveloperMode());
        developerInfoLabel.setIcon(Icons.COMMON_WARNING);
        developerInfoLabel.setText("Developer mode enables actions that could affect your system stability and data integrity.");

        Diagnostics.DebugMode debugMode = Diagnostics.getDebugMode();
        languageParserCheckBox.setSelected(debugMode.isLanguageParser());
        databaseAccessCheckBox.setSelected(debugMode.isDatabaseAccess());
        databaseResourcesCheckBox.setSelected(debugMode.isDatabaseResource());

        Diagnostics.DatabaseLag databaseLag = Diagnostics.getDatabaseLag();
        databaseLaggingCheckBox.setSelected(databaseLag.isEnabled());
        connectivityLagTextField.setText(Long.toString(databaseLag.getConnectivity()));
        queryingLagTextField.setText(Long.toString(databaseLag.getQuerying()));
        fetchingLagTextField.setText(Long.toString(databaseLag.getFetching()));
        updateFields(null);

        databaseLaggingCheckBox.addActionListener(e -> updateFields(e));
        developerModeCheckBox.addActionListener(e -> updateFields(e));
    }

    private void updateFields(ActionEvent e) {

        boolean developerMode = developerModeCheckBox.isSelected();
        languageParserCheckBox.setEnabled(developerMode);
        databaseAccessCheckBox.setEnabled(developerMode);
        databaseResourcesCheckBox.setEnabled(developerMode);
        databaseLaggingCheckBox.setEnabled(developerMode);

        boolean databaseLaggingEnabled = developerMode && databaseLaggingCheckBox.isSelected();
        connectivityLagTextField.setEnabled(databaseLaggingEnabled);
        queryingLagTextField.setEnabled(databaseLaggingEnabled);
        fetchingLagTextField.setEnabled(databaseLaggingEnabled);

        developerInfoLabel.setVisible(developerMode);
    }



    public void applyFormChanges() throws ConfigurationException {
        Diagnostics.setDeveloperMode(developerModeCheckBox.isSelected());
        Diagnostics.DebugMode debugMode = Diagnostics.getDebugMode();
        debugMode.setLanguageParser(languageParserCheckBox.isSelected());
        debugMode.setDatabaseAccess(databaseAccessCheckBox.isSelected());
        debugMode.setDatabaseResource(databaseResourcesCheckBox.isSelected());

        Diagnostics.DatabaseLag databaseLag = Diagnostics.getDatabaseLag();
        databaseLag.setEnabled(databaseLaggingCheckBox.isSelected());
        databaseLag.setConnectivity(validateIntegerValue(connectivityLagTextField, "Connectivity Lag", true, 0, 60000, null));
        databaseLag.setQuerying(validateIntegerValue(queryingLagTextField, "Querying Lag", true, 0, 60000, null));
        databaseLag.setFetching(validateIntegerValue(fetchingLagTextField, "Fetching Lag", true, 0, 10000, null));
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
