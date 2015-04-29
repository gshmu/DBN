package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNCollapsiblePanel;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.config.GuidedDatabaseSettings;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;

public class GuidedDatabaseSettingsForm extends ConnectionDatabaseSettingsForm<GuidedDatabaseSettings> {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField userTextField;
    private JTextField hostTextField;
    private DBNComboBox<DatabaseType> databaseTypeComboBox;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox emptyPasswordCheckBox;
    private JCheckBox activeCheckBox;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JPanel driverLibraryPanel;

    private ConnectionDriverSettingsForm<GuidedDatabaseSettingsForm> driverSettingsForm;

    public GuidedDatabaseSettingsForm(GuidedDatabaseSettings configuration) {
        super(configuration);

        DatabaseType databaseType = configuration.getDatabaseType();
        databaseTypeComboBox.setValues(databaseType);
        databaseTypeComboBox.setSelectedValue(databaseType);
        databaseTypeComboBox.setEnabled(false);

        driverSettingsForm = new ConnectionDriverSettingsForm<GuidedDatabaseSettingsForm>(this);

        boolean externalLibrary = configuration.getDriverSource() == DriverSource.EXTERNAL;
        DBNCollapsiblePanel<GuidedDatabaseSettingsForm> driverPanel = new DBNCollapsiblePanel<GuidedDatabaseSettingsForm>(this, driverSettingsForm.getComponent(), "Driver", externalLibrary);
        driverLibraryPanel.add(driverPanel.getComponent(), BorderLayout.CENTER);

        resetFormChanges();
        registerComponent(mainPanel);
        updateAuthenticationFields();
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
        return driverSettingsForm.getDriverLibraryTextField();
    }

    @Override
    protected DBNComboBox<DriverSource> getDriverSourceComboBox() {
        return driverSettingsForm.getDriverSourceComboBox();
    }

    protected DBNComboBox<DriverOption> getDriverComboBox() {
        return driverSettingsForm.getDriverComboBox();
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
        return driverSettingsForm.getDriverErrorLabel();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges(final GuidedDatabaseSettings configuration){
        DBNComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        configuration.setActive(activeCheckBox.isSelected());
        String newName = nameTextField.getText();
        final boolean nameChanged = !newName.equals(configuration.getName());
        configuration.setName(newName);

        configuration.setDescription(descriptionTextField.getText());
        configuration.setDriverLibrary(driverLibraryTextField.getText());
        configuration.setDriver(driverComboBox.getSelectedValue() == null ? null : driverComboBox.getSelectedValue().getName());
        configuration.setHost(hostTextField.getText());
        configuration.setPort(portTextField.getText());
        configuration.setDatabase(databaseTextField.getText());

        Authentication authentication = configuration.getAuthentication();
        authentication.setUser(userTextField.getText());
        authentication.setPassword(new String(passwordField.getPassword()));
        authentication.setOsAuthentication(osAuthenticationCheckBox.isSelected());
        authentication.setEmptyPassword(emptyPasswordCheckBox.isSelected());

        configuration.setConnectivityStatus(temporaryConfig.getConnectivityStatus());
        configuration.setDriverSource(driverSourceComboBox.getSelectedValue());

        configuration.updateHashCode();

        new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (nameChanged) {
                    Project project = configuration.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.nameChanged(configuration.getConnectionId());
                }
            }
        };
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateStringInputValue(nameTextField, "Name", true);
        final GuidedDatabaseSettings configuration = getConfiguration();

        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

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
        DBNComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        GuidedDatabaseSettings connectionConfig = getConfiguration();

        activeCheckBox.setSelected(connectionConfig.isActive());
        nameTextField.setText(connectionConfig.getDisplayName());
        descriptionTextField.setText(connectionConfig.getDescription());
        hostTextField.setText(connectionConfig.getHost());
        portTextField.setText(connectionConfig.getPort());
        databaseTextField.setText(connectionConfig.getDatabase());

        Authentication authentication = connectionConfig.getAuthentication();
        userTextField.setText(authentication.getUser());
        passwordField.setText(authentication.getPassword());
        osAuthenticationCheckBox.setSelected(authentication.isOsAuthentication());
        emptyPasswordCheckBox.setSelected(authentication.isEmptyPassword());

        driverSourceComboBox.setSelectedValue(connectionConfig.getDriverSource());
        driverLibraryTextField.setText(connectionConfig.getDriverLibrary());
        updateDriverFields();
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), connectionConfig.getDriver()));
    }
}

