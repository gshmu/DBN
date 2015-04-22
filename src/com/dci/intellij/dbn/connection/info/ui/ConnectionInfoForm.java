package com.dci.intellij.dbn.connection.info.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.sql.Connection;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.connection.config.ConnectionDatabaseSettings;
import com.dci.intellij.dbn.connection.config.GuidedDatabaseSettings;
import com.dci.intellij.dbn.connection.info.ConnectionInfo;

public class ConnectionInfoForm extends DBNFormImpl<ConnectionInfoDialog>{
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel setupPanel;
    private JPanel metaDataPanel;
    private JPanel detailsPanel;
    private JLabel infoProductNameLabel;
    private JLabel infoProductVersionLabel;
    private JLabel infoDriverNameLabel;
    private JLabel infoDriverVersionLabel;
    private JLabel infoConnectionUrlLabel;
    private JLabel infoUserNameLabel;
    private JLabel infoDatabaseTypeLabel;
    private JLabel infoDriverJdbcType;
    private JLabel setupNameValueLabel;
    private JLabel setupDescriptionValueLabel;
    private JLabel setupDriverLibraryValueLabel;
    private JLabel setupDriverValueLabel;
    private JLabel setupUrlValueLabel;
    private JLabel statusMessageLabel;
    private JLabel setupDatabaseValueLabel;
    private JLabel setupDescLabel;
    private JLabel setupHostValueLabel;
    private JLabel setupPortValueLabel;
    private JLabel setupUrlLabel;

    public ConnectionInfoForm(ConnectionInfoDialog parentComponent, ConnectionHandler connectionHandler) {
        super(parentComponent);
        initHeaderPanel(connectionHandler);
        initSetupPanel(connectionHandler);
        initInfoPanel(connectionHandler);
    }

    public ConnectionInfoForm(@NotNull ConnectionInfoDialog parentComponent, ConnectionInfo connectionInfo, String connectionName, EnvironmentType environmentType) {
        super(parentComponent);
        setupPanel.setVisible(false);
        initHeaderPanel(connectionName, environmentType);
        initInfoPanel(connectionInfo);
    }

    private void initHeaderPanel(ConnectionHandler connectionHandler) {
        DBNHeaderForm headerForm = new DBNHeaderForm();
        headerForm.setTitle(connectionHandler.getName());
        headerForm.setIcon(connectionHandler.getIcon());
        headerForm.setBackground(connectionHandler.getEnvironmentType().getColor());
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    private void initHeaderPanel(String connectionName, EnvironmentType environmentType) {
        DBNHeaderForm headerForm = new DBNHeaderForm();
        headerForm.setTitle(connectionName);
        headerForm.setIcon(Icons.CONNECTION_ACTIVE);
        headerForm.setBackground(environmentType.getColor());
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    private void initInfoPanel(ConnectionHandler connectionHandler) {
        try {
            Connection connection = connectionHandler.getStandaloneConnection();
            ConnectionInfo connectionInfo = new ConnectionInfo(connection.getMetaData());

            initInfoPanel(connectionInfo);
        } catch (SQLException e) {
            infoDatabaseTypeLabel.setText(DatabaseType.UNKNOWN.getName());
            infoDatabaseTypeLabel.setIcon(DatabaseType.UNKNOWN.getIcon());


            infoProductNameLabel.setText("-");
            infoProductVersionLabel.setText("-");
            infoDriverNameLabel.setText("-");
            infoDriverVersionLabel.setText("-");
            infoDriverJdbcType.setText("-");
            infoConnectionUrlLabel.setText("-");
            infoUserNameLabel.setText("-");
            statusMessageLabel.setText("Connection error: " + e.getMessage());
            statusMessageLabel.setIcon(Icons.EXEC_MESSAGES_ERROR);
        }

        updateBorderTitleForeground(detailsPanel);
    }

    private void initInfoPanel(ConnectionInfo connectionInfo) {
        DatabaseType databaseType = connectionInfo.getDatabaseType();
        infoDatabaseTypeLabel.setText(databaseType.getName());
        infoDatabaseTypeLabel.setIcon(databaseType.getIcon());

        infoProductNameLabel.setText(connectionInfo.getProductName());
        infoProductVersionLabel.setText(connectionInfo.getProductVersion());
        infoDriverNameLabel.setText(connectionInfo.getDriverName());
        infoDriverVersionLabel.setText(connectionInfo.getDriverVersion());
        infoDriverJdbcType.setText(connectionInfo.getDriverJdbcType());
        infoConnectionUrlLabel.setText(connectionInfo.getUrl());
        infoUserNameLabel.setText(connectionInfo.getUserName());

        statusMessageLabel.setText("Connection successful");
        statusMessageLabel.setIcon(Icons.EXEC_MESSAGES_INFO);
    }

    private void initSetupPanel(ConnectionHandler connectionHandler) {
        setupNameValueLabel.setText(connectionHandler.getName());

        String description = connectionHandler.getDescription();
        if (StringUtil.isEmpty(description)) {
            setupDescLabel.setVisible(false);
            setupDescriptionValueLabel.setVisible(false);
        } else {
            setupDescriptionValueLabel.setText(description);
        }
        ConnectionDatabaseSettings databaseSettings = connectionHandler.getSettings().getDatabaseSettings();
        setupDriverLibraryValueLabel.setText(getPresentableText(databaseSettings.getDriverLibrary()));
        setupDriverValueLabel.setText(getPresentableText(databaseSettings.getDriver()));

        setupHostValueLabel.setText(getPresentableText(databaseSettings.getHost()));
        setupPortValueLabel.setText(getPresentableText(databaseSettings.getPort()));
        setupDatabaseValueLabel.setText(getPresentableText(databaseSettings.getDatabase()));
        if (databaseSettings instanceof GuidedDatabaseSettings) {
            setupUrlLabel.setVisible(false);
            setupUrlValueLabel.setVisible(false);
        } else {
            setupUrlValueLabel.setText(getPresentableText(databaseSettings.getConnectionUrl()));
        }
        updateBorderTitleForeground(setupPanel);
    }

    @NotNull
    private String getPresentableText(String value) {
        return StringUtil.isEmpty(value) ? "-" : value;
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }
}
