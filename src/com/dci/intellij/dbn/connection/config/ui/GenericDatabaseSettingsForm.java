package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.config.GenericDatabaseSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

public class GenericDatabaseSettingsForm extends ConnectionDatabaseSettingsForm<GenericDatabaseSettings>{
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField userTextField;
    private JTextField urlTextField;
    private TextFieldWithBrowseButton driverLibraryTextField;
    private DBNComboBox<DatabaseType> databaseTypeComboBox;
    private DBNComboBox<DriverOption> driverComboBox;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox emptyPasswordCheckBox;
    private JCheckBox activeCheckBox;
    private JLabel driverErrorLabel;

    private GenericDatabaseSettings temporaryConfig;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public GenericDatabaseSettingsForm(GenericDatabaseSettings connectionConfig) {
        super(connectionConfig);
        Project project = connectionConfig.getProject();
        temporaryConfig = connectionConfig.clone();
        databaseTypeComboBox.setValues(
                DatabaseType.ORACLE,
                DatabaseType.MYSQL,
                DatabaseType.POSTGRES);

        databaseTypeComboBox.addListener(new ValueSelectorListener<DatabaseType>() {
            @Override
            public void valueSelected(DatabaseType value) {
                String url = urlTextField.getText();
                String urlPattern = value.getUrlResolver().getDefaultUrl();
                if (StringUtil.isEmpty(url)) {
                    urlTextField.setText(urlPattern);
                } else {
                    for (DatabaseType databaseType : DatabaseType.values()) {
                        if (url.trim().equalsIgnoreCase(databaseType.getUrlResolver().getDefaultUrl())) {
                            urlTextField.setText(urlPattern);
                            break;
                        }
                    }
                }
                updateDriverFields();
            }
        });

        resetFormChanges();

        registerComponent(mainPanel);

        driverLibraryTextField.addBrowseFolderListener(
                "Select driver library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                project, LIBRARY_FILE_DESCRIPTOR);

        updateAuthenticationFields();
    }

    @Override
    protected GenericDatabaseSettings createConfig(ConnectionSettings configuration) {
        return new GenericDatabaseSettings(configuration);
    }

    protected JCheckBox getActiveCheckBox() {
        return activeCheckBox;
    }

    protected JTextField getNameTextField() {
        return nameTextField;
    }

    @Override
    protected TextFieldWithBrowseButton getDriverLibraryTextField() {
        return driverLibraryTextField;
    }

    protected DBNComboBox<DriverOption> getDriverComboBox() {
        return driverComboBox;
    }

    @Override
    protected JTextField getUserTextField() {
        return userTextField;
    }

    @Override
    protected JPasswordField getPasswordField() {
        return passwordField;
    }

    protected JCheckBox getOsAuthenticationCheckBox() {
        return osAuthenticationCheckBox;
    }

    protected JCheckBox getEmptyPasswordCheckBox() {
        return emptyPasswordCheckBox;
    }

    public JLabel getDriverErrorLabel() {
        return driverErrorLabel;
    }

    @Override
    public DBNComboBox<DatabaseType> getDatabaseTypeComboBox() {
        return databaseTypeComboBox;
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges(GenericDatabaseSettings configuration){
        configuration.setActive(activeCheckBox.isSelected());
        configuration.setDatabaseType(databaseTypeComboBox.getSelectedValue());
        configuration.setName(nameTextField.getText());
        configuration.setDescription(descriptionTextField.getText());
        configuration.setDriverLibrary(driverLibraryTextField.getText());
        configuration.setDriver(driverComboBox.getSelectedValue() == null ? null : driverComboBox.getSelectedValue().getName());
        configuration.setConnectionUrl(urlTextField.getText());

        Authentication authentication = configuration.getAuthentication();
        authentication.setUser(userTextField.getText());
        authentication.setPassword(new String(passwordField.getPassword()));
        authentication.setOsAuthentication(osAuthenticationCheckBox.isSelected());
        authentication.setEmptyPassword(emptyPasswordCheckBox.isSelected());

        configuration.setConnectivityStatus(temporaryConfig.getConnectivityStatus());
        configuration.updateHashCode();
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateStringInputValue(nameTextField, "Name", true);
        DatabaseType selectedDatabaseType = databaseTypeComboBox.getSelectedValue();
        if (selectedDatabaseType == null) {
            throw new ConfigurationException("Database type not selected");
        } else {
            DriverOption selectedDriver = driverComboBox.getSelectedValue();
            DatabaseType driverDatabaseType = selectedDriver == null ? null : DatabaseType.resolve(selectedDriver.getName());
            if (driverDatabaseType != null && driverDatabaseType != selectedDatabaseType) {
                throw new ConfigurationException("Entered driver library does not match the selected database type");
            }

            databaseTypeComboBox.setEnabled(false);
        }


        final GenericDatabaseSettings configuration = getConfiguration();

        final boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !CommonUtil.safeEqual(configuration.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getConnectionUrl(), urlTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getAuthentication().getUser(), userTextField.getText());


        applyFormChanges(configuration);

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (settingsChanged) {
                    Project project = configuration.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.settingsChanged(configuration.getConnectionId());
                }
            }
        };
    }


    public void resetFormChanges() {
        GenericDatabaseSettings configuration = getConfiguration();

        DatabaseType databaseType = configuration.getDatabaseType();
        if (databaseType != DatabaseType.UNKNOWN) {
            databaseTypeComboBox.setSelectedValue(databaseType);
            databaseTypeComboBox.setEnabled(false);
        }

        activeCheckBox.setSelected(configuration.isActive());
        nameTextField.setText(configuration.getDisplayName());
        descriptionTextField.setText(configuration.getDescription());
        driverLibraryTextField.setText(configuration.getDriverLibrary());
        urlTextField.setText(configuration.getConnectionUrl());

        Authentication authentication = configuration.getAuthentication();
        userTextField.setText(authentication.getUser());
        passwordField.setText(authentication.getPassword());
        osAuthenticationCheckBox.setSelected(authentication.isOsAuthentication());
        emptyPasswordCheckBox.setSelected(authentication.isEmptyPassword());

        updateDriverFields();
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), configuration.getDriver()));
    }
}

