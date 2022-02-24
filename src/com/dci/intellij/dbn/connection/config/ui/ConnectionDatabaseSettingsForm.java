package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.database.AuthenticationInfo;
import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.options.ConfigurationHandle;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.form.DBNHintForm;
import com.dci.intellij.dbn.common.ui.util.ComboBoxes;
import com.dci.intellij.dbn.common.ui.util.UserInterface;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.*;
import com.dci.intellij.dbn.connection.config.*;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.file.ui.DatabaseFileSettingsForm;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.dci.intellij.dbn.common.ui.util.ComboBoxes.*;

public class ConnectionDatabaseSettingsForm extends ConfigurationEditorForm<ConnectionDatabaseSettings> {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField hostTextField;
    private JComboBox<DatabaseType> databaseTypeComboBox;
    private JComboBox<DatabaseUrlType> urlTypeComboBox;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private JPanel driverLibraryPanel;
    private JLabel databaseTypeLabel;
    private JPanel authenticationPanel;
    private JTextField urlTextField;
    private JPanel databaseInfoPanel;
    private JPanel urlPanel;
    private JPanel filePanel;
    private JPanel databaseFilesPanel;
    private JPanel databaseTypeHintPanel;

    private final DatabaseFileSettingsForm databaseFileSettingsForm;
    private final ConnectionDriverSettingsForm driverSettingsForm;
    private final ConnectionAuthenticationSettingsForm authenticationSettingsForm;

    private DatabaseType selectedDatabaseType;
    private final Map<DatabaseType, String> urlHistory = new HashMap<>();

    public ConnectionDatabaseSettingsForm(ConnectionDatabaseSettings configuration) {
        super(configuration);

        ConnectionConfigType configType = configuration.getConfigType();
        updateFieldVisibility(configType, configuration.getDatabaseType());

        selectedDatabaseType = configuration.getDatabaseType();
        if (configType == ConnectionConfigType.CUSTOM) {
            initComboBox(databaseTypeComboBox,
                    DatabaseType.ORACLE,
                    DatabaseType.MYSQL,
                    DatabaseType.POSTGRES,
                    DatabaseType.SQLITE,
                    DatabaseType.GENERIC);
        } else {
            databaseTypeLabel.setText(selectedDatabaseType.getName());
            databaseTypeLabel.setIcon(selectedDatabaseType.getIcon());
            initComboBox(databaseTypeComboBox, selectedDatabaseType);
            setSelection(databaseTypeComboBox, selectedDatabaseType);
            databaseTypeComboBox.setEnabled(false);
            databaseTypeComboBox.setVisible(false);

            DatabaseUrlType[] urlTypes = selectedDatabaseType.getUrlTypes();
            initComboBox(urlTypeComboBox, urlTypes);
            setSelection(urlTypeComboBox, urlTypes[0]);
            urlTypeComboBox.setVisible(urlTypes.length > 1);
        }

        databaseFileSettingsForm = new DatabaseFileSettingsForm(this, configuration.getDatabaseInfo().getFiles());

        databaseFilesPanel.add(databaseFileSettingsForm.getComponent(), BorderLayout.CENTER);

        authenticationSettingsForm = new ConnectionAuthenticationSettingsForm(this);
        //DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> authenticationSettingsPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, authenticationSettingsForm.getComponent(), "Authentication", true);
        authenticationPanel.add(authenticationSettingsForm.getComponent(), BorderLayout.CENTER);

        driverSettingsForm = new ConnectionDriverSettingsForm(this);
        boolean externalLibrary = configuration.getDriverSource() == DriverSource.EXTERNAL;
        //DBNCollapsiblePanel<ConnectionDatabaseSettingsForm> driverPanel = new DBNCollapsiblePanel<ConnectionDatabaseSettingsForm>(this, driverSettingsForm.getComponent(), "Driver", externalLibrary);
        driverLibraryPanel.add(driverSettingsForm.getComponent(), BorderLayout.CENTER);

        resetFormChanges();
        registerComponent(mainPanel);

        if (configType == ConnectionConfigType.CUSTOM) {
            databaseTypeComboBox.addActionListener(e -> databaseTypeChanged());
            updateNativeSupportDatabaseHint();
        }
    }

