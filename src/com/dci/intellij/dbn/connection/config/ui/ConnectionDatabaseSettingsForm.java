package com.dci.intellij.dbn.connection.config.ui;

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
import com.dci.intellij.dbn.connection.DatabaseUrlResolver;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.driver.DriverSource;
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
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JPanel driverLibraryPanel;
    private JLabel databaseTypeLabel;
    private JPanel authenticationPanel;

    private ConnectionDriverSettingsForm driverSettingsForm;
    private ConnectionAuthenticationSettingsForm authenticationSettingsForm;

    public ConnectionDatabaseSettingsForm(ConnectionDatabaseSettings configuration) {
        super(configuration);

        DatabaseType databaseType = configuration.getDatabaseType();
        if (databaseType == DatabaseType.UNKNOWN) {
            databaseTypeComboBox.setValues(
                    DatabaseType.ORACLE,
                    DatabaseType.MYSQL,
                    DatabaseType.POSTGRES);
            databaseTypeComboBox.addListener(new ValueSelectorListener<DatabaseType>() {
                @Override
                public void selectionChanged(DatabaseType oldValue, DatabaseType newValue) {
                    DatabaseUrlResolver oldUrlResolver = oldValue == null ? null : oldValue.getUrlResolver();
                    DatabaseUrlResolver urlResolver = newValue.getUrlResolver();
                    String host = hostTextField.getText();
                    String port = portTextField.getText();
                    String database = databaseTextField.getText();

                    if (StringUtil.isEmpty(host) || (oldUrlResolver != null && oldUrlResolver.getDefaultHost().equals(host))) {
                        hostTextField.setText(urlResolver.getDefaultHost());
                    }

                    if (StringUtil.isEmpty(port) || (oldUrlResolver != null && oldUrlResolver.getDefaultPort().equals(port))) {
                        portTextField.setText(urlResolver.getDefaultPort());
                    }
                    if (StringUtil.isEmpty(database) || (oldUrlResolver != null && oldUrlResolver.getDefaultDatabase().equals(database))) {
                        databaseTextField.setText(urlResolver.getDefaultDatabase());
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
        }

        authenticationSettingsForm = new ConnectionAuthenticationSettingsForm(this);
        DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> authenticationSettingsPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, authenticationSettingsForm.getComponent(), "Authentication", true);
        authenticationPanel.add(authenticationSettingsPanel.getComponent(), BorderLayout.CENTER);

        driverSettingsForm = new ConnectionDriverSettingsForm(this);
        boolean externalLibrary = configuration.getDriverSource() == DriverSource.EXTERNAL;
        DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> driverPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, driverSettingsForm.getComponent(), "Driver", externalLibrary);
        driverLibraryPanel.add(driverPanel.getComponent(), BorderLayout.CENTER);

        resetFormChanges();
        registerComponent(mainPanel);
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

        DatabaseType databaseType = databaseTypeComboBox.getSelectedValue();
        DriverOption driverOption = driverComboBox.getSelectedValue();

        configuration.setDatabaseType(databaseType == null ? DatabaseType.UNKNOWN : databaseType);
        configuration.setName(nameTextField.getText());
        configuration.setDescription(descriptionTextField.getText());
        configuration.setDriverLibrary(driverLibraryTextField.getText());
        configuration.setDriver(driverOption == null ? null : driverOption.getName());
        configuration.setHost(hostTextField.getText());
        configuration.setPort(portTextField.getText());
        configuration.setDatabase(databaseTextField.getText());

        Authentication authentication = configuration.getAuthentication();
        authenticationSettingsForm.applyFormChanges(authentication);

        configuration.setDriverSource(driverSourceComboBox.getSelectedValue());
        configuration.updateHashCode();
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

        final boolean nameChanged = !nameTextField.getText().equals(configuration.getName());

        final boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !CommonUtil.safeEqual(configuration.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getHost(), hostTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getPort(), portTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getDatabase(), databaseTextField.getText()) ||
                !CommonUtil.safeEqual(configuration.getAuthentication().getUser(), authenticationSettingsForm.getUserTextField().getText());


        applyFormChanges(configuration);

         new SettingsChangeNotifier() {
            @Override
            public void notifyChanges() {
                if (nameChanged) {
                    Project project = configuration.getProject();
                    ConnectionSettingsListener listener = EventUtil.notify(project, ConnectionSettingsListener.TOPIC);
                    listener.nameChanged(configuration.getConnectionId());
                }

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

        nameTextField.setText(connectionConfig.getDisplayName());
        descriptionTextField.setText(connectionConfig.getDescription());
        hostTextField.setText(connectionConfig.getHost());
        portTextField.setText(connectionConfig.getPort());
        databaseTextField.setText(connectionConfig.getDatabase());

        Authentication authentication = connectionConfig.getAuthentication();
        authenticationSettingsForm.resetFormChanges(authentication);

        driverSourceComboBox.setSelectedValue(connectionConfig.getDriverSource());
        driverLibraryTextField.setText(connectionConfig.getDriverLibrary());
        driverSettingsForm.updateDriverFields();
        driverComboBox.setSelectedValue(DriverOption.get(driverComboBox.getValues(), connectionConfig.getDriver()));
    }

    public DatabaseType getSelectedDatabaseType() {
        return databaseTypeComboBox.getSelectedValue();
    }
}

