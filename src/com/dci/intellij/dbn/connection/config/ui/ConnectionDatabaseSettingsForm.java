package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNCollapsiblePanel;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.Authentication;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

public class ConnectionDatabaseSettingsForm extends ConfigurationEditorForm<ConnectionDatabaseSettings> {
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

    protected ConnectionDatabaseSettings temporaryConfig;

    private ConnectionDriverSettingsForm driverSettingsForm;

    public ConnectionDatabaseSettingsForm(ConnectionDatabaseSettings configuration) {
        super(configuration);
        temporaryConfig = configuration.clone();

        DatabaseType databaseType = configuration.getDatabaseType();
        if (databaseType == DatabaseType.UNKNOWN) {
            databaseTypeComboBox.setValues(
                    DatabaseType.ORACLE,
                    DatabaseType.MYSQL,
                    DatabaseType.POSTGRES);
            databaseTypeComboBox.addListener(new ValueSelectorListener<DatabaseType>() {
                @Override
                public void valueSelected(DatabaseType databaseType) {
                    if (StringUtil.isEmpty(hostTextField.getText())) {
                        hostTextField.setText(databaseType.getUrlResolver().getDefaultHost());
                    }
                    if (StringUtil.isEmpty(portTextField.getText())) {
                        hostTextField.setText(databaseType.getUrlResolver().getDefaultPort());
                    }
                    if (StringUtil.isEmpty(databaseTextField.getText())) {
                        hostTextField.setText(databaseType.getUrlResolver().getDefaultDatabase());
                    }
                    updateDriverFields();
                }
            });
        } else {
            databaseTypeComboBox.setValues(databaseType);
            databaseTypeComboBox.setSelectedValue(databaseType);
            databaseTypeComboBox.setEnabled(false);
        }

        driverSettingsForm = new ConnectionDriverSettingsForm(this);

        boolean externalLibrary = configuration.getDriverSource() == DriverSource.EXTERNAL;
        DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> driverPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, driverSettingsForm.getComponent(), "Driver", externalLibrary);
        driverLibraryPanel.add(driverPanel.getComponent(), BorderLayout.CENTER);

        resetFormChanges();
        registerComponent(mainPanel);
        updateAuthenticationFields();
    }

    public void notifyPresentationChanges() {
        ConnectionDatabaseSettings configuration = temporaryConfig;//getConfiguration();
        String name = nameTextField.getText();
        ConnectivityStatus connectivityStatus = configuration.getConnectivityStatus();
        Icon icon = configuration.getParent().isNew() ? Icons.CONNECTION_NEW :
                !activeCheckBox.isSelected() ? Icons.CONNECTION_DISABLED :
                        connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
                                connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        ConnectionPresentationChangeListener listener = EventUtil.notify(configuration.getProject(), ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = configuration.getParent().getDetailSettings().getEnvironmentType();
        listener.presentationChanged(name, icon, environmentType.getColor(), getConfiguration().getConnectionId(), configuration.getDatabaseType());
    }

    public boolean isConnectionActive() {
        return activeCheckBox.isSelected();
    }

    public ConnectionDatabaseSettings getTemporaryConfig() {
        return temporaryConfig;
    }

    public void setTemporaryConfig(ConnectionDatabaseSettings temporaryConfig) {
        this.temporaryConfig = temporaryConfig;
    }

    public ConnectivityStatus getConnectivityStatus() {
        return temporaryConfig.getConnectivityStatus();
    }

    //protected abstract ConnectionDatabaseSettings createConfig(ConnectionSettings configuration);

    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                ConnectionDatabaseSettings configuration = getConfiguration();
                configuration.setModified(true);

                Document document = e.getDocument();

                if (document == driverSettingsForm.getDriverLibraryTextField().getTextField().getDocument()) {
                    updateDriverFields();
                }

                if (document == nameTextField.getDocument()) {
                    ConnectionBundleSettings connectionBundleSettings = configuration.getParent().getParent();
                    ConnectionBundleSettingsForm settingsEditor = connectionBundleSettings.getSettingsEditor();
                    if (settingsEditor != null) {
                        JList connectionList = settingsEditor.getList();
                        connectionList.revalidate();
                        connectionList.repaint();
                        notifyPresentationChanges();
                    }
                }
            }
        };
    }

    protected void updateAuthenticationFields() {
        boolean isOsAuthentication = osAuthenticationCheckBox.isSelected();
        boolean isEmptyPassword = emptyPasswordCheckBox.isSelected();
        userTextField.setEnabled(!isOsAuthentication);

        passwordField.setEnabled(!isOsAuthentication && !emptyPasswordCheckBox.isSelected());
        passwordField.setBackground(isOsAuthentication || isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());
        emptyPasswordCheckBox.setEnabled(!isOsAuthentication);
    }


    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                ConnectionDatabaseSettings configuration = getConfiguration();
                configuration.setModified(true);

                if (source == osAuthenticationCheckBox || source == emptyPasswordCheckBox) {
                    boolean isOsAuthentication = osAuthenticationCheckBox.isSelected();
                    boolean isEmptyPassword = emptyPasswordCheckBox.isSelected();
                    userTextField.setEnabled(!isOsAuthentication);

                    passwordField.setEnabled(!isOsAuthentication && !isEmptyPassword);
                    passwordField.setBackground(isOsAuthentication || isEmptyPassword ? UIUtil.getPanelBackground() : UIUtil.getTextFieldBackground());
                    emptyPasswordCheckBox.setEnabled(!isOsAuthentication);

                    if (isOsAuthentication || isEmptyPassword) {
                        passwordField.setText("");
                    }
                    if (isOsAuthentication) {
                        userTextField.setText("");
                        emptyPasswordCheckBox.setSelected(false);
                    }
                }

                if (source == activeCheckBox || source == nameTextField) {
                    ConnectionBundleSettings connectionBundleSettings = configuration.getParent().getParent();
                    ConnectionBundleSettingsForm settingsEditor = connectionBundleSettings.getSettingsEditor();

                    if (settingsEditor != null) {
                        JList connectionList = settingsEditor.getList();
                        connectionList.revalidate();
                        connectionList.repaint();
                        notifyPresentationChanges();
                    }
                }
            }
        };
    }

    protected void updateDriverFields() {
        DBNComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        DriverSource driverSource = driverSourceComboBox == null ? DriverSource.EXTERNAL : driverSourceComboBox.getSelectedValue();

        String error = null;
        if (driverSource == DriverSource.EXTERNAL) {
            JTextField textField = driverSettingsForm.getDriverLibraryTextField().getTextField();
            String driverLibrary = textField.getText();

            boolean fileExists = StringUtil.isNotEmpty(driverLibrary) && fileExists(driverLibrary);
            DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();
            if (fileExists) {
                textField.setForeground(UIUtil.getTextFieldForeground());
                DatabaseType databaseType = DatabaseType.resolve(driverLibrary);
                if (databaseType != DatabaseType.UNKNOWN && databaseType != databaseTypeComboBox.getSelectedValue()) {
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


        JLabel driverErrorLabel = driverSettingsForm.getDriverErrorLabel();
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

    public String getConnectionName() {
        return nameTextField.getText();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges(final ConnectionDatabaseSettings configuration){
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
        final ConnectionDatabaseSettings configuration = getConfiguration();

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

        ConnectionDatabaseSettings connectionConfig = getConfiguration();

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