    protected void databaseTypeChanged() {
        ConnectionDatabaseSettings configuration = getConfiguration();
        ConnectionConfigType configType = configuration.getConfigType();
        DatabaseType oldDatabaseType = selectedDatabaseType;
        DatabaseType newDatabaseType = getSelection(databaseTypeComboBox);

        DatabaseUrlPattern oldUrlPattern = oldDatabaseType.getDefaultUrlPattern();
        DatabaseUrlPattern newUrlPattern = newDatabaseType.getDefaultUrlPattern();
        updateFieldVisibility(configType, newDatabaseType);
        if (configType == ConnectionConfigType.BASIC) { // TODO this is not used any more
            if (newUrlPattern.getUrlType() == DatabaseUrlType.FILE) {
                String file = databaseFileSettingsForm.getMainFilePath();
                DatabaseInfo defaults = newUrlPattern.getDefaultInfo();
                DatabaseInfo oldDefaults = oldUrlPattern == null ? null : oldUrlPattern.getDefaultInfo();
                if (Strings.isEmpty(file) || (oldDefaults != null && Objects.equals(oldDefaults.getFiles().getMainFile().getPath(), file))) {
                    databaseFileSettingsForm.setMainFilePath(defaults.getFiles().getMainFile().getPath());
                }
            } else {
                String host = hostTextField.getText();
                String port = portTextField.getText();
                String database = databaseTextField.getText();

                DatabaseInfo defaults = newUrlPattern.getDefaultInfo();
                DatabaseInfo oldDefaults = oldUrlPattern == null ? null : oldUrlPattern.getDefaultInfo();
                if (Strings.isEmpty(host) || (oldDefaults != null && Objects.equals(oldDefaults.getHost(), host))) {
                    hostTextField.setText(defaults.getHost());
                }

                if (Strings.isEmpty(port) || (oldDefaults != null && Objects.equals(oldDefaults.getPort(), port))) {
                    portTextField.setText(defaults.getPort());
                }
                if (Strings.isEmpty(database) || (oldDefaults != null && Objects.equals(oldDefaults.getDatabase(), database))) {
                    databaseTextField.setText(defaults.getDatabase());
                }
                DatabaseUrlType[] urlTypes = newDatabaseType.getUrlTypes();
                initComboBox(urlTypeComboBox, urlTypes);
                setSelection(urlTypeComboBox, urlTypes[0]);
                urlTypeComboBox.setVisible(urlTypes.length > 1);
            }
        } else {
            String oldUrl = urlTextField.getText();
            urlHistory.put(oldDatabaseType, oldUrl);

            String historyUrl = urlHistory.get(newDatabaseType);
            if (Strings.isNotEmpty(historyUrl)) {
                urlTextField.setText(historyUrl);
            } else if (Strings.isEmpty(oldUrl) || newDatabaseType != DatabaseType.GENERIC){
                urlTextField.setText(newUrlPattern.getDefaultUrl());
            }
        }

        driverSettingsForm.updateDriverFields();
        selectedDatabaseType = newDatabaseType;
        updateNativeSupportDatabaseHint();
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
        AuthenticationType[] authTypes = databaseType.getAuthTypes();
        authenticationPanel.setVisible(authTypes.length > 1 || authTypes[0] != AuthenticationType.NONE);
    }

