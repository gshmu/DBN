package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;


public class ConnectionDriverSettingsForm extends DBNFormImpl<ConnectionDatabaseSettingsForm>{
    private TextFieldWithBrowseButton driverLibraryTextField;
    private DBNComboBox<DriverOption> driverComboBox;
    private JPanel mainPanel;
    private JLabel driverErrorLabel;
    private DBNComboBox<DriverSource> driverSourceComboBox;
    private JPanel driverSetupPanel;
    private JLabel driverLabel;
    private JLabel driverLibraryLabel;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public ConnectionDriverSettingsForm(@NotNull final ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);

        driverSourceComboBox.setValues(DriverSource.BUILTIN, DriverSource.EXTERNAL);
        driverSourceComboBox.addListener(new ValueSelectorListener<DriverSource>() {
            @Override
            public void selectionChanged(DriverSource oldValue, DriverSource newValue) {
                boolean isExternalLibrary = newValue == DriverSource.EXTERNAL;
                driverLibraryTextField.setEnabled(isExternalLibrary);
                driverComboBox.setEnabled(isExternalLibrary);
                updateDriverFields();
                //driverSetupPanel.setVisible(isExternalLibrary);
            }
        });

        driverLibraryTextField.addBrowseFolderListener(
                "Select Driver Library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                null, LIBRARY_FILE_DESCRIPTOR);
    }

    protected void updateDriverFields() {
        DriverSource driverSource = driverSourceComboBox == null ? DriverSource.EXTERNAL : driverSourceComboBox.getSelectedValue();

        String error = null;
        boolean externalDriver = driverSource == DriverSource.EXTERNAL;
        driverLibraryLabel.setVisible(externalDriver);
        driverLibraryTextField.setVisible(externalDriver);
        driverLabel.setVisible(externalDriver);
        driverComboBox.setVisible(externalDriver);

        if (externalDriver) {
            JTextField textField = driverLibraryTextField.getTextField();
            String driverLibrary = textField.getText();

            boolean fileExists = StringUtil.isNotEmpty(driverLibrary) && fileExists(driverLibrary);
            if (fileExists) {
                textField.setForeground(UIUtil.getTextFieldForeground());
                DatabaseType libraryDatabaseType = DatabaseType.resolve(driverLibrary);
                if (libraryDatabaseType != DatabaseType.UNKNOWN && libraryDatabaseType != getParentComponent().getSelectedDatabaseType()) {
                    error = "The driver library does not match the selected database type";
                    driverComboBox.clearValues();
                    driverComboBox.setSelectedValue(null);
                } else {
                    List<Driver> drivers = DatabaseDriverManager.getInstance().loadDrivers(driverLibrary);
                    DriverOption selectedOption = driverComboBox.getSelectedValue();
                    driverComboBox.clearValues();
                    //driverComboBox.addItem("");
                    if (drivers != null && drivers.size() > 0) {
                        List<DriverOption> driverOptions = new ArrayList<DriverOption>();
                        for (Driver driver : drivers) {
                            DriverOption driverOption = new DriverOption(driver);
                            driverOptions.add(driverOption);
                            if (selectedOption != null && selectedOption.getDriver().equals(driver)) {
                                selectedOption = driverOption;
                            }
                        }

                        driverComboBox.setValues(driverOptions);

                        if (selectedOption == null && driverOptions.size() > 0) {
                            selectedOption = driverOptions.get(0);
                        }
                    } else {
                        error = "Invalid driver library";
                    }
                    driverComboBox.setSelectedValue(selectedOption);
                }
            } else {
                textField.setForeground(JBColor.RED);
                if (StringUtil.isEmpty(driverLibrary)) {
                    error = "Driver library is not specified";
                } else {
                    error = "Cannot locate driver library file";
                }
                driverComboBox.clearValues();
                //driverComboBox.addItem("");
            }
        }

        if (error != null) {
            driverErrorLabel.setIcon(Icons.COMMON_ERROR);
            driverErrorLabel.setText(error);
            driverErrorLabel.setVisible(true);
        } else {
            driverErrorLabel.setText("");
            driverErrorLabel.setVisible(false);
        }
    }

    private static boolean fileExists(String driverLibrary) {
        return driverLibrary != null && new File(driverLibrary).exists();
    }

    public TextFieldWithBrowseButton getDriverLibraryTextField() {
        return driverLibraryTextField;
    }

    public DBNComboBox<DriverOption> getDriverComboBox() {
        return driverComboBox;
    }

    public JLabel getDriverErrorLabel() {
        return driverErrorLabel;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public DBNComboBox<DriverSource> getDriverSourceComboBox() {
        return driverSourceComboBox;
    }
}

