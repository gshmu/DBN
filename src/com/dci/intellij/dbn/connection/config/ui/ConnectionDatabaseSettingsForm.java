package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectivityStatus;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionBundleSettings;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.ConnectionSettings;
import com.dci.intellij.dbn.driver.DatabaseDriverManager;
import com.dci.intellij.dbn.driver.DriverSource;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

public abstract class ConnectionDatabaseSettingsForm<T extends ConnectionDatabaseSettings> extends ConfigurationEditorForm<T> {
    protected T temporaryConfig;

    public ConnectionDatabaseSettingsForm(T configuration) {
        super(configuration);
        temporaryConfig = (T) configuration.clone();
    }

    protected abstract JTextField getNameTextField();
    protected abstract TextFieldWithBrowseButton getDriverLibraryTextField();
    protected abstract JCheckBox getActiveCheckBox();
    protected abstract DBNComboBox<DriverSource> getDriverSourceComboBox();
    protected abstract DBNComboBox<DriverOption> getDriverComboBox();
    protected abstract DBNComboBox<DatabaseType> getDatabaseTypeComboBox();
    protected abstract JLabel getDriverErrorLabel();
    protected abstract JCheckBox getOsAuthenticationCheckBox();
    protected abstract JCheckBox getEmptyPasswordCheckBox();
    protected abstract JTextField getUserTextField();
    protected abstract JPasswordField getPasswordField();

    public void notifyPresentationChanges() {
        T configuration = temporaryConfig;//getConfiguration();
        String name = getNameTextField().getText();
        ConnectivityStatus connectivityStatus = configuration.getConnectivityStatus();
        Icon icon = configuration.getParent().isNew() ? Icons.CONNECTION_NEW :
                !getActiveCheckBox().isSelected() ? Icons.CONNECTION_DISABLED :
               connectivityStatus == ConnectivityStatus.VALID ? Icons.CONNECTION_ACTIVE :
               connectivityStatus == ConnectivityStatus.INVALID ? Icons.CONNECTION_INVALID : Icons.CONNECTION_INACTIVE;

        ConnectionPresentationChangeListener listener = EventUtil.notify(configuration.getProject(), ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = configuration.getParent().getDetailSettings().getEnvironmentType();
        listener.presentationChanged(name, icon, environmentType.getColor(), getConfiguration().getConnectionId(), configuration.getDatabaseType());
    }

    public boolean isConnectionActive() {
        return getActiveCheckBox().isSelected();
    }

    public T getTemporaryConfig() {
        return temporaryConfig;
    }

    public void setTemporaryConfig(T temporaryConfig) {
        this.temporaryConfig = temporaryConfig;
    }

    public ConnectivityStatus getConnectivityStatus() {
        return temporaryConfig.getConnectivityStatus();
    }

    protected abstract T createConfig(ConnectionSettings configuration);

    protected DocumentListener createDocumentListener() {
        return new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                T configuration = getConfiguration();
                configuration.setModified(true);

                Document document = e.getDocument();

                if (document == getDriverLibraryTextField().getTextField().getDocument()) {
                    updateDriverFields();
                }

                if (document == getNameTextField().getDocument()) {
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
        JCheckBox osAuthenticationCheckBox = getOsAuthenticationCheckBox();
        JCheckBox emptyPasswordCheckBox = getEmptyPasswordCheckBox();
        JPasswordField passwordField = getPasswordField();
        JTextField userTextField = getUserTextField();

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

                JCheckBox osAuthenticationCheckBox = getOsAuthenticationCheckBox();
                JCheckBox emptyPasswordCheckBox = getEmptyPasswordCheckBox();

                if (source == osAuthenticationCheckBox || source == emptyPasswordCheckBox) {
                    JTextField userTextField = getUserTextField();
                    JPasswordField passwordField = getPasswordField();

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

                if (source == getActiveCheckBox() || source == getNameTextField()) {
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
        DBNComboBox<DriverSource> driverSourceComboBox = getDriverSourceComboBox();
        DriverSource driverSource = driverSourceComboBox == null ? DriverSource.EXTERNAL : driverSourceComboBox.getSelectedValue();

        String error = null;
        if (driverSource == DriverSource.EXTERNAL) {
            JTextField textField = getDriverLibraryTextField().getTextField();
            String driverLibrary = textField.getText();

            boolean fileExists = StringUtil.isNotEmpty(driverLibrary) && fileExists(driverLibrary);
            DBNComboBox<DriverOption> driverComboBox = getDriverComboBox();
            if (fileExists) {
                textField.setForeground(UIUtil.getTextFieldForeground());
                DatabaseType databaseType = DatabaseType.resolve(driverLibrary);
                if (databaseType != DatabaseType.UNKNOWN && databaseType != getDatabaseTypeComboBox().getSelectedValue()) {
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


        JLabel driverErrorLabel = getDriverErrorLabel();
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
        return getNameTextField().getText();
    }
}
