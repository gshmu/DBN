package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.ComboBoxUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.TimerUtil;
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
    private HyperlinkLabel reloadDriversLink;
    private JLabel reloadDriversCheckLabel;

    /** allow select a single jar file or a directory */
    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, true, true, true, false, false);

    ConnectionDriverSettingsForm(@NotNull final ConnectionDatabaseSettingsForm parentComponent) {
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
                "Select Driver Library or Directory",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                null, LIBRARY_FILE_DESCRIPTOR);

        reloadDriversCheckLabel.setText("");
        reloadDriversCheckLabel.setIcon(Icons.COMMON_CHECK);
        reloadDriversCheckLabel.setVisible(false);
        reloadDriversLink.setHyperlinkText("Reload Drivers");
        reloadDriversLink.addHyperlinkListener(e -> {
            reloadDriversLink.setVisible(false);
            DatabaseDriverManager driverManager = DatabaseDriverManager.getInstance();
            File driverLibrary = new File(driverLibraryTextField.getText());
            List<Driver> drivers = null;
            try {
                drivers = driverManager.loadDrivers(driverLibrary, true);
                if (drivers == null || drivers.isEmpty()) {
                    reloadDriversCheckLabel.setIcon(Icons.COMMON_WARNING);
                    reloadDriversCheckLabel.setText("No drivers found");
                } else {
                    reloadDriversCheckLabel.setIcon(Icons.COMMON_CHECK);
                    reloadDriversCheckLabel.setText("Drivers reloaded");
                }
            } catch (Exception ex) {
                reloadDriversCheckLabel.setIcon(Icons.COMMON_WARNING);
                reloadDriversCheckLabel.setText(ex.getMessage());
            }
            reloadDriversCheckLabel.setVisible(true);
            Timer timer = TimerUtil.createNamedTimer(
                    "TemporaryLabelTimeout",
                    3000,
                    listener -> {
                        updateDriverReloadLink();
                        reloadDriversCheckLabel.setVisible(false);
                    });
            timer.setRepeats(false);
            timer.start();
        });
    }

    void updateDriverFields() {
        DatabaseType databaseType = getDatabaseType();
        boolean allowBuiltInLibrary = isBuiltInLibrarySupported(databaseType);

        driverSourceComboBox.setEnabled(allowBuiltInLibrary);
        if (!allowBuiltInLibrary) {
            ComboBoxUtil.setSelection(driverSourceComboBox, DriverSource.EXTERNAL);
        }
        //driverSourceLabel.setVisible(allowBuiltInLibrary);

        String error = null;
        boolean externalDriver = getDriverSource() == DriverSource.EXTERNAL;
        driverLibraryLabel.setVisible(externalDriver);
        driverLibraryTextField.setVisible(externalDriver);
        driverLabel.setVisible(externalDriver);
        driverComboBox.setVisible(externalDriver);
        updateDriverReloadLink();

        if (externalDriver) {
            String driverLibrary = getDriverLibrary();

            boolean fileExists = StringUtil.isNotEmpty(driverLibrary) && fileExists(driverLibrary);
            JTextField libraryTextField = driverLibraryTextField.getTextField();
            if (fileExists) {
                libraryTextField.setForeground(UIUtil.getTextFieldForeground());
                DatabaseType libraryDatabaseType = DatabaseType.resolve(driverLibrary);
                if (isBuiltInLibrarySupported(databaseType) && libraryDatabaseType != getDatabaseType()) {
                    error = "The driver library does not match the selected database type";
                    initComboBox(driverComboBox);
                    setSelection(driverComboBox, null);
                } else {
                    DatabaseDriverManager driverManager = DatabaseDriverManager.getInstance();
                    List<Driver> drivers = null;
                    try {
                        drivers = driverManager.loadDrivers(new File(driverLibrary), false);
                    } catch (Exception e) {
                        MessageUtil.showErrorDialog(getProject(), "");
                        e.printStackTrace(); // TODO
                    }
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
                libraryTextField.setForeground(JBColor.RED);
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

    private void updateDriverReloadLink() {
        reloadDriversLink.setVisible(
                getDriverSource() == DriverSource.EXTERNAL &&
                        isDriverLibraryAccessible());
    }

    private DriverSource getDriverSource() {
        DatabaseType databaseType = getDatabaseType();
        boolean allowBuiltInLibrary = isBuiltInLibrarySupported(databaseType);
        return allowBuiltInLibrary ? getSelection(driverSourceComboBox) : DriverSource.EXTERNAL;
    }

    private boolean isBuiltInLibrarySupported(DatabaseType databaseType) {
        return databaseType != DatabaseType.GENERIC;
    }

    private boolean isDriverLibraryAccessible() {
        String driverLibrary = getDriverLibrary();
        return StringUtil.isNotEmpty(driverLibrary) && new File(driverLibrary).exists();
    }

    private String getDriverLibrary() {
        return driverLibraryTextField.getTextField().getText();
    }

    private DatabaseType getDatabaseType() {
        return ensureParentComponent().getSelectedDatabaseType();
    }

    private static boolean fileExists(String driverLibrary) {
        return driverLibrary != null && new File(driverLibrary).exists();
    }

    TextFieldWithBrowseButton getDriverLibraryTextField() {
        return driverLibraryTextField;
    }

    JComboBox<DriverOption> getDriverComboBox() {
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

    JComboBox<DriverSource> getDriverSourceComboBox() {
        return driverSourceComboBox;
    }
}

