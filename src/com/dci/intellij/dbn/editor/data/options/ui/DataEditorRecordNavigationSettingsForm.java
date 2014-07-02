package com.dci.intellij.dbn.editor.data.options.ui;

import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.data.record.navigation.RecordNavigationTarget;
import com.dci.intellij.dbn.editor.data.options.DataEditorRecordNavigationSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class DataEditorRecordNavigationSettingsForm extends ConfigurationEditorForm<DataEditorRecordNavigationSettings> {
    private JPanel mainPanel;
    private JRadioButton viewerRadioButton;
    private JRadioButton editorRadioButton;
    private JRadioButton askRadioButton;


    public DataEditorRecordNavigationSettingsForm(DataEditorRecordNavigationSettings configuration) {
        super(configuration);
        updateBorderTitleForeground(mainPanel);
        resetChanges();

        registerComponent(viewerRadioButton);
        registerComponent(editorRadioButton);
        registerComponent(askRadioButton);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public void applyChanges() throws ConfigurationException {
        DataEditorRecordNavigationSettings configuration = getConfiguration();

        RecordNavigationTarget navigationTarget =
                viewerRadioButton.isSelected() ? RecordNavigationTarget.VIEWER :
                editorRadioButton.isSelected() ? RecordNavigationTarget.EDITOR :
                askRadioButton.isSelected() ? RecordNavigationTarget.ASK :
                RecordNavigationTarget.VIEWER;
        configuration.setNavigationTarget(navigationTarget);
    }

    public void resetChanges() {
        DataEditorRecordNavigationSettings configuration = getConfiguration();
        RecordNavigationTarget navigationTarget = configuration.getNavigationTarget();
        if (navigationTarget == RecordNavigationTarget.VIEWER) viewerRadioButton.setSelected(true); else
        if (navigationTarget == RecordNavigationTarget.EDITOR) editorRadioButton.setSelected(true); else
        if (navigationTarget == RecordNavigationTarget.ASK) askRadioButton.setSelected(true);
    }
}
