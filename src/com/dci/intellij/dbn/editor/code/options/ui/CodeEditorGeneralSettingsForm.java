package com.dci.intellij.dbn.editor.code.options.ui;

import com.dci.intellij.dbn.common.options.SettingsChangeNotifier;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.editor.code.options.CodeEditorGeneralSettings;
import com.dci.intellij.dbn.language.common.SpellcheckingSettingsListener;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class CodeEditorGeneralSettingsForm extends ConfigurationEditorForm<CodeEditorGeneralSettings> {
    private JCheckBox showObjectNavigationGutterCheckBox;
    private JCheckBox specDeclarationGutterCheckBox;
    private JPanel mainPanel;
    private JCheckBox enableSpellchecking;
    private JCheckBox enableReferenceSpellchecking;

    public CodeEditorGeneralSettingsForm(CodeEditorGeneralSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        resetFormChanges();
        registerComponent(mainPanel);
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public void applyFormChanges() throws ConfigurationException {
        CodeEditorGeneralSettings settings = getConfiguration();
        settings.setShowObjectsNavigationGutter(showObjectNavigationGutterCheckBox.isSelected());
        settings.setShowSpecDeclarationNavigationGutter(specDeclarationGutterCheckBox.isSelected());

        boolean enableSpellchecking = this.enableSpellchecking.isSelected();
        boolean enableReferenceSpellchecking = this.enableReferenceSpellchecking.isSelected();
        boolean spellcheckingSettingsChanged =
                settings.isEnableSpellchecking() != enableSpellchecking ||
                settings.isEnableReferenceSpellchecking() != enableReferenceSpellchecking;

        settings.setEnableSpellchecking(enableSpellchecking);
        settings.setEnableReferenceSpellchecking(enableReferenceSpellchecking);

        if (spellcheckingSettingsChanged) {
            SettingsChangeNotifier.register(() -> EventUtil.notify(getProject(), SpellcheckingSettingsListener.TOPIC).settingsChanged());
        }
    }

    public void resetFormChanges() {
        CodeEditorGeneralSettings settings = getConfiguration();
        showObjectNavigationGutterCheckBox.setSelected(settings.isShowObjectsNavigationGutter());
        specDeclarationGutterCheckBox.setSelected(settings.isShowSpecDeclarationNavigationGutter());
        enableSpellchecking.setSelected(settings.isEnableSpellchecking());
        enableReferenceSpellchecking.setSelected(settings.isEnableReferenceSpellchecking());
    }
}