    void notifyPresentationChanges() {
        ConnectionDatabaseSettings configuration = getConfiguration();
        String name = nameTextField.getText();
        ConnectivityStatus connectivityStatus = configuration.getConnectivityStatus();
        ConnectionSettings connectionSettings = configuration.getParent();
        ConnectionSettingsForm connectionSettingsForm = connectionSettings.getSettingsEditor();

        Icon icon = connectionSettings.isNew() ? Icons.CONNECTION_NEW :
                connectionSettingsForm != null && !connectionSettingsForm.isConnectionActive() ? Icons.CONNECTION_DISABLED :
                        connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_CONNECTED :
                        connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        EnvironmentType environmentType = connectionSettings.getDetailSettings().getEnvironmentType();
        JBColor color = environmentType.getColor();
        ConnectionId connectionId = configuration.getConnectionId();
        DatabaseType databaseType = configuration.getDatabaseType();

        ProjectEvents.notify(
                configuration.getProject(),
                ConnectionPresentationChangeListener.TOPIC,
                (listener) -> listener.presentationChanged(name, icon, color, connectionId, databaseType));
    }

    //protected abstract ConnectionDatabaseSettings createConfig(ConnectionSettings configuration);

    @Override
    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
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
                        UserInterface.repaint(connectionList);
                        notifyPresentationChanges();
                    }
                }
            }
        };
    }


    @Override
    protected ActionListener createActionListener() {
        return e -> {
            Object source = e.getSource();
            ConnectionDatabaseSettings configuration = getConfiguration();
            configuration.setModified(true);
            if (source == nameTextField) {
                ConnectionBundleSettings connectionBundleSettings = configuration.getParent().getParent();
                ConnectionBundleSettingsForm settingsEditor = connectionBundleSettings.getSettingsEditor();

                if (settingsEditor != null) {
                    JList connectionList = settingsEditor.getList();
                    UserInterface.repaint(connectionList);
                    notifyPresentationChanges();
                }
            }
        };
    }

    public String getConnectionName() {
        return nameTextField.getText();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges(final ConnectionDatabaseSettings configuration) throws ConfigurationException {
        JComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        JComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        DatabaseType databaseType = getSelectedDatabaseType();
        DriverOption driverOption = ComboBoxes.getSelection(driverComboBox);
        DatabaseUrlType urlType = Commons.nvl(getSelection(urlTypeComboBox), DatabaseUrlType.DATABASE);

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

        if (urlType == DatabaseUrlType.FILE) {
            DatabaseFiles databaseFiles = databaseFileSettingsForm.getDatabaseFiles();
            databaseFiles.validate();
            databaseInfo.setFiles(databaseFiles);
        } else {
            databaseInfo.setFiles(null);
        }


        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();
        String oldUserName = authenticationInfo.getUser();
        String oldPassword = authenticationInfo.getPassword();
        authenticationSettingsForm.applyFormChanges(authenticationInfo);
        if (!ConfigurationHandle.isTransitory()) {
            authenticationInfo.updateKeyChain(oldUserName, oldPassword);
        }

        configuration.setDriverSource(getSelection(driverSourceComboBox));
        configuration.updateSignature();
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        ConfigurationEditorUtil.validateStringValue(nameTextField, "Name", true);
        ConnectionDatabaseSettings configuration = getConfiguration();

        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();

        DatabaseType selectedDatabaseType = getSelectedDatabaseType();
        DatabaseType driverDatabaseType = getDriverDatabaseType();
        if (driverDatabaseType != null && driverDatabaseType != selectedDatabaseType) {
            if (selectedDatabaseType == DatabaseType.GENERIC) {
                // TODO hint there is dedicated support for the database type resolved from driver
            } else {
                throw new ConfigurationException("The provided driver library is not a valid " + selectedDatabaseType.getName() + " driver library.");
            }
        }

        boolean nameChanged = !Objects.equals(nameTextField.getText(), configuration.getName());

        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        DatabaseUrlType urlType = getSelection(urlTypeComboBox);
        boolean settingsChanged =
                //!connectionConfig.getProperties().equals(propertiesEditorForm.getProperties()) ||
                !Commons.match(configuration.getDatabaseType(), selectedDatabaseType) ||
                !Commons.match(configuration.getDriverLibrary(), driverLibraryTextField.getText()) ||
                !Commons.match(databaseInfo.getHost(), hostTextField.getText()) ||
                !Commons.match(databaseInfo.getPort(), portTextField.getText()) ||
                !Commons.match(databaseInfo.getDatabase(), databaseTextField.getText()) ||
                !Commons.match(databaseInfo.getUrlType(), urlType) ||
                !Commons.match(databaseInfo.getFiles(), urlType == DatabaseUrlType.FILE ? databaseFileSettingsForm.getDatabaseFiles() : null) ||
                !Commons.match(configuration.getAuthenticationInfo().getUser(), authenticationSettingsForm.getUserTextField().getText());


        applyFormChanges(configuration);

        Project project = configuration.getProject();
        SettingsChangeNotifier.register(() -> {
            ConnectionId connectionId = configuration.getConnectionId();
            if (nameChanged) {
                ProjectEvents.notify(project,
                        ConnectionConfigListener.TOPIC,
                        listener -> listener.connectionNameChanged(connectionId));
            }

            if (settingsChanged) {
                ProjectEvents.notify(project,
                        ConnectionConfigListener.TOPIC,
                        listener -> listener.connectionChanged(connectionId));
            }
        });
    }

    @NotNull
    DatabaseType getSelectedDatabaseType() {
        ConnectionDatabaseSettings configuration = getConfiguration();;
        return Commons.nvl(getSelection(databaseTypeComboBox), configuration.getDatabaseType());
    }

    @Nullable
    private DatabaseType getDriverDatabaseType() {
        JComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();
        DriverOption selectedDriver = ComboBoxes.getSelection(driverComboBox);
        return selectedDriver == null ? null : DatabaseType.resolve(selectedDriver.getName());
    }


    @Override
    public void resetFormChanges() {
        JComboBox<DriverSource> driverSourceComboBox = driverSettingsForm.getDriverSourceComboBox();
        TextFieldWithBrowseButton driverLibraryTextField = driverSettingsForm.getDriverLibraryTextField();
        JComboBox<DriverOption> driverComboBox = driverSettingsForm.getDriverComboBox();

        ConnectionDatabaseSettings configuration = getConfiguration();

        nameTextField.setText(configuration.getDisplayName());
        descriptionTextField.setText(configuration.getDescription());
        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        urlTextField.setText(databaseInfo.getUrl());
        databaseFileSettingsForm.setDatabaseFiles(databaseInfo.getFiles());
        hostTextField.setText(databaseInfo.getHost());
        portTextField.setText(databaseInfo.getPort());
        databaseTextField.setText(databaseInfo.getDatabase());

        DatabaseType databaseType = configuration.getDatabaseType();
        setSelection(databaseTypeComboBox, databaseType);


        DatabaseUrlType[] urlTypes = databaseType.getUrlTypes();
        initComboBox(urlTypeComboBox, urlTypes);
        setSelection(urlTypeComboBox, databaseInfo.getUrlType());
        urlTypeComboBox.setVisible(urlTypes.length > 1);

        AuthenticationInfo authenticationInfo = configuration.getAuthenticationInfo();
        authenticationSettingsForm.resetFormChanges(authenticationInfo);

        setSelection(driverSourceComboBox, configuration.getDriverSource());
        driverLibraryTextField.setText(configuration.getDriverLibrary());
        driverSettingsForm.updateDriverFields();
        setSelection(driverComboBox, DriverOption.get(getElements(driverComboBox), configuration.getDriver()));
    }

    private void updateNativeSupportDatabaseHint() {
        DatabaseType selectedDatabaseType = getSelectedDatabaseType();
        DatabaseType driverDatabaseType = getDriverDatabaseType();
        if (selectedDatabaseType == DatabaseType.GENERIC && driverDatabaseType != null && driverDatabaseType != selectedDatabaseType) {
            String databaseTypeName = driverDatabaseType.getName();
            DBNHintForm hintForm = new DBNHintForm(this,
                    "Your database type was identified as \"" + databaseTypeName +"\".\n" +
                    "Use specific connection type instead of \"Generic\", " +
                            "to enable dedicated support for this database", MessageType.WARNING, true);
            databaseTypeHintPanel.add(hintForm.getComponent(), BorderLayout.CENTER);
        } else {
            databaseTypeHintPanel.removeAll();
        }
    }
}

