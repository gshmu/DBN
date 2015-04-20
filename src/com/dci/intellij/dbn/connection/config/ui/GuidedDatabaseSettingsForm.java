package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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

public class GuidedDatabaseSettingsForm extends ConnectionDatabaseSettingsForm<GuidedDatabaseSettings> {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField userTextField;
    private JTextField hostTextField;
    private TextFieldWithBrowseButton driverLibraryTextField;
    private DBNComboBox<DriverOption> driverComboBox;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox emptyPasswordCheckBox;
    private JCheckBox activeCheckBox;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JLabel databaseTypeLabel;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public GuidedDatabaseSettingsForm(GuidedDatabaseSettings configuration) {
        super(configuration);
        Project project = configuration.getProject();

        resetFormChanges();
        registerComponent(mainPanel);
        DatabaseType databaseType = configuration.getDatabaseType();
        databaseTypeLabel.setText(databaseType.getName());
        databaseTypeLabel.setIcon(databaseType.getIcon());

        driverLibraryTextField.addBrowseFolderListener(
                "Select driver library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                project, LIBRARY_FILE_DESCRIPTOR);

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

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges(GuidedDatabaseSettings connectionConfig){
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
        final GuidedDatabaseSettings connectionConfig = getConfiguration();

        final boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !CommonUtil.safeEqual(connectionConfig.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !CommonUtil.safeEqual(connectionConfig.getHost(), hostTextField.getText()) ||
                !CommonUtil.safeEqual(connectionConfig.getPort(), portTextField.getText()) ||
                !CommonUtil.safeEqual(connectionConfig.getDatabase(), databaseTextField.getText()) ||
                !CommonUtil.safeEqual(connectionConfig.getAuthentication().getUser(), userTextField.getText());


        applyChanges(connectionConfig);

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (settingsChanged) {
                    Project project = connectionConfig.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.settingsChanged(connectionConfig.getConnectionId());
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

        populateDriverList(connectionConfig.getDriverLibrary());
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), connectionConfig.getDriver()));
    }
}

