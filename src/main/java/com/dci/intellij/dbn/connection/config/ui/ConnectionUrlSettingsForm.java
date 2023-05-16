package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.database.DatabaseInfo;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.DatabaseUrlPattern;
import com.dci.intellij.dbn.connection.DatabaseUrlType;
import com.dci.intellij.dbn.connection.config.ConnectionConfigType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.dci.intellij.dbn.connection.config.file.ui.DatabaseFileSettingsForm;
import com.dci.intellij.dbn.connection.config.tns.TnsName;
import com.dci.intellij.dbn.connection.config.tns.TnsNamesParser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.fields.ExpandableTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.*;

import static com.dci.intellij.dbn.common.ui.util.ComboBoxes.*;
import static com.dci.intellij.dbn.connection.DatabaseUrlType.CUSTOM;

public class ConnectionUrlSettingsForm extends DBNFormBase {
    private JLabel urlTypeLabel;
    private JLabel hostLabelField;
    private JLabel portLabelField;
    private JLabel databaseLabel;
    private JLabel tnsFolderLabel;
    private JLabel tnsProfileLabel;
    private JLabel databaseFilesLabel;
    private JLabel urlLabel;
    private JPanel databaseFilesPanel;
    private ComboBox<DatabaseUrlType> urlTypeComboBox;
    private ComboBox<String> tnsProfileNameComboBox;
    private JTextField hostTextField;
    private JTextField portTextField;
    private JTextField databaseTextField;
    private TextFieldWithBrowseButton tnsFolderTextField;
    private ExpandableTextField urlTextField;
    private JPanel mainPanel;

    private final DatabaseFileSettingsForm databaseFileSettingsForm;
    private final Map<DatabaseType, String> urlHistory = new HashMap<>();

