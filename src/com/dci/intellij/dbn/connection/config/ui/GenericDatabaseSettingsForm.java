package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Driver;
import java.util.List;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionBundle;
import com.dci.intellij.dbn.connection.ConnectionManager;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettingsListener;
import com.dci.intellij.dbn.connection.config.GenericConnectionDatabaseSettings;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;

public class GenericDatabaseSettingsForm extends ConfigurationEditorForm<GenericConnectionDatabaseSettings>{
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
    private JPanel connectionParametersPanel;
    private JCheckBox autoConnectCheckBox;
    private JLabel autoConnectHintLabel;
    private JTextArea autoConnectTextArea;

    private GenericConnectionDatabaseSettings temporaryConfig;

    private static final FileChooserDescriptor LIBRARY_FILE_DESCRIPTOR = new FileChooserDescriptor(false, false, true, true, false, false);

    public GenericDatabaseSettingsForm(GenericConnectionDatabaseSettings connectionConfig) {
        super(connectionConfig);
        Project project = connectionConfig.getProject();
        temporaryConfig = connectionConfig.clone();
        updateBorderTitleForeground(connectionParametersPanel);
        resetChanges();
        updateLibraryTextField();

        registerComponent(mainPanel);

        driverLibraryTextField.addBrowseFolderListener(
                "Select driver library",
                "Library must contain classes implementing the 'java.sql.Driver' class.",
                project, LIBRARY_FILE_DESCRIPTOR);

        userTextField.setEnabled(!osAuthenticationCheckBox.isSelected());
        passwordField.setEnabled(!osAuthenticationCheckBox.isSelected());
        autoConnectHintLabel.setText("");
        autoConnectHintLabel.setIcon(Icons.COMMON_INFO);
        autoConnectTextArea.setBackground(UIUtil.getPanelBackground());
        autoConnectTextArea.setText("NOTE: If \"Connect automatically\" is not selected, the system will not restore the entire workspace the next time you open the project (i.e. all open editors for this connection will not be reopened automatically).");
        autoConnectTextArea.setFont(UIUtil.getLabelFont());
        autoConnectTextArea.setForeground(Colors.HINT_COLOR);

        boolean visibleHint = !autoConnectCheckBox.isSelected();
        autoConnectHintLabel.setVisible(visibleHint);
        autoConnectTextArea.setVisible(visibleHint);
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
                    JList connectionList = connectionBundle.getSettingsEditor().getList();
                    connectionList.revalidate();
                    connectionList.repaint();
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
        listener.presentationChanged(name, icon, null, getConfiguration().getId(), configuration.getDatabaseType());

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
                    temporaryConfig = new GenericConnectionDatabaseSettings(connectionBundle, getConfiguration().getParent());
                    applyChanges(temporaryConfig);
                    ConnectionManager connectionManager = ConnectionManager.getInstance(connectionBundle.getProject());

                    if (source == testButton) connectionManager.testConfigConnection(temporaryConfig, true);
                    if (source == infoButton) connectionManager.showConnectionInfo(temporaryConfig, null);
                }
                else if (source == osAuthenticationCheckBox) {
                    userTextField.setEnabled(!osAuthenticationCheckBox.isSelected());
                    passwordField.setEnabled(!osAuthenticationCheckBox.isSelected());
                    getConfiguration().setModified(true);
                } else if (source == autoConnectCheckBox){
                    boolean visibleHint = !autoConnectCheckBox.isSelected();
                    autoConnectHintLabel.setVisible(visibleHint);
                    autoConnectTextArea.setVisible(visibleHint);
                } else {
                    getConfiguration().setModified(true);
                }

                if (source == activeCheckBox || source == nameTextField || source == testButton || source == infoButton) {
                    JList connectionList = connectionBundle.getSettingsEditor().getList();
                    connectionList.revalidate();
                    connectionList.repaint();
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
        connectionConfig.setConnectAutomatically(autoConnectCheckBox.isSelected());
        connectionConfig.setConnectivityStatus(temporaryConfig.getConnectivityStatus());
        connectionConfig.updateHashCode();
    }

    public void applyChanges() {
        GenericConnectionDatabaseSettings connectionConfig = getConfiguration();
        boolean settingsChanged =
                !CommonUtil.safeEqual(connectionConfig.getDriverLibrary(), driverLibraryTextField.getText()) ||
                        !CommonUtil.safeEqual(connectionConfig.getDatabaseUrl(), urlTextField.getText()) ||
                        !CommonUtil.safeEqual(connectionConfig.getUser(), userTextField.getText());

        applyChanges(connectionConfig);

        if (settingsChanged) {
            EventManager.notify(connectionConfig.getProject(), ConnectionSettingsListener.TOPIC).settingsChanged(connectionConfig.getId());
        }
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
        autoConnectCheckBox.setSelected(connectionConfig.isConnectAutomatically());
        populateDriverList(connectionConfig.getDriverLibrary());
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

