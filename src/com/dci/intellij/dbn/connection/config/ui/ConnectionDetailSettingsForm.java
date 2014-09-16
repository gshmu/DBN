package com.dci.intellij.dbn.connection.config.ui;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentChangeListener;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.environment.options.EnvironmentPresentationChangeListener;
import com.dci.intellij.dbn.common.environment.options.EnvironmentSettings;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorUtil;
import com.dci.intellij.dbn.common.properties.ui.PropertiesEditorForm;
import com.dci.intellij.dbn.common.ui.ComboBoxUtil;
import com.dci.intellij.dbn.connection.ConnectionStatusListener;
import com.dci.intellij.dbn.connection.config.ConnectionDetailSettings;
import com.dci.intellij.dbn.options.general.GeneralProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.ColorIcon;
import com.intellij.util.ui.UIUtil;

public class ConnectionDetailSettingsForm extends ConfigurationEditorForm<ConnectionDetailSettings>{
    private JPanel mainPanel;
    private JComboBox encodingComboBox;
    private JCheckBox autoCommitCheckBox;
    private JPanel propertiesPanel;
    private JComboBox environmentTypesComboBox;
    private JPanel generalGroupPanel;
    private JPanel propertiesGroupPanel;
    private JTextField maxPoolSizeTextField;
    private JTextField idleTimeTextField;
    private JCheckBox ddlFileBindingCheckBox;
    private JTextField alternativeStatementDelimiterTextField;
    private JCheckBox autoConnectCheckBox;
    private JLabel autoConnectHintLabel;
    private JTextArea autoConnectTextArea;

    private PropertiesEditorForm propertiesEditorForm;

