package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.GenericConnectionDatabaseSettings;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Driver;
import java.util.List;

public class GenericDatabaseSettingsForm extends ConfigurationEditorForm<GenericConnectionDatabaseSettings> implements ConnectionPresentationChangeListener{
    private JButton testButton;
    private JButton infoButton;
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private JTextField userTextField;
    private JTextField urlTextField;
    private TextFieldWithBrowseButton driverLibraryTextField;
    private JComboBox driverComboBox;
    private JPasswordField passwordField;
    private JCheckBox osAuthenticationCheckBox;
    private JCheckBox activeCheckBox;
    private JPanel headerPanel;
    private JPanel connectionParametersPanel;
    private DBNHeaderForm headerForm;

    private GenericConnectionDatabaseSettings temporaryConfig;
    private String connectionId;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public GenericDatabaseSettingsForm(GenericConnectionDatabaseSettings connectionConfig) {
        super(connectionConfig);
        Project project = connectionConfig.getProject();
        temporaryConfig = connectionConfig.clone();
        updateBorderTitleForeground(connectionParametersPanel);
        resetChanges();
        updateLibraryTextField();

        registerComponent(activeCheckBox);
        registerComponent(nameTextField);
        registerComponent(descriptionTextField);
        registerComponent(driverLibraryTextField.getTextField());
        registerComponent(urlTextField);
        registerComponent(userTextField);
        registerComponent(passwordField);
        registerComponent(driverComboBox);
        registerComponent(testButton);
        registerComponent(infoButton);
        registerComponent(osAuthenticationCheckBox);

        driverLibraryTextField.addBrowseFolderListener(
                "Select driver library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                project, LIBRARY_FILE_DESCRIPTOR);

        userTextField.setEnabled(!osAuthenticationCheckBox.isSelected());
        passwordField.setEnabled(!osAuthenticationCheckBox.isSelected());
        EventManager.subscribe(project, ConnectionPresentationChangeListener.TOPIC, this);
        headerForm = new DBNHeaderForm();
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    public void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                GenericConnectionDatabaseSettings connectionConfig = getConfiguration();
                connectionConfig.setModified(true);

                Document document = e.getDocument();

                if (document == driverLibraryTextField.getTextField().getDocument()) {
                    updateLibraryTextField();
                }

                if (document == nameTextField.getDocument()) {
                    ConnectionBundle connectionBundle = connectionConfig.getConnectionBundle();
                    connectionBundle.getSettingsEditor().getList().repaint();
                    notifyPresentationChanges();
                }
            }
        };
    }

    public void notifyPresentationChanges() {
        GenericConnectionDatabaseSettings configuration = temporaryConfig;//getConfiguration();
        String name = nameTextField.getText();
        ConnectivityStatus connectivityStatus = configuration.getConnectivityStatus();
        Icon icon = configuration.isNew() ? Icons.CONNECTION_NEW :
               !activeCheckBox.isSelected() ? Icons.CONNECTION_DISABLED :
               connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
               connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        ConnectionPresentationChangeListener listener = EventManager.notify(configuration.getProject(), ConnectionPresentationChangeListener.TOPIC);
        listener.presentationChanged(name, icon, headerForm.getBackground(), connectionId);

    }

    @Override
    public void presentationChanged(String name, Icon icon, Color color, String connectionId) {
        if (this.connectionId.equals(connectionId)) {
            if (name != null) headerForm.setTitle(name);
            if (icon != null) headerForm.setIcon(icon);
            headerForm.setBackground(color == null ? UIUtil.getPanelBackground() :color);
        }
    }

    private void updateLibraryTextField() {
        JTextField textField = driverLibraryTextField.getTextField();
        if (fileExists(textField.getText())) {
            populateDriverList(textField.getText());
            textField.setForeground(UIUtil.getTextFieldForeground());
        } else {
            textField.setForeground(Color.RED);
        }
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                ConnectionDatabaseSettings databaseSettings = getConfiguration();
                ConnectionBundle connectionBundle = databaseSettings.getConnectionBundle();

                if (source == testButton || source == infoButton) {
                    temporaryConfig = new GenericConnectionDatabaseSettings(connectionBundle);
                    applyChanges(temporaryConfig);
                    ConnectionManager connectionManager = ConnectionManager.getInstance(connectionBundle.getProject());

                    if (source == testButton) connectionManager.testConfigConnection(temporaryConfig, true);
                    if (source == infoButton) connectionManager.showConnectionInfo(temporaryConfig, null);
                }
                else if (source == osAuthenticationCheckBox) {
                    userTextField.setEnabled(!osAuthenticationCheckBox.isSelected());
                    passwordField.setEnabled(!osAuthenticationCheckBox.isSelected());
                    getConfiguration().setModified(true);
                } else {
                    getConfiguration().setModified(true);
                }

                if (source == activeCheckBox || source == nameTextField || source == testButton || source == infoButton) {
                    connectionBundle.getSettingsEditor().getList().repaint();
                    notifyPresentationChanges();
                }
            }
        };
    }



    private void populateDriverList(final String driverLibrary) {
        boolean fileExists = fileExists(driverLibrary);
        if (fileExists) {
            List<Driver> drivers = DatabaseDriverManager.getInstance().loadDrivers(driverLibrary);
            Object selected = driverComboBox.getSelectedItem();
            driverComboBox.removeAllItems();
            //driverComboBox.addItem("");
            if (drivers != null) {
                for (Driver driver : drivers) {
                    driverComboBox.addItem(driver.getClass().getName());
                }
                if (selected == null && drivers.size() > 0) {
                    selected = drivers.get(0).getClass().getName();
                }
            }
            driverComboBox.setSelectedItem(selected);
        } else {
            driverComboBox.removeAllItems();
            //driverComboBox.addItem("");
        }
    }

    private boolean fileExists(String driverLibrary) {
        return driverLibrary != null && new File(driverLibrary).exists();
    }

    public String getConnectionName() {
        return nameTextField.getText();
    }

    public boolean isConnectionActive() {
        return activeCheckBox.isSelected();
    }

    public ConnectivityStatus getConnectivityStatus() {
        return temporaryConfig.getConnectivityStatus();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyChanges(GenericConnectionDatabaseSettings connectionConfig) {
        connectionConfig.setActive(activeCheckBox.isSelected());
        connectionConfig.setName(nameTextField.getText());
        connectionConfig.setDescription(descriptionTextField.getText());
        connectionConfig.setDriverLibrary(driverLibraryTextField.getText());
        connectionConfig.setDriver(driverComboBox.getSelectedItem() == null ? null : driverComboBox.getSelectedItem().toString());
        connectionConfig.setDatabaseUrl(urlTextField.getText());
        connectionConfig.setUser(userTextField.getText());
        connectionConfig.setPassword(new String(passwordField.getPassword()));
        connectionConfig.setOsAuthentication(osAuthenticationCheckBox.isSelected());
        connectionConfig.setConnectivityStatus(temporaryConfig.getConnectivityStatus());
        connectionConfig.updateHashCode();
    }

    public void applyChanges() {
        applyChanges(getConfiguration());
    }


    public void resetChanges() {
        GenericConnectionDatabaseSettings connectionConfig = getConfiguration();
        activeCheckBox.setSelected(connectionConfig.isActive());
        nameTextField.setText(connectionConfig.getDisplayName());
        descriptionTextField.setText(connectionConfig.getDescription());
        driverLibraryTextField.setText(connectionConfig.getDriverLibrary());
        driverComboBox.setSelectedItem(connectionConfig.getDriver());
        urlTextField.setText(connectionConfig.getDatabaseUrl());
        userTextField.setText(connectionConfig.getUser());
        passwordField.setText(connectionConfig.getPassword());
        osAuthenticationCheckBox.setSelected(connectionConfig.isOsAuthentication());
        populateDriverList(connectionConfig.getDriverLibrary());
    }

    @Override
    public void dispose() {
        EventManager.unsubscribe(this);
        super.dispose();
    }
}

