package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
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

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.getSelection;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.initComboBox;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.setSelection;


public class ConnectionDriverSettingsForm extends DBNFormImpl<ConnectionDatabaseSettingsForm>{
    private TextFieldWithBrowseButton driverLibraryTextField;
    private JPanel mainPanel;
    private JPanel driverSetupPanel;
    private JComboBox<DriverSource> driverSourceComboBox;
    private JComboBox<DriverOption> driverComboBox;
    private JLabel driverErrorLabel;
    private JLabel driverLabel;
    private JLabel driverLibraryLabel;
    private JLabel driverSourceLabel;

    /** allow select a single jar file or a directory */
    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, true, true, true, false, false);

    public ConnectionDriverSettingsForm(@NotNull final ConnectionDatabaseSettingsForm parentComponent) {
        super(parentComponent);

        initComboBox(driverSourceComboBox, DriverSource.BUILTIN, DriverSource.EXTERNAL);
        driverSourceComboBox.addActionListener(e -> {
            DriverSource selection = getSelection(driverSourceComboBox);

            boolean isExternalLibrary = selection == DriverSource.EXTERNAL;
            driverLibraryTextField.setEnabled(isExternalLibrary);
            driverComboBox.setEnabled(isExternalLibrary);
            updateDriverFields();
            //driverSetupPanel.setVisible(isExternalLibrary);
        });

        driverLibraryTextField.addBrowseFolderListener(
                "Select Driver Library or directory",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                null, LIBRARY_FILE_DESCRIPTOR);
    }

    public void updateDriverFields() {
        DatabaseType databaseType = ensureParentComponent().getSelectedDatabaseType();
        boolean allowBuiltInLibrary = databaseType != DatabaseType.GENERIC;

        DriverSource driverSource = allowBuiltInLibrary ? getSelection(driverSourceComboBox) : DriverSource.EXTERNAL;
        driverSourceComboBox.setVisible(allowBuiltInLibrary);
        driverSourceLabel.setVisible(allowBuiltInLibrary);

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
                if (libraryDatabaseType != DatabaseType.UNKNOWN && libraryDatabaseType != ensureParentComponent().getSelectedDatabaseType()) {
                    error = "The driver library does not match the selected database type";
                    initComboBox(driverComboBox);
                    setSelection(driverComboBox, null);
                } else {
                    List<Driver> drivers = DatabaseDriverManager.getInstance().loadDrivers(driverLibrary, false);
                    DriverOption selectedOption = getSelection(driverComboBox);
                    initComboBox(driverComboBox);
                    //driverComboBox.addItem("");
                    if (drivers != null && !drivers.isEmpty()) {
                        List<DriverOption> driverOptions = new ArrayList<DriverOption>();
                        for (Driver driver : drivers) {
                            DriverOption driverOption = new DriverOption(driver);
                            driverOptions.add(driverOption);
                            if (selectedOption != null && selectedOption.getDriver().equals(driver)) {
                                selectedOption = driverOption;
                            }
                        }

                        initComboBox(driverComboBox, driverOptions);

                        if (selectedOption == null && !driverOptions.isEmpty()) {
                            selectedOption = driverOptions.get(0);
                        }
                    } else {
                        error = "Invalid driver library";
                    }
                    setSelection(driverComboBox, selectedOption);
                }
            } else {
                textField.setForeground(JBColor.RED);
                if (StringUtil.isEmpty(driverLibrary)) {
                    error = "Driver library is not specified";
                } else {
                    error = "Cannot locate driver library file";
                }
                initComboBox(driverComboBox);
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

    public JComboBox<DriverOption> getDriverComboBox() {
        return driverComboBox;
    }

    public JLabel getDriverErrorLabel() {
        return driverErrorLabel;
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    public JComboBox<DriverSource> getDriverSourceComboBox() {
        return driverSourceComboBox;
    }
}