    public ConnectionDetailSettingsForm(final ConnectionDetailSettings configuration) {
        super(configuration);

        Map<String, String> properties = new HashMap<String, String>();
        properties.putAll(configuration.getProperties());
        updateBorderTitleForeground(generalGroupPanel);
        updateBorderTitleForeground(propertiesGroupPanel);

        propertiesEditorForm = new PropertiesEditorForm(properties);
        propertiesEditorForm.setMoveButtonsVisible(false);
        propertiesPanel.add(propertiesEditorForm.getComponent(), BorderLayout.CENTER);
        for (Charset charset : Charset.availableCharsets().values()) {
            encodingComboBox.addItem(charset);
        }

        DefaultComboBoxModel environmentTypesModel = createEnvironmentTypesModel(getEnvironmentTypes());
        environmentTypesComboBox.setModel(environmentTypesModel);
        resetChanges();

        registerComponent(mainPanel);

        environmentTypesComboBox.setRenderer(environmentTypeCellRenderer);
        environmentTypesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifyPresentationChanges();
            }
        });

        autoConnectHintLabel.setText("");
        autoConnectHintLabel.setIcon(Icons.COMMON_INFO);
        autoConnectTextArea.setBackground(UIUtil.getPanelBackground());
        autoConnectTextArea.setText("NOTE: If \"Connect automatically\" is not selected, the system will not restore the entire workspace the next time you open the project (i.e. all open editors for this connection will not be reopened automatically).");
        autoConnectTextArea.setFont(UIUtil.getLabelFont());
        autoConnectTextArea.setForeground(Colors.HINT_COLOR);

        boolean visibleHint = !autoConnectCheckBox.isSelected();
        autoConnectHintLabel.setVisible(visibleHint);
        autoConnectTextArea.setVisible(visibleHint);


        EventManager.subscribe(configuration.getProject(), EnvironmentPresentationChangeListener.TOPIC, presentationChangeListener);
    }

    public void notifyPresentationChanges() {
        Project project = getConfiguration().getProject();
        ConnectionPresentationChangeListener listener = EventManager.notify(project, ConnectionPresentationChangeListener.TOPIC);
        EnvironmentType environmentType = (EnvironmentType) environmentTypesComboBox.getSelectedItem();
        Color color = environmentType == null ? null : environmentType.getColor();
        listener.presentationChanged(null, null, color, getConfiguration().getConnectionId(), null);
    }

    protected ActionListener createActionListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Object source = e.getSource();
                if (source == autoConnectCheckBox){
                    boolean visibleHint = !autoConnectCheckBox.isSelected();
                    autoConnectHintLabel.setVisible(visibleHint);
                    autoConnectTextArea.setVisible(visibleHint);
                }
                getConfiguration().setModified(true);
            }
        };
    }

    private final ColoredListCellRenderer environmentTypeCellRenderer = new ColoredListCellRenderer() {
        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus) {
            EnvironmentType environmentType = (EnvironmentType) value;
            if (environmentType != null) {
                Color color = environmentType.getColor();
                String name = environmentType.getName();

                if (color != null) {
                    setIcon(new ColorIcon(12, color));
                }

                if (name != null) {
                    append(name, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }
                
            }
        }
    };

    private EnvironmentTypeBundle getEnvironmentTypes() {
        Project project = getConfiguration().getProject();
        EnvironmentSettings environmentSettings = GeneralProjectSettings.getInstance(project).getEnvironmentSettings();
        return environmentSettings.getEnvironmentTypes();
    }

    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public void applyChanges() throws ConfigurationException {
        ConnectionDetailSettings configuration = getConfiguration();

        Map<String, String> newProperties = propertiesEditorForm.getProperties();
        Charset newCharset = (Charset) encodingComboBox.getSelectedItem();
        boolean newAutoCommit = autoCommitCheckBox.isSelected();
        boolean newDdlFileBinding = ddlFileBindingCheckBox.isSelected();
        EnvironmentType newEnvironmentType = (EnvironmentType) environmentTypesComboBox.getSelectedItem();
        String newEnvironmentTypeId = newEnvironmentType.getId();

        boolean settingsChanged =
                !configuration.getProperties().equals(newProperties) ||
                !configuration.getCharset().equals(newCharset) ||
                configuration.isEnableAutoCommit() != newAutoCommit ||
                configuration.isEnableDdlFileBinding() != newDdlFileBinding;

        boolean environmentChanged =
                !configuration.getEnvironmentType().getId().equals(newEnvironmentTypeId);


        configuration.setEnvironmentTypeId(newEnvironmentTypeId);
        configuration.setProperties(newProperties);
        configuration.setCharset(newCharset);
        configuration.setEnableAutoCommit(newAutoCommit);
        configuration.setConnectAutomatically(autoConnectCheckBox.isSelected());
        configuration.setEnableDdlFileBinding(newDdlFileBinding);
        configuration.setAlternativeStatementDelimiter(alternativeStatementDelimiterTextField.getText());
        int idleTimeToDisconnect = ConfigurationEditorUtil.validateIntegerInputValue(idleTimeTextField, "Idle Time to Disconnect (minutes)", 0, 60, "");
        int maxPoolSize = ConfigurationEditorUtil.validateIntegerInputValue(maxPoolSizeTextField, "Max Connection Pool Size", 3, 20, "");
        configuration.setIdleTimeToDisconnect(idleTimeToDisconnect);
        configuration.setMaxConnectionPoolSize(maxPoolSize);

        Project project = getConfiguration().getProject();
        if (environmentChanged) {
            EnvironmentChangeListener listener = EventManager.notify(project, EnvironmentChangeListener.TOPIC);
            listener.environmentConfigChanged(newEnvironmentTypeId);
        }

        if (settingsChanged) {
            ConnectionStatusListener listener = EventManager.notify(project, ConnectionStatusListener.TOPIC);
            listener.statusChanged(getConfiguration().getConnectionId());
        }

    }

    @Override
    public void resetChanges() {
        ConnectionDetailSettings configuration = getConfiguration();
        encodingComboBox.setSelectedItem(configuration.getCharset());
        propertiesEditorForm.setProperties(configuration.getProperties());
        autoCommitCheckBox.setSelected(configuration.isEnableAutoCommit());
        ddlFileBindingCheckBox.setSelected(configuration.isEnableDdlFileBinding());
        autoConnectCheckBox.setSelected(configuration.isConnectAutomatically());
        environmentTypesComboBox.setSelectedItem(configuration.getEnvironmentType());
        idleTimeTextField.setText(Integer.toString(configuration.getIdleTimeToDisconnect()));
        maxPoolSizeTextField.setText(Integer.toString(configuration.getMaxConnectionPoolSize()));
        alternativeStatementDelimiterTextField.setText(configuration.getAlternativeStatementDelimiter());
    }

    private DefaultComboBoxModel createEnvironmentTypesModel(EnvironmentTypeBundle environmentTypes) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(EnvironmentType.DEFAULT);
        ComboBoxUtil.addItems(model, environmentTypes.clone());
        return model;
    }

    private EnvironmentPresentationChangeListener presentationChangeListener = new EnvironmentPresentationChangeListener() {
        @Override
        public void settingsChanged(EnvironmentTypeBundle environmentTypes) {
            EnvironmentType selectedItem = (EnvironmentType) environmentTypesComboBox.getSelectedItem();
            String selectedId = selectedItem == null ? EnvironmentType.DEFAULT.getId() : selectedItem.getId();
            selectedItem = environmentTypes.getEnvironmentType(selectedId);

            DefaultComboBoxModel model = createEnvironmentTypesModel(environmentTypes);
            environmentTypesComboBox.setModel(model);
            environmentTypesComboBox.setSelectedItem(selectedItem);
            notifyPresentationChanges();
        }
    };

    @Override
    public void dispose() {
        EventManager.unsubscribe(presentationChangeListener);
        super.dispose();
    }

}
