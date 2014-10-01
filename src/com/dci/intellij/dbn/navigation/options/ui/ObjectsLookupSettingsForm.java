package com.dci.intellij.dbn.navigation.options.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.KeyUtil;
import com.dci.intellij.dbn.common.ui.list.CheckBoxList;
import com.dci.intellij.dbn.navigation.options.ObjectsLookupSettings;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.options.ConfigurationException;

public class ObjectsLookupSettingsForm extends ConfigurationEditorForm<ObjectsLookupSettings> {
    private JPanel mainPanel;
    private JScrollPane lookupObjectsScrollPane;
    private JRadioButton loadRadioButton;
    private JRadioButton noLoadRadioButton;
    private JRadioButton promptRadioButton;
    private JRadioButton noPromptRadioButton;
    private CheckBoxList lookupObjectsList;

    public ObjectsLookupSettingsForm(ObjectsLookupSettings configuration) {
        super(configuration);
        Shortcut[] shortcuts = KeyUtil.getShortcuts("DBNavigator.Actions.Navigation.GotoDatabaseObject");
        TitledBorder border = (TitledBorder) mainPanel.getBorder();
        border.setTitle("Lookup Objects (" + KeymapUtil.getShortcutsText(shortcuts) + ")");
        updateBorderTitleForeground(mainPanel);

        lookupObjectsList = new CheckBoxList(configuration.getLookupObjectTypes());
        lookupObjectsScrollPane.setViewportView(lookupObjectsList);

        boolean databaseLoadActive = getConfiguration().getForceDatabaseLoad().value();
        loadRadioButton.setSelected(databaseLoadActive);
        noLoadRadioButton.setSelected(!databaseLoadActive);

        boolean promptConnectionSelectionActive = getConfiguration().getPromptConnectionSelection().value();
        promptRadioButton.setSelected(promptConnectionSelectionActive);
        noPromptRadioButton.setSelected(!promptConnectionSelectionActive);

        registerComponents(
                lookupObjectsList,
                promptRadioButton,
                noPromptRadioButton,
                loadRadioButton,
                noLoadRadioButton);
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        lookupObjectsList.applyChanges();
        getConfiguration().getForceDatabaseLoad().applyChanges(loadRadioButton);
        getConfiguration().getPromptConnectionSelection().applyChanges(promptRadioButton);
    }

    @Override
    public void resetFormChanges() {
        getConfiguration().getForceDatabaseLoad().applyChanges(loadRadioButton);
        getConfiguration().getPromptConnectionSelection().applyChanges(promptRadioButton);
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