    public ConnectionUrlSettingsForm(ConnectionDatabaseSettingsForm parent, ConnectionDatabaseSettings configuration) {
        super(parent);

        databaseFileSettingsForm = new DatabaseFileSettingsForm(this, configuration.getDatabaseInfo().getFiles());
        databaseFilesPanel.add(databaseFileSettingsForm.getComponent(), BorderLayout.CENTER);

        DatabaseUrlType[] urlTypes = configuration.getDatabaseType().getUrlTypes();
        initComboBox(urlTypeComboBox, urlTypes);
        setSelection(urlTypeComboBox, urlTypes[0]);
        urlTypeLabel.setVisible(urlTypes.length > 1);
        urlTypeComboBox.setVisible(urlTypes.length > 1);
        urlTypeComboBox.addActionListener(e -> handleUrlTypeChange());

        tnsProfileNameComboBox.addActionListener(e -> handleProfileSelection(e));
        tnsFolderTextField.addBrowseFolderListener(
                "Select Wallet Directory",
                "Folder must contain tnsnames.ora",
                null, new FileChooserDescriptor(false, true, true, true, false, false));
        tnsFolderTextField.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                handleTnsFolderChanged(tnsFolderTextField.getText());
            }
        });
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    public String getHost() {
        return hostTextField.getText();
    }

    public String getPort() {
        return portTextField.getText();
    }

    public String getDatabase() {
        return databaseTextField.getText();
    }

    public String getTnsFolder() {
        return tnsFolderTextField.getText();
    }

    public String getTnsProfile() {
        return (String) tnsProfileNameComboBox.getSelectedItem();
    }

    public String getUrl() {
        return urlTextField.getText();
    }

    public DatabaseFiles getDatabaseFiles() {
        return databaseFileSettingsForm.getDatabaseFiles();
    }


    private void handleProfileSelection(ActionEvent event) {
        String folderName = tnsFolderTextField.getText();
        String profile = (String) tnsProfileNameComboBox.getSelectedItem();
        if (!Strings.isEmpty(Strings.trim(profile)) && !Strings.isEmpty(Strings.trim(folderName))) {
            String url = String.format("jdbc:oracle:thin:@%s?TNS_ADMIN=%s",
                    profile, new File(folderName).getAbsolutePath());
            urlTextField.setText(url);
        }
    }

    private void handleTnsFolderChanged(@NotNull String text) {
        tnsProfileNameComboBox.removeAllItems();
        File tnsFolder = new File(text);
        if (tnsFolder.isDirectory()) {
            File tnsFile = new File(tnsFolder, "tnsnames.ora");
            if (tnsFile.exists()) {
                Set<String> tnsEntries = getTnsEntries(tnsFile);
                if (!tnsEntries.isEmpty()) {
                    tnsEntries.forEach(e -> tnsProfileNameComboBox.addItem(e));
                }
            }
        }
    }

    private Set<String> getTnsEntries(File tnsnamesOraFile) {
        final Set<String> tnsEntries = new LinkedHashSet<>();
        try {
            List<TnsName> tnsNames = TnsNamesParser.parse(tnsnamesOraFile);
            for (TnsName tnsName : tnsNames) {
                tnsEntries.add(tnsName.getName());
            }
        } catch (Exception e) {
            //ErrorHandler.logErrorStack("Error occured while reading tnsnames.ora file for database: " + adbInstance.getDbName(), e);
        }
        return tnsEntries;
    }

    public void updateFieldVisibility(ConnectionConfigType configType, DatabaseType databaseType) {
        if (configType == ConnectionConfigType.BASIC) {
            //urlLabel.setVisible(false);
            urlTextField.setEnabled(false);
            DatabaseUrlType urlType = databaseType.getDefaultUrlPattern().getUrlType();
            boolean isFileUrlType = urlType == DatabaseUrlType.FILE;
            databaseFilesLabel.setVisible(isFileUrlType);
            databaseFilesPanel.setVisible(isFileUrlType);
        } else if (configType == ConnectionConfigType.CUSTOM) {
            //urlLabel.setVisible(true);
            urlTextField.setEnabled(true);
            databaseFilesLabel.setVisible(false);
            databaseFilesPanel.setVisible(false);
        }
    }

    public DatabaseUrlType getUrlType() {
        return getSelection(urlTypeComboBox);
    }

    private void handleUrlTypeChange() {
        DatabaseUrlType urlType = getUrlType();
        boolean tnsVisible = urlType == DatabaseUrlType.TNS;
        boolean hpdVisible = urlType.isOneOf(
                DatabaseUrlType.SID,
                DatabaseUrlType.SERVICE,
                DatabaseUrlType.DATABASE);

        urlTextField.setEnabled(urlType == CUSTOM);

        // tns folder
        tnsFolderTextField.setVisible(tnsVisible);
        tnsFolderLabel.setVisible(tnsVisible);
        tnsProfileNameComboBox.setVisible(tnsVisible);
        tnsProfileLabel.setVisible(tnsVisible);

        // classic service name or sid
        databaseLabel.setVisible(hpdVisible);
        databaseTextField.setVisible(hpdVisible);
        hostLabelField.setVisible(hpdVisible);
        hostTextField.setVisible(hpdVisible);
        portLabelField.setVisible(hpdVisible);
        portTextField.setVisible(hpdVisible);
    }

    void handleDatabaseTypeChange(ConnectionConfigType configType, DatabaseType oldDatabaseType, DatabaseType newDatabaseType) {
        DatabaseUrlPattern oldUrlPattern = oldDatabaseType.getDefaultUrlPattern();
        DatabaseUrlPattern newUrlPattern = newDatabaseType.getDefaultUrlPattern();
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

                urlTypeLabel.setVisible(urlTypes.length > 1);
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
    }

    void resetFormChanges() {
        ConnectionDatabaseSettingsForm parent = ensureParentComponent();
        ConnectionDatabaseSettings configuration = parent.getConfiguration();
        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        DatabaseType databaseType = configuration.getDatabaseType();

        urlTextField.setText(databaseInfo.getUrl());
        databaseFileSettingsForm.setDatabaseFiles(databaseInfo.getFiles());
        hostTextField.setText(databaseInfo.getHost());
        portTextField.setText(databaseInfo.getPort());
        databaseTextField.setText(databaseInfo.getDatabase());

        DatabaseUrlType[] urlTypes = databaseType.getUrlTypes();
        initComboBox(urlTypeComboBox, urlTypes);
        setSelection(urlTypeComboBox, databaseInfo.getUrlType());
        urlTypeLabel.setVisible(urlTypes.length > 1);
        urlTypeComboBox.setVisible(urlTypes.length > 1);
    }


    boolean settingsChanged() {
        ConnectionDatabaseSettingsForm parent = ensureParentComponent();
        ConnectionDatabaseSettings configuration = parent.getConfiguration();

        DatabaseInfo databaseInfo = configuration.getDatabaseInfo();
        DatabaseUrlType urlType = getUrlType();
        return
            !Commons.match(databaseInfo.getHost(), getHost()) ||
            !Commons.match(databaseInfo.getPort(), getPort()) ||
            !Commons.match(databaseInfo.getDatabase(), getDatabase()) ||
            !Commons.match(databaseInfo.getTnsFolder(), getTnsFolder()) ||
            !Commons.match(databaseInfo.getTnsProfile(), getTnsProfile()) ||
            !Commons.match(databaseInfo.getUrl(), getUrl()) ||
            !Commons.match(databaseInfo.getUrlType(), urlType) ||
            !Commons.match(databaseInfo.getFiles(), urlType == DatabaseUrlType.FILE ? getDatabaseFiles() : null);

    }


}
