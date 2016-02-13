package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlPattern;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ConnectionDatabaseSettingsForm extends ConfigurationEditorForm<ConnectionDatabaseSettings> {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField hostTextField;
    private DBNComboBox<DatabaseType> databaseTypeComboBox;
    private DBNComboBox<DatabaseUrlType> urlTypeComboBox;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JPanel driverLibraryPanel;
    private JLabel databaseTypeLabel;
    private JPanel authenticationPanel;
    private JTextField urlTextField;
    private TextFieldWithBrowseButton fileTextField;
    private JPanel databaseInfoPanel;
    private JPanel urlPanel;
    private JPanel filePanel;

    private ConnectionDriverSettingsForm driverSettingsForm;
    private ConnectionAuthenticationSettingsForm authenticationSettingsForm;

    public ConnectionDatabaseSettingsForm(final ConnectionDatabaseSettings configuration) {
        super(configuration);

        fileTextField.addBrowseFolderListener("Select Database File", null, null, new FileChooserDescriptor(true, false, false, false, false, false));

        final ConnectionConfigType configType = configuration.getConfigType();
        updateFieldVisibility(configType, configuration.getDatabaseType());

        DatabaseType databaseType = configuration.getDatabaseType();
        if (databaseType == DatabaseType.UNKNOWN) {
            databaseTypeComboBox.setValues(
                    DatabaseType.ORACLE,
                    DatabaseType.MYSQL,
                    DatabaseType.POSTGRES,
                    DatabaseType.SQLITE);
            databaseTypeComboBox.addListener(new ValueSelectorListener<DatabaseType>() {
                @Override
                public void selectionChanged(DatabaseType oldValue, DatabaseType newValue) {
                    DatabaseUrlPattern oldUrlPattern = oldValue == null ? null : oldValue.getDefaultUrlPattern();
                    DatabaseUrlPattern newUrlPattern = newValue.getDefaultUrlPattern();
                    updateFieldVisibility(configType, newValue);
                    if (configType == ConnectionConfigType.BASIC) {
                        if (newUrlPattern.getUrlType() == DatabaseUrlType.FILE) {
                            String file = fileTextField.getText();
                            DatabaseInfo defaults = newUrlPattern.getDefaultInfo();
                            DatabaseInfo oldDefaults = oldUrlPattern == null ? null : oldUrlPattern.getDefaultInfo();
                            if (StringUtil.isEmpty(file) || (oldDefaults != null && oldDefaults.getFile().equals(file))) {
                                fileTextField.setText(defaults.getFile());
                            }
                        } else {
                            String host = hostTextField.getText();
                            String port = portTextField.getText();
                            String database = databaseTextField.getText();

                            DatabaseInfo defaults = newUrlPattern.getDefaultInfo();
                            DatabaseInfo oldDefaults = oldUrlPattern == null ? null : oldUrlPattern.getDefaultInfo();
                            if (StringUtil.isEmpty(host) || (oldDefaults != null && oldDefaults.getHost().equals(host))) {
                                hostTextField.setText(defaults.getHost());
                            }

                            if (StringUtil.isEmpty(port) || (oldDefaults != null && oldDefaults.getPort().equals(port))) {
                                portTextField.setText(defaults.getPort());
                            }
                            if (StringUtil.isEmpty(database) || (oldDefaults != null && oldDefaults.getDatabase().equals(database))) {
                                databaseTextField.setText(defaults.getDatabase());
                            }
                            DatabaseUrlType[] urlTypes = newValue.getUrlTypes();
                            urlTypeComboBox.setValues(urlTypes);
                            urlTypeComboBox.setSelectedValue(urlTypes[0]);
                            urlTypeComboBox.setVisible(urlTypes.length > 1);
                        }
                    } else {
                        if (oldUrlPattern == null || oldUrlPattern.getDefaultUrl().equals(urlTextField.getText())) {
                            urlTextField.setText(newUrlPattern.getDefaultUrl());
                        }
                    }

                    driverSettingsForm.updateDriverFields();
                }
            });
        } else {
            databaseTypeLabel.setText(databaseType.getName());
            databaseTypeLabel.setIcon(databaseType.getIcon());
            databaseTypeComboBox.setValues(databaseType);
            databaseTypeComboBox.setSelectedValue(databaseType);
            databaseTypeComboBox.setEnabled(false);
            databaseTypeComboBox.setVisible(false);

            DatabaseUrlType[] urlTypes = databaseType.getUrlTypes();
            urlTypeComboBox.setValues(urlTypes);
            urlTypeComboBox.setSelectedValue(urlTypes[0]);
            urlTypeComboBox.setVisible(urlTypes.length > 1);
        }

        authenticationSettingsForm = new ConnectionAuthenticationSettingsForm(this);
        //DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> authenticationSettingsPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, authenticationSettingsForm.getComponent(), "Authentication", true);
        authenticationPanel.add(authenticationSettingsForm.getComponent(), BorderLayout.CENTER);

        driverSettingsForm = new ConnectionDriverSettingsForm(this);
        boolean externalLibrary = configuration.getDriverSource() == DriverSource.EXTERNAL;
        //DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> driverPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, driverSettingsForm.getComponent(), "Driver", externalLibrary);
        driverLibraryPanel.add(driverSettingsForm.getComponent(), BorderLayout.CENTER);

        resetFormChanges();
        registerComponent(mainPanel);
    }

    private void updateFieldVisibility(ConnectionConfigType configType, DatabaseType databaseType) {
        if (configType == ConnectionConfigType.BASIC) {
            urlPanel.setVisible(false);
            DatabaseUrlType urlType = databaseType.getDefaultUrlPattern().getUrlType();
            boolean isFileUrlType = urlType == DatabaseUrlType.FILE;
            filePanel.setVisible(isFileUrlType);
            databaseInfoPanel.setVisible(!isFileUrlType);
        } else if (configType == ConnectionConfigType.CUSTOM){
            urlPanel.setVisible(true);
            filePanel.setVisible(false);
            databaseInfoPanel.setVisible(false);
        }
        authenticationPanel.setVisible(databaseType.isAuthenticationSupported());
    }

    public void notifyPresentationChanges() {
        ConnectionDatabaseSettings configuration = getConfiguration();
        String name = nameTextField.getText();
        ConnectivityStatus connectivityStatus = configuration.getConnectivityStatus();
        ConnectionSettings connectionSettings = configuration.getParent();
        ConnectionSettingsForm connectionSettingsForm = connectionSettings.getSettingsEditor();

        Icon icon = connectionSettings.isNew() ? Icons.CONNECTION_NEW :
                connectionSettingsForm != null && !connectionSettingsForm.isConnectionActive() ? Icons.CONNECTION_DISABLED :
                        connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
                        connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        ConnectionPresentationChangeListener listener = EventUtil.notify(configuration.getProject(), ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = connectionSettings.getDetailSettings().getEnvironmentType();
        listener.presentationChanged(name, icon, environmentType.getColor(), getConfiguration().getConnectionId(), configuration.getDatabaseType());
    }

    //protected abstract ConnectionDatabaseSettings createConfig(ConnectionSettings configuration);

    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                ConnectionDatabaseSettings configuration = getConfiguration();
                configuration.setModified(true);

                Document document = e.getDocument();

                if (document == driverSettingsForm.getDriverLibraryTextField().getTextField().getDocument()) {
                    driverSettingsForm.updateDriverFields();
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


    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                ConnectionDatabaseSettings configuration = getConfiguration();
                configuration.setModified(true);
                if (source == nameTextField) {
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

        DatabaseType databaseType = CommonUtil.nvl(databaseTypeComboBox.getSelectedValue(), configuration.getDatabaseType());
        DriverOption driverOption = driverComboBox.getSelectedValue();
        DatabaseUrlType urlType = CommonUtil.nvl(urlTypeComboBox.getSelectedValue(), DatabaseUrlType.DATABASE);

        configuration.setDatabaseType(databaseType);
        configuration.setName(nameTextField.getText());
        configuration.setDescription(descriptionTextField.getText());
        configuration.setDriverLibrary(driverLibraryTextField.getText());
        configuration.setDriver(driverOption == null ? null : driverOption.getName());
        configuration.setUrlPattern(DatabaseUrlPattern.get(databaseType, urlType));
        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        databaseInfo.setHost(hostTextField.getText());
        databaseInfo.setPort(portTextField.getText());
        databaseInfo.setDatabase(databaseTextField.getText());
        databaseInfo.setUrl(urlTextField.getText());
        databaseInfo.setUrlType(urlType);
        databaseInfo.setFile(fileTextField.getText());

        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();
        authenticationSettingsForm.applyFormChanges(authenticationInfo);

        configuration.setDriverSource(driverSourceComboBox.getSelectedValue());
        configuration.updateHashCode();
    }

    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateStringInputValue(nameTextField, "Name", true);
        final ConnectionDatabaseSettings configuration = getConfiguration();

        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        DatabaseType selectedDatabaseType = CommonUtil.nvl(databaseTypeComboBox.getSelectedValue(), configuration.getDatabaseType());
        DriverOption selectedDriver = driverComboBox.getSelectedValue();
        DatabaseType driverDatabaseType = selectedDriver == null ? null : DatabaseType.resolve(selectedDriver.getName());
        if (driverDatabaseType != null && driverDatabaseType != selectedDatabaseType) {
            throw new ConfigurationException("The provided driver library is not a valid " + selectedDatabaseType.getDisplayName() + " driver library.");
        }

        final boolean nameChanged = !nameTextField.getText().equals(configuration.getName());

        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        final boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !CommonUtil.safeEqual(configuration.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !CommonUtil.safeEqual(databaseInfo.getHost(), hostTextField.getText()) ||
                !CommonUtil.safeEqual(databaseInfo.getPort(), portTextField.getText()) ||
                !CommonUtil.safeEqual(databaseInfo.getDatabase(), databaseTextField.getText()) ||
                !CommonUtil.safeEqual(databaseInfo.getUrlType(), urlTypeComboBox.getSelectedValue()) ||
                !CommonUtil.safeEqual(databaseInfo.getFile(), fileTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getAuthenticationInfo().getUser(), authenticationSettingsForm.getUserTextField().getText());


        applyFormChanges(configuration);

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (nameChanged) {
                    Project project = configuration.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.connectionNameChanged(configuration.getConnectionId());
                }

                if (settingsChanged) {
                    Project project = configuration.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.connectionChanged(configuration.getConnectionId());
                }
            }
        };
    }


    public void resetFormChanges() {
        DBNComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        DBNComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        ConnectionDatabaseSettings configuration = getConfiguration();

        nameTextField.setText(configuration.getDisplayName());
        descriptionTextField.setText(configuration.getDescription());
        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        urlTextField.setText(databaseInfo.getUrl());
        fileTextField.setText(databaseInfo.getFile());
        hostTextField.setText(databaseInfo.getHost());
        portTextField.setText(databaseInfo.getPort());
        databaseTextField.setText(databaseInfo.getDatabase());
        DatabaseType databaseType = configuration.getDatabaseType();
        if (databaseType != DatabaseType.UNKNOWN) {
            databaseTypeComboBox.setSelectedValue(databaseType);
        }

        DatabaseUrlType[] urlTypes = databaseType.getUrlTypes();
        urlTypeComboBox.setValues(urlTypes);
        urlTypeComboBox.setSelectedValue(databaseInfo.getUrlType());
        urlTypeComboBox.setVisible(urlTypes.length > 1);

        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();
        authenticationSettingsForm.resetFormChanges(authenticationInfo);

        driverSourceComboBox.setSelectedValue(configuration.getDriverSource());
        driverLibraryTextField.setText(configuration.getDriverLibrary());
        driverSettingsForm.updateDriverFields();
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), configuration.getDriver()));
    }

    public DatabaseType getSelectedDatabaseType() {
        return databaseTypeComboBox.getSelectedValue();
    }
}

