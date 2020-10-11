package com.dci.intellij.dbn.connection.config.ui;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeId;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentConfigLocalListener;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.EventNotifier;
import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.ui.DBNHintForm;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandlerStatusListener;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.getSelection;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.initComboBox;
import static com.dci.intellij.dbn.common.ui.ComboBoxUtil.setSelection;
import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class ConnectionDetailSettingsForm extends ConfigurationEditorForm<ConnectionDetailSettings> {
    private JPanel mainPanel;
    private JComboBox<CharsetOption> encodingComboBox;
    private JComboBox<EnvironmentType> environmentTypesComboBox;
    private JPanel generalGroupPanel;
    private JTextField maxPoolSizeTextField;
    private JTextField idleTimeTextField;
    private JTextField alternativeStatementDelimiterTextField;
    private JPanel autoConnectHintPanel;
    private JTextField passwordExpiryTextField;
    private JCheckBox databaseLoggingCheckBox;
    private JCheckBox sessionManagementCheckBox;
    private JCheckBox ddlFileBindingCheckBox;
    private JCheckBox autoConnectCheckBox;
    private JCheckBox restoreWorkspaceCheckBox;
    private JCheckBox restoreWorkspaceDeepCheckBox;
    private JTextField idleTimePoolTextField;

    public ConnectionDetailSettingsForm(final ConnectionDetailSettings configuration) {
        super(configuration);

        updateBorderTitleForeground(generalGroupPanel);

        initComboBox(encodingComboBox, CharsetOption.ALL);

        List<EnvironmentType> environmentTypes = new ArrayList<>(getEnvironmentTypes());
        environmentTypes.add(0, EnvironmentType.DEFAULT);
        initComboBox(environmentTypesComboBox, environmentTypes);
        resetFormChanges();

        registerComponent(mainPanel);

        environmentTypesComboBox.addActionListener(e -> notifyPresentationChanges());

        String autoConnectHintText = "NOTE: If \"Connect automatically\" is not selected, the system will not restore the workspace the next time you open the project (i.e. all open editors for this connection will not be reopened automatically).";
        DBNHintForm hintForm = new DBNHintForm(autoConnectHintText, MessageType.INFO, false);
        autoConnectHintPanel.add(hintForm.getComponent());

        boolean visibleHint = !autoConnectCheckBox.isSelected() && restoreWorkspaceCheckBox.isSelected();
        autoConnectHintPanel.setVisible(visibleHint);

        subscribe(EnvironmentConfigLocalListener.TOPIC, presentationChangeListener);
    }

    private void notifyPresentationChanges() {
        Project project = getConfiguration().getProject();
        EnvironmentType environmentType = getSelection(environmentTypesComboBox);
        Color color = environmentType == null ? null : environmentType.getColor();

        EventNotifier.notify(project,
                ConnectionPresentationChangeListener.TOPIC,
                (listener) -> listener.presentationChanged(null, null, color, getConfiguration().getConnectionId(), null));
    }

    public EnvironmentType getSelectedEnvironmentType() {
        return getSelection(environmentTypesComboBox);
    }

    @Override
    protected ActionListener createActionListener() {
        return e -> {
            Object source = e.getSource();
            if (source == autoConnectCheckBox || source == restoreWorkspaceCheckBox){
                boolean visibleHint = !autoConnectCheckBox.isSelected() && restoreWorkspaceCheckBox.isSelected();
                autoConnectHintPanel.setVisible(visibleHint);
            }
            if (source == restoreWorkspaceCheckBox) {
                restoreWorkspaceDeepCheckBox.setEnabled(restoreWorkspaceCheckBox.isSelected());
                if (!restoreWorkspaceCheckBox.isSelected()) {
                    restoreWorkspaceDeepCheckBox.setSelected(false);
                }
            }
            getConfiguration().setModified(true);
        };
    }

    private List<EnvironmentType> getEnvironmentTypes() {
        Project project = getConfiguration().getProject();
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
        return environmentSettings.getEnvironmentTypes().getEnvironmentTypes();
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        final ConnectionDetailSettings configuration = getConfiguration();

        EnvironmentType newEnvironmentType = CommonUtil.nvl(getSelection(environmentTypesComboBox), EnvironmentType.DEFAULT);
        final EnvironmentTypeId newEnvironmentTypeId = newEnvironmentType.getId();

        Charset charset = configuration.getCharset();
        Charset newCharset = getSelection(encodingComboBox).getCharset();
        boolean settingsChanged = !charset.equals(newCharset);

        EnvironmentTypeId environmentTypeId = configuration.getEnvironmentType().getId();
        boolean environmentChanged = environmentTypeId != newEnvironmentTypeId;


        applyFormChanges(configuration);

        Project project = configuration.getProject();
        SettingsChangeNotifier.register(() -> {
            if (environmentChanged) {
                EventNotifier.notify(project,
                        EnvironmentManagerListener.TOPIC,
                        (listener) -> listener.configurationChanged(project));
            }

            if (settingsChanged) {
                EventNotifier.notify(project, ConnectionHandlerStatusListener.TOPIC,
                        (listener) -> listener.statusChanged(configuration.getConnectionId()));
            }
        });
    }

    @Override
    public void applyFormChanges(ConnectionDetailSettings configuration) throws ConfigurationException {
        CharsetOption charsetOption = getSelection(encodingComboBox);
        EnvironmentType environmentType = getSelection(environmentTypesComboBox);

        configuration.setEnvironmentTypeId(environmentType == null ? EnvironmentTypeId.DEFAULT : environmentType.getId());
        configuration.setCharset(charsetOption == null ? null : charsetOption.getCharset());
        configuration.setRestoreWorkspace(restoreWorkspaceCheckBox.isSelected());
        configuration.setRestoreWorkspaceDeep(restoreWorkspaceDeepCheckBox.isSelected());
        configuration.setConnectAutomatically(autoConnectCheckBox.isSelected());
        configuration.setEnableSessionManagement(sessionManagementCheckBox.isSelected());
        configuration.setEnableDdlFileBinding(ddlFileBindingCheckBox.isSelected());
        configuration.setEnableDatabaseLogging(databaseLoggingCheckBox.isSelected());
        configuration.setAlternativeStatementDelimiter(alternativeStatementDelimiterTextField.getText());
        int idleTimeToDisconnect = ConfigurationEditorUtil.validateIntegerInputValue(idleTimeTextField, "Idle time to disconnect (minutes)", true, 0, 60, "");
        int idleTimeToDisconnectPool = ConfigurationEditorUtil.validateIntegerInputValue(idleTimePoolTextField, "Idle time to disconnect pool (minutes)", true, 1, 60, "");
        int passwordExpiryTime = ConfigurationEditorUtil.validateIntegerInputValue(passwordExpiryTextField, "Idle time to request password (minutes)", true, 0, 60, "");
        int maxPoolSize = ConfigurationEditorUtil.validateIntegerInputValue(maxPoolSizeTextField, "Max connection pool size", true, 3, 20, "");
        configuration.setIdleTimeToDisconnect(idleTimeToDisconnect);
        configuration.setIdleTimeToDisconnectPool(idleTimeToDisconnectPool);
        configuration.setCredentialExpiryTime(passwordExpiryTime);
        configuration.setMaxConnectionPoolSize(maxPoolSize);
    }

    @Override
    public void resetFormChanges() {
        ConnectionDetailSettings configuration = getConfiguration();
        setSelection(encodingComboBox, CharsetOption.get(configuration.getCharset()));
        sessionManagementCheckBox.setSelected(configuration.isEnableSessionManagement());
        ddlFileBindingCheckBox.setSelected(configuration.isEnableDdlFileBinding());
        databaseLoggingCheckBox.setSelected(configuration.isEnableDatabaseLogging());
        autoConnectCheckBox.setSelected(configuration.isConnectAutomatically());
        restoreWorkspaceCheckBox.setSelected(configuration.isRestoreWorkspace());
        restoreWorkspaceDeepCheckBox.setSelected(configuration.isRestoreWorkspaceDeep());
        setSelection(environmentTypesComboBox, configuration.getEnvironmentType());
        idleTimeTextField.setText(Integer.toString(configuration.getIdleTimeToDisconnect()));
        idleTimePoolTextField.setText(Integer.toString(configuration.getIdleTimeToDisconnectPool()));
        passwordExpiryTextField.setText(Integer.toString(configuration.getCredentialExpiryTime()));
        maxPoolSizeTextField.setText(Integer.toString(configuration.getMaxConnectionPoolSize()));
        alternativeStatementDelimiterTextField.setText(configuration.getAlternativeStatementDelimiter());
    }

    private EnvironmentConfigLocalListener presentationChangeListener = new EnvironmentConfigLocalListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {
            EnvironmentType selectedItem = getSelection(environmentTypesComboBox);
            EnvironmentTypeId selectedId = selectedItem == null ? EnvironmentType.DEFAULT.getId() : selectedItem.getId();
            selectedItem = environmentTypes.getEnvironmentType(selectedId);

            List<EnvironmentType> newEnvironmentTypes = new ArrayList<>(environmentTypes.getEnvironmentTypes());
            newEnvironmentTypes.add(0, EnvironmentType.DEFAULT);
            initComboBox(environmentTypesComboBox, newEnvironmentTypes);
            setSelection(environmentTypesComboBox, selectedItem);
            notifyPresentationChanges();
        }
    };
}
