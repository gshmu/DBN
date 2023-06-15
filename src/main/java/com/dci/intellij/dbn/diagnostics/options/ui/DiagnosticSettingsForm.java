package com.dci.intellij.dbn.diagnostics.options.ui;

import com.dci.intellij.dbn.common.text.TextContent;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.form.DBNHintForm;
import com.dci.intellij.dbn.diagnostics.Diagnostics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil.validateIntegerValue;
import static com.dci.intellij.dbn.common.text.TextContent.plain;

public class DiagnosticSettingsForm extends DBNFormBase {
    private JPanel mainPanel;
    private JCheckBox developerModeCheckBox;
    private JCheckBox databaseResourcesCheckBox;
    private JCheckBox databaseAccessCheckBox;

    private JCheckBox databaseLaggingCheckBox;
    private JTextField connectivityLagTextField;
    private JTextField queryingLagTextField;
    private JTextField fetchingLagTextField;
    private JCheckBox dialogSizingCheckbox;
    private JCheckBox bulkActionsCheckbox;
    private JCheckBox failsafeLoggingCheckBox;
    private JCheckBox backgroundDisposerCheckBox;
    private JPanel hintPanel;
    private JLabel acknowledgementLabel;

    private final DBNHintForm disclaimerForm;

    public DiagnosticSettingsForm(@Nullable Disposable parent) {
        super(parent);
        developerModeCheckBox.setSelected(Diagnostics.isDeveloperMode());

        TextContent hintText = plain("NOTE\nDeveloper Mode enables actions that can affect your system stability and data integrity. " +
                "Features like \"Slow Database Simulations\" or excessive \"Debug Logging\" are meant for diagnostic activities only " +
                "and are significantly degrading the performance of your development environment.\n\n" +
                "Please disable developer mode unless explicitly instructed to use it and properly guided throughout the process by DBN plugin developers.");
        disclaimerForm = new DBNHintForm(this, hintText, null, true);
        hintPanel.add(disclaimerForm.getComponent());

        Diagnostics.DebugLogging debugLogging = Diagnostics.getDebugLogging();
        failsafeLoggingCheckBox.setSelected(debugLogging.isFailsafeErrors());
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
        backgroundDisposerCheckBox.setSelected(miscellaneous.isBackgroundDisposerDisabled());

        updateFields(null);

        databaseLaggingCheckBox.addActionListener(e -> updateFields(e));
        developerModeCheckBox.addActionListener(e -> updateFields(e));
    }

    private void updateFields(ActionEvent e) {
        boolean developerMode = developerModeCheckBox.isSelected();
        databaseAccessCheckBox.setEnabled(developerMode);
        databaseResourcesCheckBox.setEnabled(developerMode);
        databaseLaggingCheckBox.setEnabled(developerMode);
        dialogSizingCheckbox.setEnabled(developerMode);
        bulkActionsCheckbox.setEnabled(developerMode);
        failsafeLoggingCheckBox.setEnabled(developerMode);
        backgroundDisposerCheckBox.setEnabled(developerMode);

        boolean databaseLaggingEnabled = developerMode && databaseLaggingCheckBox.isSelected();
        connectivityLagTextField.setEnabled(databaseLaggingEnabled);
        queryingLagTextField.setEnabled(databaseLaggingEnabled);
        fetchingLagTextField.setEnabled(databaseLaggingEnabled);

        acknowledgementLabel.setText(developerMode ? "(will be automatically disabled after 10 minutes)" : "");
        acknowledgementLabel.setForeground(UIUtil.getLabelDisabledForeground());
        //acknowledgementLabel.setIcon(developerMode ? Icons.COMMON_INFO : null);
        disclaimerForm.setHighlighted(developerMode);
    }



    public void applyFormChanges() throws ConfigurationException {
        Diagnostics.setDeveloperMode(developerModeCheckBox.isSelected());
        Diagnostics.DebugLogging debugLogging = Diagnostics.getDebugLogging();
        debugLogging.setFailsafeErrors(failsafeLoggingCheckBox.isSelected());
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
        miscellaneous.setBackgroundDisposerDisabled(backgroundDisposerCheckBox.isSelected());

    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }
}
