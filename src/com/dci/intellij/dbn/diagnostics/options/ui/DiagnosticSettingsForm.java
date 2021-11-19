package com.dci.intellij.dbn.diagnostics.options.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
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
    private JCheckBox dialogSizingCheckbox;
    private JCheckBox bulkActionsCheckbox;
    private JPanel hintPanel;
    private JLabel acknowledgementLabel;

    private final DBNHintForm disclaimerForm;

    public DiagnosticSettingsForm(@Nullable Disposable parent) {
        super(parent);
        developerModeCheckBox.setSelected(Diagnostics.isDeveloperMode());

        String hintText = "Developer Mode enables actions that can affect your system stability and data integrity. " +
                "Features like \"Slow Database Simulations\" or excessive \"Debug Logging\" are meant for diagnostic activities only " +
                "and are significantly degrading the performance of your development environment.\n\n" +
                "Please disable developer mode unless explicitly instructed to use it and properly guided throughout the process by DBN plugin developers.";
        disclaimerForm = new DBNHintForm(this, hintText, null, true);
        hintPanel.add(disclaimerForm.getComponent());

        Diagnostics.DebugLogging debugLogging = Diagnostics.getDebugLogging();
        languageParserCheckBox.setSelected(debugLogging.isLanguageParser());
        databaseAccessCheckBox.setSelected(debugLogging.isDatabaseAccess());
        databaseResourcesCheckBox.setSelected(debugLogging.isDatabaseResource());

        Diagnostics.DatabaseLag databaseLag = Diagnostics.getDatabaseLag();
        databaseLaggingCheckBox.setSelected(databaseLag.isEnabled());
        connectivityLagTextField.setText(Long.toString(databaseLag.getConnectivity()));
        queryingLagTextField.setText(Long.toString(databaseLag.getQuerying()));
        fetchingLagTextField.setText(Long.toString(databaseLag.getFetching()));

        Diagnostics.Miscellaneous miscellaneous = Diagnostics.getMiscellaneous();
        dialogSizingCheckbox.setSelected(miscellaneous.isDialogSizingReset());
        bulkActionsCheckbox.setSelected(miscellaneous.isBulkActionsEnabled());

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
        dialogSizingCheckbox.setEnabled(developerMode);
        bulkActionsCheckbox.setEnabled(developerMode);

        boolean databaseLaggingEnabled = developerMode && databaseLaggingCheckBox.isSelected();
        connectivityLagTextField.setEnabled(databaseLaggingEnabled);
        queryingLagTextField.setEnabled(databaseLaggingEnabled);
        fetchingLagTextField.setEnabled(databaseLaggingEnabled);

        acknowledgementLabel.setText(developerMode ? "Please acknowledge and consent with below..." : "");
        acknowledgementLabel.setIcon(developerMode ? Icons.COMMON_WARNING : null);
        disclaimerForm.setHighlighted(developerMode);
    }



    public void applyFormChanges() throws ConfigurationException {
        Diagnostics.setDeveloperMode(developerModeCheckBox.isSelected());
        Diagnostics.DebugLogging debugLogging = Diagnostics.getDebugLogging();
        debugLogging.setLanguageParser(languageParserCheckBox.isSelected());
        debugLogging.setDatabaseAccess(databaseAccessCheckBox.isSelected());
        debugLogging.setDatabaseResource(databaseResourcesCheckBox.isSelected());

        Diagnostics.DatabaseLag databaseLag = Diagnostics.getDatabaseLag();
        databaseLag.setEnabled(databaseLaggingCheckBox.isSelected());
        databaseLag.setConnectivity(validateIntegerValue(connectivityLagTextField, "Connectivity Lag", true, 0, 60000, null));
        databaseLag.setQuerying(validateIntegerValue(queryingLagTextField, "Querying Lag", true, 0, 60000, null));
        databaseLag.setFetching(validateIntegerValue(fetchingLagTextField, "Fetching Lag", true, 0, 10000, null));

        Diagnostics.Miscellaneous miscellaneous = Diagnostics.getMiscellaneous();
        miscellaneous.setDialogSizingReset(dialogSizingCheckbox.isSelected());
        miscellaneous.setBulkActionsEnabled(bulkActionsCheckbox.isSelected());

    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
