package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.config.GuidedDatabaseSettings;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class GuidedDatabaseSettingsForm extends ConnectionDatabaseSettingsForm<GuidedDatabaseSettings> {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField userTextField;
    private JTextField hostTextField;
    private TextFieldWithBrowseButton driverLibraryTextField;
    private DBNComboBox<DriverOption> driverComboBox;
    private DBNComboBox<DatabaseType> databaseTypeComboBox;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox emptyPasswordCheckBox;
    private JCheckBox activeCheckBox;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JLabel driverErrorLabel;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public GuidedDatabaseSettingsForm(GuidedDatabaseSettings configuration) {
        super(configuration);
        Project project = configuration.getProject();

        DatabaseType databaseType = configuration.getDatabaseType();
        databaseTypeComboBox.setValues(databaseType);
        databaseTypeComboBox.setSelectedValue(databaseType);
        databaseTypeComboBox.setEnabled(false);


        resetFormChanges();
        registerComponent(mainPanel);
        updateAuthenticationFields();
        driverLibraryTextField.addBrowseFolderListener(
                "Select driver library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                project, LIBRARY_FILE_DESCRIPTOR);
    }

    @Override
    protected GuidedDatabaseSettings createConfig(ConnectionSettings configuration) {
        return new GuidedDatabaseSettings(configuration, getConfiguration().getDatabaseType());
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
    public DBNComboBox<DatabaseType> getDatabaseTypeComboBox() {
        return databaseTypeComboBox;
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

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges(GuidedDatabaseSettings connectionConfig){
        connectionConfig.setActive(activeCheckBox.isSelected());
        connectionConfig.setName(nameTextField.getText());
        connectionConfig.setDescription(descriptionTextField.getText());
        connectionConfig.setDriverLibrary(driverLibraryTextField.getText());
        connectionConfig.setDriver(driverComboBox.getSelectedValue() == null ? null : driverComboBox.getSelectedValue().getName());
        connectionConfig.setHost(hostTextField.getText());
        connectionConfig.setPort(portTextField.getText());
        connectionConfig.setDatabase(databaseTextField.getText());

        Authentication authentication = connectionConfig.getAuthentication();
        authentication.setUser(userTextField.getText());
        authentication.setPassword(new String(passwordField.getPassword()));
        authentication.setOsAuthentication(osAuthenticationCheckBox.isSelected());
        authentication.setEmptyPassword(emptyPasswordCheckBox.isSelected());

        connectionConfig.setConnectivityStatus(temporaryConfig.getConnectivityStatus());
        connectionConfig.updateHashCode();
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateStringInputValue(nameTextField, "Name", true);
        final GuidedDatabaseSettings configuration = getConfiguration();

        DatabaseType selectedDatabaseType = configuration.getDatabaseType();
        DriverOption selectedDriver = driverComboBox.getSelectedValue();
        DatabaseType driverDatabaseType = selectedDriver == null ? null : DatabaseType.resolve(selectedDriver.getName());
        if (driverDatabaseType != null && driverDatabaseType != selectedDatabaseType) {
            throw new ConfigurationException("The provided driver library is not a valid " + selectedDatabaseType.getDisplayName() + " driver library.");
        }

        final boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !CommonUtil.safeEqual(configuration.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getHost(), hostTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getPort(), portTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getDatabase(), databaseTextField.getText()) ||
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
        GuidedDatabaseSettings connectionConfig = getConfiguration();

        activeCheckBox.setSelected(connectionConfig.isActive());
        nameTextField.setText(connectionConfig.getDisplayName());
        descriptionTextField.setText(connectionConfig.getDescription());
        driverLibraryTextField.setText(connectionConfig.getDriverLibrary());
        hostTextField.setText(connectionConfig.getHost());
        portTextField.setText(connectionConfig.getPort());
        databaseTextField.setText(connectionConfig.getDatabase());

        Authentication authentication = connectionConfig.getAuthentication();
        userTextField.setText(authentication.getUser());
        passwordField.setText(authentication.getPassword());
        osAuthenticationCheckBox.setSelected(authentication.isOsAuthentication());
        emptyPasswordCheckBox.setSelected(authentication.isEmptyPassword());

        updateDriverFields();
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), connectionConfig.getDriver()));
    }
}

